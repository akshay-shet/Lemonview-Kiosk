package com.lemonview.ai.model

import java.io.Serializable

data class SkinResult(
    val skinHealthPercentage: Int,
    val skinTone: String,
    val skinAnalysisExplanation: String,
    val diseasesLevel: Map<String, Int>, // Disease name to percentage
    val recommendations: List<String>, // 5-6 recommendations
    val timestamp: Long = System.currentTimeMillis(),
    // Confidence & Accuracy Metrics
    val overallConfidence: Float = 0.85f, // 0.0-1.0 (0-100%)
    val diseaseDetectionConfidence: Map<String, Float> = emptyMap(), // Per-disease confidence
    val skinTypeConfidence: Float = 0.85f, // Confidence in detected skin type
    val imageQualityScore: Float = 0.85f, // Overall image quality (0.0-1.0)
    val analysisQuality: String = "GOOD", // EXCELLENT, GOOD, FAIR, POOR
    val modelVersions: String = "TFLite:1.0|ONNX:1.0|Gemini:pro-vision", // Track model versions
    val analysisTimeMs: Long = 0L // Time taken for analysis in milliseconds
) : Serializable

data class Routine2Weeks(
    val week_1: String,
    val week_2: String
)

data class DailyRoutine(
    val day: Int,
    val morning: List<String>,
    val afternoon: List<String>,
    val evening: List<String>
) : Serializable

data class RoutinePlan14Days(
    val analysisSkinResult: SkinResult,
    val dailyRoutines: List<DailyRoutine> // 14 items
) : Serializable
