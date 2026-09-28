package com.ranveeraggarwal.macrolens.estimate.gemini

import com.ranveeraggarwal.macrolens.estimate.MealEstimate
import com.ranveeraggarwal.macrolens.estimate.MealEstimateException
import com.ranveeraggarwal.macrolens.estimate.MealEstimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Base64

/**
 * [MealEstimator] backed by Google's Gemini REST API (generateContent).
 *
 * Sends the prompt and the photo in one request and asks for JSON that matches
 * [MEAL_RESPONSE_SCHEMA], so the answer can be parsed without guesswork.
 */
class GeminiMealEstimator(
    private val apiKey: String,
    private val httpClient: OkHttpClient,
    private val model: String = DEFAULT_MODEL,
) : MealEstimator {

    private val json = Json { explicitNulls = false }

    override suspend fun estimate(photoJpeg: ByteArray): MealEstimate {
        if (apiKey.isBlank()) {
            throw MealEstimateException(
                "No Gemini API key. Add GEMINI_API_KEY to local.properties and rebuild the app."
            )
        }
        val request = buildHttpRequest(photoJpeg)
        val responseBody = send(request)
        return GeminiResponseParser.parse(responseBody)
    }

    private fun buildHttpRequest(photoJpeg: ByteArray): Request {
        val requestJson = json.encodeToString(buildGenerateContentRequest(photoJpeg))
        return Request.Builder()
            .url("$BASE_URL/models/$model:generateContent")
            // Sent as a header rather than in the URL so it never ends up in error messages.
            .header("x-goog-api-key", apiKey)
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun buildGenerateContentRequest(photoJpeg: ByteArray): GenerateContentRequest {
        val photoPart = Part(
            inlineData = InlineData(
                mimeType = "image/jpeg",
                data = Base64.getEncoder().encodeToString(photoJpeg),
            )
        )
        val promptPart = Part(text = MEAL_PROMPT.trim())
        return GenerateContentRequest(
            contents = listOf(Content(parts = listOf(photoPart, promptPart))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseJsonSchema = Json.parseToJsonElement(MEAL_RESPONSE_SCHEMA),
            ),
        )
    }

    /** Runs the HTTP call on a background thread and returns the body of a successful response. */
    private suspend fun send(request: Request): String = withContext(Dispatchers.IO) {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body.string()
                if (!response.isSuccessful) {
                    throw MealEstimateException(describeHttpError(response.code, body))
                }
                body
            }
        } catch (e: IOException) {
            throw MealEstimateException("Couldn't reach Gemini. Check your internet connection.", e)
        }
    }

    private fun describeHttpError(statusCode: Int, errorBody: String): String {
        val googleMessage = GeminiResponseParser.parseErrorMessage(errorBody)
        return if (googleMessage != null) {
            "Gemini error ($statusCode): $googleMessage"
        } else {
            "Gemini error ($statusCode)."
        }
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-3.8-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

        /** JSON Schema the model's answer must follow. Mirrors [MealJson]. */
        private const val MEAL_RESPONSE_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "items": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "name": { "type": "string", "description": "Short name of the food" },
                  "grams": { "type": "number", "description": "Estimated portion weight in grams" },
                  "kcal": { "type": "number", "description": "Estimated calories for this portion" }
                },
                "required": ["name", "grams", "kcal"]
              }
            }
          },
          "required": ["items"]
        }
        """
    }
}
