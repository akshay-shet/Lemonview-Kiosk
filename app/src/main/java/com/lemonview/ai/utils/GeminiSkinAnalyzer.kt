package com.lemonview.ai.utils

import android.graphics.Bitmap
import android.util.Base64
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object GeminiSkinAnalyzer {
    /**
     * Analyze skin from actual image bitmap for personalized, image-specific results
     * This ensures different users with different skin conditions get different analyses
     */
    suspend fun analyzeSkinFromImage(
        imageBitmap: Bitmap,
        language: String = "en"
    ): String {
        val model = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = ApiConfig.GEMINI_API_KEY
        )

        // Convert bitmap to base64 for Gemini API
        val base64Image = bitmapToBase64(imageBitmap)

        val prompt = """
You are an expert dermatologist with 20+ years of clinical experience in analyzing facial skin conditions.

Analyze THIS SPECIFIC facial image and respond ONLY in valid JSON format. Look for:
1. Individual skin characteristics visible in THIS image
2. Specific skin conditions present
3. Localized problem areas
4. Skin texture and appearance details

CRITICAL: Generate UNIQUE results based on what you see in THIS SPECIFIC IMAGE.
Each image should produce different results based on actual visible skin conditions.

JSON structure (respond ONLY with this JSON, no explanation):

{
  "health_percentage": <0-100 based on THIS image's visible condition>,
  "skin_diseases": [<list of diseases visible in THIS image>],
  "disease_severity_map": {
    "여드름 (Acne)": <0-100>,
    "검은반점 (Dark Spots)": <0-100>,
    "주근깨 (Freckles)": <0-100>,
    "검은머리 (Blackheads)": <0-100>,
    "화이트헤드 (Whiteheads)": <0-100>,
    "염증 (Inflammation)": <0-100>,
    "건조함 (Dryness)": <0-100>,
    "유분기 (Oiliness)": <0-100>,
    "민감성 (Sensitivity)": <0-100>,
    "홍조 (Redness)": <0-100>,
    "주름 (Wrinkles)": <0-100>,
    "잔주름 (Fine Lines)": <0-100>,
    "튼살 (Stretch Marks)": <0-100>,
    "흉터 (Scars)": <0-100>,
    "색소침착 (Hyperpigmentation)": <0-100>,
    "칙칙함 (Dullness)": <0-100>,
    "탄력저하 (Loss of Elasticity)": <0-100>,
    "모공확대 (Enlarged Pores)": <0-100>
  },
  "skin_type_detected": "<Oily/Dry/Combination/Sensitive/Normal>",
  "skin_tone": "<Fair/Light/Medium/Olive/Tan/Deep>",
  "analysis_explanation": "<Detailed analysis in $language describing what you see in THIS specific image>",
  "medical_recommendations": [<5-6 specific recommendations based on THIS image>],
  "metrics": {
    "oil": <0-100>,
    "hydration": <0-100>,
    "pores": <0-100>,
    "pigmentation": <0-100>,
    "sensitivity": <0-100>,
    "texture_smoothness": <0-100>,
    "elasticity": <0-100>,
    "inflammation": <0-100>
  }
}

RULES:
- Analyze ONLY what is visible in THIS image
- Generate DIFFERENT results for DIFFERENT images
- Be specific about location of skin issues (cheeks, forehead, nose, chin, etc.)
- Provide precise severity percentages (0-100) for each disease
- Do NOT return generic results
- Output ONLY valid JSON
- Language: $language
"""

        return withContext(Dispatchers.IO) {
            try {
                // Send image and prompt to Gemini using multimodal API
                val response = model.generateContent(
                    com.google.ai.client.generativeai.type.content {
                        text(prompt)
                        image(imageBitmap)
                    }
                )
                response.text ?: buildDefaultErrorResponse()
            } catch (e: Exception) {
                android.util.Log.e("GeminiSkinAnalyzer", "Error analyzing skin: ${e.message}", e)
                e.printStackTrace()
                buildDefaultErrorResponse()
            }
        }
    }

    /**
     * Legacy method for backward compatibility - now uses image if available
     */
    suspend fun analyzeSkin(
        imageDescription: String,
        language: String
    ): String {
        // This is a fallback - in production, always use analyzeSkinFromImage with actual bitmap
        val model = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = ApiConfig.GEMINI_API_KEY
        )

        val prompt = """
Based on this skin description: "$imageDescription"

Analyze and respond ONLY in valid JSON format with detailed disease severity map.
Generate SPECIFIC results matching the description provided.

JSON structure:
{
  "health_percentage": <0-100>,
  "skin_diseases": [<diseases matching description>],
  "disease_severity_map": {
    "여드름 (Acne)": <0-100>,
    "검은반점 (Dark Spots)": <0-100>,
    "주근깨 (Freckles)": <0-100>,
    "검은머리 (Blackheads)": <0-100>,
    "화이트헤드 (Whiteheads)": <0-100>,
    "염증 (Inflammation)": <0-100>,
    "건조함 (Dryness)": <0-100>,
    "유분기 (Oiliness)": <0-100>,
    "민감성 (Sensitivity)": <0-100>,
    "홍조 (Redness)": <0-100>,
    "주름 (Wrinkles)": <0-100>,
    "잔주름 (Fine Lines)": <0-100>,
    "튼살 (Stretch Marks)": <0-100>,
    "흉터 (Scars)": <0-100>,
    "색소침착 (Hyperpigmentation)": <0-100>,
    "칙칙함 (Dullness)": <0-100>,
    "탄력저하 (Loss of Elasticity)": <0-100>,
    "모공확대 (Enlarged Pores)": <0-100>
  },
  "skin_type_detected": "<skin type>",
  "analysis_explanation": "<detailed explanation>",
  "medical_recommendations": [<recommendations>],
  "metrics": {
    "oil": <0-100>,
    "hydration": <0-100>,
    "pores": <0-100>,
    "pigmentation": <0-100>,
    "sensitivity": <0-100>,
    "texture_smoothness": <0-100>,
    "elasticity": <0-100>,
    "inflammation": <0-100>
  }
}

Language: $language
Output ONLY JSON.
"""

        return withContext(Dispatchers.IO) {
            try {
                val response = model.generateContent(prompt)
                response.text ?: buildDefaultErrorResponse()
            } catch (e: Exception) {
                e.printStackTrace()
                buildDefaultErrorResponse()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun buildDefaultErrorResponse(): String {
        return """{
            "health_percentage": 0,
            "skin_diseases": [],
            "disease_severity_map": {
                "여드름 (Acne)": 0,
                "검은반점 (Dark Spots)": 0,
                "주근깨 (Freckles)": 0,
                "검은머리 (Blackheads)": 0,
                "화이트헤드 (Whiteheads)": 0,
                "염증 (Inflammation)": 0,
                "건조함 (Dryness)": 0,
                "유분기 (Oiliness)": 0,
                "민감성 (Sensitivity)": 0,
                "홍조 (Redness)": 0,
                "주름 (Wrinkles)": 0,
                "잔주름 (Fine Lines)": 0,
                "튼살 (Stretch Marks)": 0,
                "흉터 (Scars)": 0,
                "색소침착 (Hyperpigmentation)": 0,
                "칙칙함 (Dullness)": 0,
                "탄력저하 (Loss of Elasticity)": 0,
                "모공확대 (Enlarged Pores)": 0
            },
            "skin_type_detected": "Unknown",
            "analysis_explanation": "Error in analysis",
            "medical_recommendations": [],
            "metrics": {
                "oil": 0,
                "hydration": 0,
                "pores": 0,
                "pigmentation": 0,
                "sensitivity": 0,
                "texture_smoothness": 0,
                "elasticity": 0,
                "inflammation": 0
            }
        }"""
    }
}