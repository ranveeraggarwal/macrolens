package com.ranveeraggarwal.macrolens.ui

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ranveeraggarwal.macrolens.estimate.FoodItem
import com.ranveeraggarwal.macrolens.estimate.MealEstimate

/** Connects the screen to the ViewModel and to the system camera app. */
@Composable
fun MealRoute(viewModel: MealViewModel) {
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onPhotoTaken(saved)
    }

    MealScreen(
        uiState = viewModel.uiState,
        onSnapMealClick = {
            try {
                takePicture.launch(viewModel.photoUriForCamera())
            } catch (e: ActivityNotFoundException) {
                viewModel.onCameraUnavailable()
            }
        },
        onRetryClick = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(
    uiState: MealUiState,
    onSnapMealClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Macro Lens") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onSnapMealClick,
                enabled = uiState !is MealUiState.Estimating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Snap meal")
            }

            when (uiState) {
                MealUiState.NoPhoto -> {
                    Text("Take a photo of your meal to estimate its calories.")
                }
                is MealUiState.Estimating -> {
                    uiState.photo?.let { MealPhoto(it) }
                    EstimatingIndicator()
                }
                is MealUiState.Estimated -> {
                    MealPhoto(uiState.photo)
                    EstimateResult(uiState.estimate)
                }
                is MealUiState.Failed -> {
                    uiState.photo?.let { MealPhoto(it) }
                    ErrorMessage(uiState.message, uiState.canRetry, onRetryClick)
                }
            }
        }
    }
}

@Composable
private fun MealPhoto(photo: Bitmap) {
    Image(
        bitmap = photo.asImageBitmap(),
        contentDescription = "Your meal",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .clip(RoundedCornerShape(12.dp)),
    )
}

@Composable
private fun EstimatingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator()
        Spacer(Modifier.width(16.dp))
        Text("Estimating calories…")
    }
}

@Composable
private fun EstimateResult(estimate: MealEstimate) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (estimate.items.isEmpty()) {
                Text("No food recognised in this photo.")
            }
            for (item in estimate.items) {
                FoodItemRow(item)
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${estimate.totalKcal} kcal", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoodItemRow(item: FoodItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name)
            Text(
                "${item.grams} g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("${item.kcal} kcal")
    }
}

@Composable
private fun ErrorMessage(message: String, canRetry: Boolean, onRetryClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message)
            if (canRetry) {
                OutlinedButton(onClick = onRetryClick) {
                    Text("Retry")
                }
            }
        }
    }
}
