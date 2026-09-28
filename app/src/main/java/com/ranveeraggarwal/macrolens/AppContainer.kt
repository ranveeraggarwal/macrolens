package com.ranveeraggarwal.macrolens

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ranveeraggarwal.macrolens.estimate.MealEstimator
import com.ranveeraggarwal.macrolens.estimate.gemini.GeminiMealEstimator
import com.ranveeraggarwal.macrolens.photo.PhotoDownscaler
import com.ranveeraggarwal.macrolens.photo.PhotoStorage
import com.ranveeraggarwal.macrolens.ui.MealViewModel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * The one place where the app's pieces are created and connected.
 * To swap the AI provider, change [mealEstimator] here.
 */
class AppContainer(context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        // Image analysis can take a while, so allow more than OkHttp's 10s default.
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    private val mealEstimator: MealEstimator = GeminiMealEstimator(
        apiKey = BuildConfig.GEMINI_API_KEY,
        httpClient = httpClient,
    )

    private val photoStorage = PhotoStorage(context.applicationContext)

    private val photoDownscaler = PhotoDownscaler()

    val mealViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MealViewModel(mealEstimator, photoStorage, photoDownscaler)
        }
    }
}
