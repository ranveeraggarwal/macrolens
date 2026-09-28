package com.ranveeraggarwal.macrolens.estimate

/**
 * Turns a photo of a meal into a calorie estimate.
 *
 * This is the only thing the rest of the app knows about the AI provider,
 * so swapping Gemini for something else means writing a new implementation
 * and changing one line in AppContainer.
 */
interface MealEstimator {

    /**
     * @param photoJpeg the meal photo, already downscaled and JPEG-encoded.
     * @throws MealEstimateException with a message that can be shown to the user.
     */
    suspend fun estimate(photoJpeg: ByteArray): MealEstimate
}

/** A failure whose [message] is written for the user to read. */
class MealEstimateException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
