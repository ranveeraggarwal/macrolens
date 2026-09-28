package com.ranveeraggarwal.macrolens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ranveeraggarwal.macrolens.ui.MealRoute
import com.ranveeraggarwal.macrolens.ui.MealViewModel
import com.ranveeraggarwal.macrolens.ui.theme.MacroLensTheme

class MainActivity : ComponentActivity() {

    private val mealViewModel: MealViewModel by viewModels {
        (application as MacroLensApplication).container.mealViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacroLensTheme {
                MealRoute(mealViewModel)
            }
        }
    }
}
