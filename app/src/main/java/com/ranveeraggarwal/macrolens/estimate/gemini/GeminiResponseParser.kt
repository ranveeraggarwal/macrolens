package com.ranveeraggarwal.macrolens.estimate.gemini

import com.ranveeraggarwal.macrolens.estimate.FoodItem
import com.ranveeraggarwal.macrolens.estimate.MealEstimate
import com.ranveeraggarwal.macrolens.estimate.MealEstimateException
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

/**
 * Turns the raw body of a successful generateContent call into a [MealEstimate].
 *
 * Kept separate from the HTTP code so it can be unit tested with fixture files.
 */
object GeminiResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    /** @throws MealEstimateException if the body isn't a usable meal estimate. */
    fun parse(responseBody: String): MealEstimate {
        val response = decode<GenerateContentResponse>(responseBody)
        val modelText = extractModelText(response)
        val meal = decode<MealJson>(modelText)
        return toMealEstimate(meal)
    }

    /** Reads Google's error message from a failed call, or null if there isn't one. */
    fun parseErrorMessage(errorBody: String): String? {
        return try {
            json.decodeFromString<ErrorResponse>(errorBody).error.message
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun extractModelText(response: GenerateContentResponse): String {
        val blockReason = response.promptFeedback?.blockReason
        if (blockReason != null) {
            throw MealEstimateException("Gemini refused to look at this photo ($blockReason).")
        }

        val candidate = response.candidates.firstOrNull()
            ?: throw MealEstimateException("Gemini returned no answer.")

        val text = candidate.content?.parts.orEmpty()
            .mapNotNull { it.text }
            .joinToString(separator = "")

        if (text.isBlank()) {
            val reason = candidate.finishReason ?: "unknown reason"
            throw MealEstimateException("Gemini returned an empty answer ($reason).")
        }
        return text
    }

    private fun toMealEstimate(meal: MealJson): MealEstimate {
        val items = meal.items.map { item ->
            FoodItem(
                name = item.name.trim(),
                grams = item.grams.roundToInt(),
                kcal = item.kcal.roundToInt(),
            )
        }
        return MealEstimate(items)
    }

    private inline fun <reified T> decode(text: String): T {
        return try {
            json.decodeFromString<T>(text)
        } catch (e: IllegalArgumentException) {
            // Covers both malformed JSON and missing fields (SerializationException is a subclass).
            throw MealEstimateException("Gemini's answer wasn't in the expected format.", e)
        }
    }
}
