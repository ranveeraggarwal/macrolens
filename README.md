# Macro Lens

Snap a photo of a meal and get an estimated calorie count from Gemini.

## Setup

1. Get a Gemini API key from Google AI Studio.
2. Add it to `local.properties` in the project root (this file is git-ignored):

   ```
   GEMINI_API_KEY=your-key-here
   ```

3. Build and install: `./gradlew installDebug`

The key is compiled into `BuildConfig`, so rebuild after changing it.

## Where things live

| What | Where |
| --- | --- |
| Wiring (what uses what) | `AppContainer.kt` |
| Prompt sent to Gemini | `estimate/gemini/MealPrompt.kt` |
| Provider-agnostic interface | `estimate/MealEstimator.kt` |
| Gemini REST call | `estimate/gemini/GeminiMealEstimator.kt` |
| Screen and state | `ui/` |
| Photo storage and downscaling | `photo/` |

Run the unit tests with `./gradlew testDebugUnitTest`. They use fixture
responses and never call the real API.
