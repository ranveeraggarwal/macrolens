package com.ranveeraggarwal.macrolens.estimate

/** One food on the plate, as estimated from the photo. */
data class FoodItem(
    val name: String,
    val grams: Int,
    val kcal: Int,
)

/** Everything we estimated about one meal photo. */
data class MealEstimate(
    val items: List<FoodItem>,
) {
    /** Summed here rather than trusted from the model, so it always matches the list. */
    val totalKcal: Int
        get() = items.sumOf { it.kcal }
}
