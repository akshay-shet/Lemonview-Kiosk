package com.lemonview.ai.utils

import android.graphics.Bitmap
import android.util.Log

/**
 * ConfidenceMetrics - Calculate AI analysis confidence and accuracy scores
 * Evaluates image quality, lighting, and model confidence
 */
object ConfidenceMetrics {
    private const val TAG = "ConfidenceMetrics"

    /**
     * Calculate image quality confidence based on resolution
     * Range: 0.0 - 1.0 (0% - 100%)
     */
    fun calculateImageQualityConfidence(bitmap: Bitmap): Float {
        val pixels = bitmap.width.toLong() * bitmap.height.toLong()
        return when {
            pixels < 500000 -> 0.40f      // 40% - Very low
            pixels < 1000000 -> 0.55f     // 55% - Low
            pixels < 2000000 -> 0.70f     // 70% - Medium
            pixels < 3000000 -> 0.82f     // 82% - Good
            pixels < 5000000 -> 0.90f     // 90% - Very good
            pixels < 8000000 -> 0.95f     // 95% - Excellent
            else -> 0.98f                 // 98% - Premium
        }
    }

    /**
     * Calculate lighting condition confidence
     * Optimal brightness: 80-150 out of 255
     */
    fun calculateLightingConfidence(bitmap: Bitmap): Float {
        try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            
            var totalBrightness = 0.0
            var darkPixels = 0
            var brightPixels = 0
            
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val brightness = (r + g + b) / 3.0
                totalBrightness += brightness
                
                if (brightness < 50) darkPixels++
                if (brightness > 200) brightPixels++
            }
            
            val avgBrightness = totalBrightness / pixels.size
            val shadowRatio = darkPixels.toFloat() / pixels.size
            val highlightRatio = brightPixels.toFloat() / pixels.size
            
            Log.d(TAG, "Lighting Analysis - Avg: $avgBrightness, Shadows: ${(shadowRatio*100).toInt()}%, Highlights: ${(highlightRatio*100).toInt()}%")
            
            return when {
                avgBrightness < 50 -> 0.45f           // Too dark
                avgBrightness < 80 -> 0.70f           // Dim
                avgBrightness <= 150 -> 0.95f         // Optimal
                avgBrightness <= 180 -> 0.85f         // Slightly bright
                avgBrightness <= 200 -> 0.75f         // Bright
                else -> 0.60f                         // Overexposed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating lighting: ${e.message}")
            return 0.75f
        }
    }

    /**
     * Analyze face coverage and positioning
     * Confidence higher when face occupies 30-70% of image
     */
    fun calculateFaceCoverageConfidence(bitmap: Bitmap): Float {
        try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            
            // Simple skin tone detection (rough estimate)
            var skinTonePixels = 0
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                
                // Skin tone typically has higher R and G than B
                if (r > 95 && g > 40 && b > 20 && r > b && (r - b) > 15) {
                    skinTonePixels++
                }
            }
            
            val skinCoverage = skinTonePixels.toFloat() / pixels.size
            
            return when {
                skinCoverage < 0.15f -> 0.50f       // Face too small
                skinCoverage < 0.30f -> 0.75f       // Suboptimal
                skinCoverage <= 0.70f -> 0.95f      // Optimal
                skinCoverage <= 0.85f -> 0.85f      // Face too large
                else -> 0.60f                       // Face fills image
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating face coverage: ${e.message}")
            return 0.80f
        }
    }

    /**
     * Overall image quality score combining all factors
     */
    fun calculateOverallImageQuality(bitmap: Bitmap): Float {
        val resolution = calculateImageQualityConfidence(bitmap)
        val lighting = calculateLightingConfidence(bitmap)
        val coverage = calculateFaceCoverageConfidence(bitmap)
        
        return (resolution * 0.40f + lighting * 0.35f + coverage * 0.25f).coerceIn(0f, 1f)
    }

    /**
     * Determine analysis quality rating
     */
    fun determineAnalysisQuality(
        imageQuality: Float,
        modelConfidence: Float
    ): String {
        val avgScore = (imageQuality * 0.6f + modelConfidence * 0.4f).coerceIn(0f, 1f)
        return when {
            avgScore >= 0.90f -> "EXCELLENT"
            avgScore >= 0.80f -> "GOOD"
            avgScore >= 0.65f -> "FAIR"
            else -> "POOR"
        }
    }

    /**
     * Calculate per-disease confidence score
     * Combines model prediction with image quality metrics
     */
    fun calculateDiseaseConfidence(
        detectionScore: Float,
        imageQualityConfidence: Float,
        lightingConfidence: Float
    ): Float {
        val baseConfidence = detectionScore.coerceIn(0f, 1f)
        return (
            baseConfidence * 0.60f +
            imageQualityConfidence * 0.25f +
            lightingConfidence * 0.15f
        ).coerceIn(0f, 1f)
    }

    /**
     * Confidence level description for user
     */
    fun getConfidenceDescription(confidence: Float): String {
        return when {
            confidence >= 0.90f -> "매우 높음 (Very High)"
            confidence >= 0.75f -> "높음 (High)"
            confidence >= 0.60f -> "중간 (Medium)"
            confidence >= 0.45f -> "낮음 (Low)"
            else -> "매우 낮음 (Very Low)"
        }
    }

    /**
     * Generate confidence warning message
     */
    fun generateConfidenceWarning(confidence: Float): String {
        return when {
            confidence >= 0.85f -> "분석 결과가 매우 신뢰할 수 있습니다.\n(Results are highly reliable)"
            confidence >= 0.70f -> "분석 결과가 대체로 신뢰할 수 있습니다.\n(Results are reasonably reliable)"
            confidence >= 0.55f -> "분석 결과를 참고하되, 정확한 진단은 피부과 상담을 권장합니다.\n(Consult a dermatologist for accurate diagnosis)"
            else -> "이미지 품질이 낮아 재촬영을 권장합니다.\n(Please retake the photo for better results)"
        }
    }
}
