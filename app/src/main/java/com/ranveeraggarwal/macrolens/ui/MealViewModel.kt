package com.ranveeraggarwal.macrolens.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranveeraggarwal.macrolens.estimate.MealEstimateException
import com.ranveeraggarwal.macrolens.estimate.MealEstimator
import com.ranveeraggarwal.macrolens.photo.PhotoDownscaler
import com.ranveeraggarwal.macrolens.photo.PhotoStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MealViewModel(
    private val mealEstimator: MealEstimator,
    private val photoStorage: PhotoStorage,
    private val photoDownscaler: PhotoDownscaler,
) : ViewModel() {

    var uiState: MealUiState by mutableStateOf(MealUiState.NoPhoto)
        private set

    private var estimateJob: Job? = null

    /** Where the camera app should save the next photo. */
    fun photoUriForCamera(): Uri = photoStorage.mealPhotoUriForCamera()

    /** Called when the camera app returns. [saved] is false if the user backed out. */
    fun onPhotoTaken(saved: Boolean) {
        if (saved) {
            estimateMealPhoto()
        }
    }

    fun onCameraUnavailable() {
        uiState = MealUiState.Failed(
            photo = null,
            message = "No camera app found on this phone.",
            canRetry = false,
        )
    }

    fun onRetry() {
        estimateMealPhoto()
    }

    private fun estimateMealPhoto() {
        estimateJob?.cancel()
        estimateJob = viewModelScope.launch {
            var photo: Bitmap? = null
            uiState = MealUiState.Estimating(photo = null)
            try {
                val downscaled = photoDownscaler.downscale(photoStorage.mealPhotoFile)
                photo = downscaled.bitmap
                uiState = MealUiState.Estimating(photo)

                val estimate = mealEstimator.estimate(downscaled.jpegBytes)
                uiState = MealUiState.Estimated(downscaled.bitmap, estimate)
            } catch (e: CancellationException) {
                throw e // A newer photo replaced this one; let the coroutine stop quietly.
            } catch (e: MealEstimateException) {
                uiState = MealUiState.Failed(photo, e.message ?: "Something went wrong.")
            } catch (e: Exception) {
                // Anything unexpected (e.g. an unreadable photo) is shown, not crashed on.
                uiState = MealUiState.Failed(photo, "Something went wrong: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }
}
