package com.lemonview.ai.model

import java.io.Serializable

data class MakeupProduct(
    val category: String,        // Foundation, Blush, EyeShadow, Lipstick, Eyeliner
    val brand: String,
    val productName: String,
    val shade: String,
    val hexColor: String,
    val description: String
) : Serializable

data class MakeupRecommendationProfile(
    val skinTone: String,        // Fair, Medium, Deep
    val skinToneRGB: String,
    val lookName: String,        // e.g., "Warm Everyday Glow", "Bold Party Look"
    val lookDescription: String,
    val foundation: MakeupProduct,
    val blush: MakeupProduct,
    val eyeShadow: MakeupProduct,
    val eyeliner: MakeupProduct,
    val lipstick: MakeupProduct,
    val applicationSteps: List<String>,
    val overallDescription: String,
    val confidenceScore: Float
) : Serializable

data class MakeupAnalysisResult(
    val imageQualityScore: Float,
    val profile: MakeupRecommendationProfile,
    val timestamp: Long = System.currentTimeMillis(),
    val skinToneEstimate: String,
    val faceDetected: Boolean,
    val detectedColorHex: String = "",
    val detectedColorNameEN: String = "",
    val detectedColorNameKR: String = ""
) : Serializable
