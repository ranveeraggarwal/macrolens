package com.ranveeraggarwal.macrolens.estimate.gemini

import com.ranveeraggarwal.macrolens.estimate.FoodItem
import com.ranveeraggarwal.macrolens.estimate.MealEstimateException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GeminiResponseParserTest {

    @Test
    fun `parses food items from a successful response`() {
        val estimate = GeminiResponseParser.parse(readFixture("meal_response.json"))

        val expectedItems = listOf(
            FoodItem(name = "White rice", grams = 180, kcal = 234),
            FoodItem(name = "Chicken curry", grams = 220, kcal = 311),
            FoodItem(name = "Mango lassi", grams = 250, kcal = 180),
        )
        assertEquals(expectedItems, estimate.items)
    }

    @Test
    fun `total is the sum of the item calories`() {
        val estimate = GeminiResponseParser.parse(readFixture("meal_response.json"))

        assertEquals(234 + 311 + 180, estimate.totalKcal)
    }

    @Test
    fun `an empty item list is a valid estimate`() {
        val body = responseWithModelText("""{"items": []}""")

        val estimate = GeminiResponseParser.parse(body)

        assertEquals(emptyList<FoodItem>(), estimate.items)
        assertEquals(0, estimate.totalKcal)
    }

    @Test
    fun `a blocked prompt gives a readable error`() {
        val error = assertThrows(MealEstimateException::class.java) {
            GeminiResponseParser.parse(readFixture("blocked_response.json"))
        }

        assertEquals("Gemini refused to look at this photo (SAFETY).", error.message)
    }

    @Test
    fun `model text that is not the expected JSON gives a readable error`() {
        val body = responseWithModelText("Looks like about 600 calories to me!")

        val error = assertThrows(MealEstimateException::class.java) {
            GeminiResponseParser.parse(body)
        }

        assertEquals("Gemini's answer wasn't in the expected format.", error.message)
    }

    @Test
    fun `a body that is not JSON at all gives a readable error`() {
        val error = assertThrows(MealEstimateException::class.java) {
            GeminiResponseParser.parse("<html>Bad gateway</html>")
        }

        assertEquals("Gemini's answer wasn't in the expected format.", error.message)
    }

    @Test
    fun `reads Google's message from an error body`() {
        val message = GeminiResponseParser.parseErrorMessage(readFixture("error_response.json"))

        assertEquals("API key not valid. Please pass a valid API key.", message)
    }

    @Test
    fun `an unreadable error body gives no message`() {
        assertNull(GeminiResponseParser.parseErrorMessage("upstream connect error"))
    }

    private fun readFixture(name: String): String {
        val stream = javaClass.classLoader!!.getResourceAsStream("gemini/$name")
            ?: error("Missing test fixture gemini/$name")
        return stream.bufferedReader().use { it.readText() }
    }

    /** Wraps [modelText] in the smallest response Gemini could send. */
    private fun responseWithModelText(modelText: String): String {
        val escapedText = modelText.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"candidates": [{"content": {"parts": [{"text": "$escapedText"}]}, "finishReason": "STOP"}]}"""
    }
}
