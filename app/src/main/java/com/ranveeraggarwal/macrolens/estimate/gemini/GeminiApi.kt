package com.ranveeraggarwal.macrolens.estimate.gemini

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Plain data classes mirroring the parts of the Gemini REST API we use.
// Reference: https://ai.google.dev/api/generate-content
// Only the fields we read or send are listed; everything else is ignored.

// ----- Request -----

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig,
)

@Serializable
data class Content(
    val parts: List<Part>,
)

/** A part holds either text or inline binary data (our photo), never both. */
@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null,
)

@Serializable
data class InlineData(
    val mimeType: String,
    /** Base64-encoded bytes. */
    val data: String,
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String,
    val responseJsonSchema: JsonElement,
)

// ----- Response -----

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList(),
    val promptFeedback: PromptFeedback? = null,
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null,
)

/** Body Google sends back with a non-2xx status. */
@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
)

@Serializable
data class ErrorDetails(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
)

// ----- The JSON the model writes inside the response text -----

@Serializable
data class MealJson(
    val items: List<FoodItemJson>,
)

/** Numbers are Doubles because the model sometimes writes "120.0" instead of "120". */
@Serializable
data class FoodItemJson(
    val name: String,
    val grams: Double,
    val kcal: Double,
)
