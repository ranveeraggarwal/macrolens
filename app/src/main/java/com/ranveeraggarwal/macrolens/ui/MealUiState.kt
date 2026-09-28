package com.ranveeraggarwal.macrolens.ui

import android.graphics.Bitmap
import com.ranveeraggarwal.macrolens.estimate.MealEstimate

/** Everything the meal screen needs to draw itself. */
sealed interface MealUiState {

    /** Nothing snapped yet. */
    data object NoPhoto : MealUiState

    /** Waiting for the estimate. [photo] is null while the photo is still being loaded. */
    data class Estimating(val photo: Bitmap?) : MealUiState

    data class Estimated(val photo: Bitmap, val estimate: MealEstimate) : MealUiState

    /** [canRetry] is false when trying again can't help, e.g. there is no camera app. */
    data class Failed(
        val photo: Bitmap?,
        val message: String,
        val canRetry: Boolean = true,
    ) : MealUiState
}
