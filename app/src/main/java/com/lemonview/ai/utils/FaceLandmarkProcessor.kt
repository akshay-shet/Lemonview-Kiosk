package com.lemonview.ai.utils

import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.sqrt

/**
 * Processes facial landmarks and extracts useful information for skin analysis
 * Classifies landmarks by facial feature type and calculates metrics
 */
class FaceLandmarkProcessor {

    /**
     * Data class to hold landmark information
     */
    data class LandmarkInfo(
        val type: String,
        val position: PointF,
        val landmarkType: Int
    )

    /**
     * Data class for face metrics
     */
    data class FaceMetrics(
        val totalLandmarks: Int,
        val eyeLandmarks: List<LandmarkInfo>,
        val noseLandmarks: List<LandmarkInfo>,
        val mouthLandmarks: List<LandmarkInfo>,
        val cheekLandmarks: List<LandmarkInfo>,
        val jawLandmarks: List<LandmarkInfo>,
        val faceArea: Float,
        val faceCenterX: Float,
        val faceCenterY: Float,
        val headRotationX: Float,
        val headRotationY: Float,
        val headRotationZ: Float,
        val smilingProbability: Float,
        val leftEyeOpenProbability: Float,
        val rightEyeOpenProbability: Float
    )

    /**
     * Extract and classify all landmarks from a detected face
     */
    fun extractLandmarks(face: Face): FaceMetrics {
        val eyeLandmarks = mutableListOf<LandmarkInfo>()
        val noseLandmarks = mutableListOf<LandmarkInfo>()
        val mouthLandmarks = mutableListOf<LandmarkInfo>()
        val cheekLandmarks = mutableListOf<LandmarkInfo>()
        val jawLandmarks = mutableListOf<LandmarkInfo>()

        // Process all landmarks
        for (landmark in face.allLandmarks) {
            val landmarkInfo = LandmarkInfo(
                type = getLandmarkTypeName(landmark.landmarkType),
                position = landmark.position,
                landmarkType = landmark.landmarkType
            )

            when (landmark.landmarkType) {
                FaceLandmark.LEFT_EYE,
                FaceLandmark.RIGHT_EYE -> eyeLandmarks.add(landmarkInfo)

                FaceLandmark.NOSE_BASE -> noseLandmarks.add(landmarkInfo)

                FaceLandmark.MOUTH_BOTTOM -> mouthLandmarks.add(landmarkInfo)

                FaceLandmark.LEFT_CHEEK,
                FaceLandmark.RIGHT_CHEEK -> cheekLandmarks.add(landmarkInfo)

                else -> jawLandmarks.add(landmarkInfo)
            }
        }

        // Calculate face metrics
        val boundingBox = face.boundingBox
        val faceArea = boundingBox.width().toFloat() * boundingBox.height().toFloat()
        val faceCenterX = boundingBox.centerX().toFloat()
        val faceCenterY = boundingBox.centerY().toFloat()

        return FaceMetrics(
            totalLandmarks = face.allLandmarks.size,
            eyeLandmarks = eyeLandmarks,
            noseLandmarks = noseLandmarks,
            mouthLandmarks = mouthLandmarks,
            cheekLandmarks = cheekLandmarks,
            jawLandmarks = jawLandmarks,
            faceArea = faceArea,
            faceCenterX = faceCenterX,
            faceCenterY = faceCenterY,
            headRotationX = face.headEulerAngleX ?: 0f,
            headRotationY = face.headEulerAngleY ?: 0f,
            headRotationZ = face.headEulerAngleZ ?: 0f,
            smilingProbability = face.smilingProbability ?: 0f,
            leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f,
            rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f
        )
    }

    /**
     * Get human-readable landmark type name
     */
    fun getLandmarkTypeName(landmarkType: Int): String {
        return when (landmarkType) {
            FaceLandmark.LEFT_EYE -> "Left Eye"
            FaceLandmark.RIGHT_EYE -> "Right Eye"
            FaceLandmark.NOSE_BASE -> "Nose"
            FaceLandmark.MOUTH_BOTTOM -> "Mouth Bottom"
            FaceLandmark.LEFT_CHEEK -> "Left Cheek"
            FaceLandmark.RIGHT_CHEEK -> "Right Cheek"
            else -> "Unknown"
        }
    }

    /**
     * Calculate distance between two landmarks (useful for proportionality)
     */
    fun calculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Assess face clarity based on landmark confidence and positioning
     */
    fun assessFaceClarity(metrics: FaceMetrics): Float {
        // Score based on number of detected landmarks (max 100)
        val landmarkScore = (metrics.totalLandmarks / 20f) * 100f
        
        // Score based on eyes being open
        val eyeOpenScore = ((metrics.leftEyeOpenProbability + metrics.rightEyeOpenProbability) / 2f) * 100f
        
        // Combine scores
        val clarity = (landmarkScore + eyeOpenScore) / 2f
        return clarity.coerceIn(0f, 100f)
    }

    /**
     * Check if face is well-positioned for analysis
     */
    fun isFaceWellPositioned(metrics: FaceMetrics): Boolean {
        // Face should be relatively straight (rotation angles less than 20 degrees)
        val isRelativelyStrait = 
            kotlin.math.abs(metrics.headRotationX) < 20f &&
            kotlin.math.abs(metrics.headRotationY) < 20f &&
            kotlin.math.abs(metrics.headRotationZ) < 20f

        // Should have minimum landmarks detected
        val sufficientLandmarks = metrics.totalLandmarks >= 8

        // Eyes should be open
        val eyesOpen = metrics.leftEyeOpenProbability > 0.3f && metrics.rightEyeOpenProbability > 0.3f

        return isRelativelyStrait && sufficientLandmarks && eyesOpen
    }

    /**
     * Generate a user-friendly message about face positioning
     */
    fun getPositioningFeedback(metrics: FaceMetrics): String {
        return when {
            metrics.totalLandmarks < 8 -> "Move closer to camera"
            kotlin.math.abs(metrics.headRotationX) > 20f -> "Tilt your chin forward/backward"
            kotlin.math.abs(metrics.headRotationY) > 20f -> "Turn your face left/right"
            kotlin.math.abs(metrics.headRotationZ) > 20f -> "Straighten your head"
            metrics.leftEyeOpenProbability < 0.3f || metrics.rightEyeOpenProbability < 0.3f -> "Open your eyes wider"
            else -> "Perfect! Ready to capture"
        }
    }

    /**
     * Get face quality score (0-100)
     */
    fun calculateQualityScore(metrics: FaceMetrics): Float {
        var score = 100f

        // Penalize for poor positioning
        if (kotlin.math.abs(metrics.headRotationX) > 15f) score -= 10f
        if (kotlin.math.abs(metrics.headRotationY) > 15f) score -= 10f
        if (kotlin.math.abs(metrics.headRotationZ) > 15f) score -= 5f

        // Penalize for eyes closed
        val avgEyeOpen = (metrics.leftEyeOpenProbability + metrics.rightEyeOpenProbability) / 2f
        if (avgEyeOpen < 0.5f) score -= 20f

        // Reward for sufficient landmarks
        if (metrics.totalLandmarks >= 15) score += 10f

        return score.coerceIn(0f, 100f)
    }
}
