package com.ranveeraggarwal.macrolens.estimate.gemini

/**
 * The instructions sent to Gemini along with the photo.
 *
 * Edit freely. The shape of the answer (items with name, grams and kcal) is
 * enforced separately by the JSON schema in GeminiMealEstimator, so this text
 * only needs to describe *how* to estimate, not the output format.
 */
const val MEAL_PROMPT = """
You are a nutrition assistant. Look at the photo of a meal and estimate its calories.

- List each distinct food or drink you can see as a separate item.
- Give each item a short, plain name, e.g. "white rice" or "grilled chicken breast".
- Estimate the portion size in grams from what is visible, using the plate,
  cutlery and hands as a sense of scale.
- Estimate the calories (kcal) for that portion.
- Include visible sauces, oils, dressings and drinks.
- If you are unsure, give your single best estimate rather than a range.
- If there is no food in the photo, return an empty list of items.
"""
