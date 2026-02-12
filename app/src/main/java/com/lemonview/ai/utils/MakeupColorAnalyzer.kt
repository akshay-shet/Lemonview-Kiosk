package com.lemonview.ai.utils

import android.graphics.Bitmap
import android.util.Log
import com.lemonview.ai.model.MakeupProduct
import com.lemonview.ai.model.MakeupRecommendationProfile
import com.lemonview.ai.model.MakeupAnalysisResult
import kotlin.math.abs

/**
 * Korean Makeup Analyzer with Real Products
 * Provides personalized makeup recommendations based on face and skin tone analysis
 */
object MakeupColorAnalyzer {

    private const val TAG = "MakeupColorAnalyzer"

    // Makeup Profiles Database - Based on skin tone with real Korean beauty products
    private val makeupProfiles = mapOf(
        "Fair" to MakeupRecommendationProfile(
            skinTone = "Fair",
            skinToneRGB = "RGB(230,190,170)",
            lookName = "Luminous Natural Glow",
            lookDescription = "This look focuses on enhancing your natural features with luminous, clean skin and subtle warm tones perfect for everyday elegance.",
            foundation = MakeupProduct(
                category = "Foundation",
                brand = "",
                productName = "",
                shade = "140N",
                hexColor = "#E8D4C0",
                description = "Light, luminous foundation matched for fair skin"
            ),
            blush = MakeupProduct(
                category = "Blush",
                brand = "",
                productName = "",
                shade = "Orgasm",
                hexColor = "#E8B8A0",
                description = "Universal peach blush for a natural flush"
            ),
            eyeShadow = MakeupProduct(
                category = "Eye Shadow",
                brand = "",
                productName = "",
                shade = "Half Baked",
                hexColor = "#A8886E",
                description = "Soft taupe-brown with matte finish"
            ),
            eyeliner = MakeupProduct(
                category = "Eyeliner",
                brand = "",
                productName = "",
                shade = "Deep Brown",
                hexColor = "#4A3728",
                description = "Soft brown liner for natural definition"
            ),
            lipstick = MakeupProduct(
                category = "Lipstick",
                brand = "",
                productName = "",
                shade = "Taupe",
                hexColor = "#C8A89C",
                description = "Warm neutral lip shade"
            ),
            applicationSteps = listOf(
                "1. 깨끗하고 촉촉한 얼굴로 시작하세요. 파운데이션을 고르게 펴 발라 자연스러운 마무리를 완성하세요.",
                "2. 푹신한 브러시로 부드러운 블러시를 광대뼈에 발라 관자놀이 쪽으로 펴 바닙니다.",
                "3. 중립적인 아이섀도를 눈꺼풀 전체에 펴 바른 후 부드럽게 블렌딩하세요.",
                "4. 짙은 컬러의 아이라이너로 속눈썹 라인을 따라 자연스럽게 정의합니다.",
                "5. 피부톤에 어울리는 립스틱으로 마무리하세요."
            ),
            overallDescription = "This luminous natural glow is perfect for everyday wear, offering a polished yet effortless appearance. The warm undertones enhance your fair complexion beautifully.",
            confidenceScore = 0.95f
        ),
        "Medium" to MakeupRecommendationProfile(
            skinTone = "Medium",
            skinToneRGB = "RGB(210,160,140)",
            lookName = "Warm Everyday Glow",
            lookDescription = "This look focuses on enhancing your natural features with warm, flattering tones. It's perfect for everyday wear, offering a polished yet effortless appearance.",
            foundation = MakeupProduct(
                category = "Foundation",
                brand = "",
                productName = "",
                shade = "300",
                hexColor = "#B98D6E",
                description = "Warm foundation with slight glow for medium skin tone"
            ),
            blush = MakeupProduct(
                category = "Blush",
                brand = "",
                productName = "",
                shade = "Orgasm",
                hexColor = "#E8B873",
                description = "Warm peach blush for balanced flush"
            ),
            eyeShadow = MakeupProduct(
                category = "Eye Shadow",
                brand = "",
                productName = "",
                shade = "Half Baked",
                hexColor = "#A67B5B",
                description = "Warm brown for warm-toned medium skin"
            ),
            eyeliner = MakeupProduct(
                category = "Eyeliner",
                brand = "",
                productName = "",
                shade = "Dark Brown",
                hexColor = "#5C4033",
                description = "Dark brown eyeliner for soft definition"
            ),
            lipstick = MakeupProduct(
                category = "Lipstick",
                brand = "",
                productName = "",
                shade = "Taupe",
                hexColor = "#C27F70",
                description = "Warm taupe-brown for medium skin"
            ),
            applicationSteps = listOf(
                "1. 깨끗하고 촉촉한 얼굴로 시작하세요. 파운데이션을 고르게 펴 발라 자연스러운 마무리를 완성하세요.",
                "2. 푹신한 붓으로 부드러운 블러시를 광대뼈에 발라 관자놀이 쪽으로 펴 바릅니다.",
                "3. 중립적인 아이섀도를 눈꺼풀 전체에 펴 바른 후 부드럽게 블렌딩하세요.",
                "4. 짙은 컬러의 아이라이너로 속눈썹 라인을 따라 자연스럽게 정의합니다.",
                "5. 피부톤에 어울리는 립스틱으로 마무리하세요."
            ),
            overallDescription = "This warm everyday glow brings out your natural warmth and creates a balanced, sophisticated look that works perfectly for any occasion.",
            confidenceScore = 0.94f
        ),
        "Deep" to MakeupRecommendationProfile(
            skinTone = "Deep",
            skinToneRGB = "RGB(180,130,110)",
            lookName = "Rich Warm Elegance",
            lookDescription = "This look emphasizes rich, warm tones that complement deep skin beautifully. Perfect for creating a luxurious, sophisticated appearance.",
            foundation = MakeupProduct(
                category = "Foundation",
                brand = "",
                productName = "",
                shade = "385",
                hexColor = "#9C6D4F",
                description = "Rich warm foundation for deep skin tone"
            ),
            blush = MakeupProduct(
                category = "Blush",
                brand = "",
                productName = "",
                shade = "Taj Mahal",
                hexColor = "#D4846F",
                description = "Rich rust-terracotta blush for natural flush"
            ),
            eyeShadow = MakeupProduct(
                category = "Eye Shadow",
                brand = "",
                productName = "",
                shade = "Bronzed",
                hexColor = "#8B6F47",
                description = "Warm bronze for deep skin tone"
            ),
            eyeliner = MakeupProduct(
                category = "Eyeliner",
                brand = "",
                productName = "",
                shade = "Deep Brown",
                hexColor = "#3D2817",
                description = "Deep brown eyeliner for maximum definition"
            ),
            lipstick = MakeupProduct(
                category = "Lipstick",
                brand = "",
                productName = "",
                shade = "Deep Red",
                hexColor = "#8B4545",
                description = "Deep red with warm undertones"
            ),
            applicationSteps = listOf(
                "1. 깨끗하고 촉촉한 얼굴로 시작하세요. 파운데이션을 고르게 펴 발라 자연스러운 마무리를 완성하세요.",
                "2. 푹신한 붓으로 풍부한 블러시를 광대뼈에 발라 관자놀이 쪽으로 펴 바려 자연스러운 음영을 만듭니다.",
                "3. 따뜻한 브론즈 아이섀도를 눈꺼풀 전체에 펴 바른 후 블렌딩하여 깊이감을 살립니다.",
                "4. 짙은 컬러의 아이라이너로 속눈썹 라인을 따라 선명하게 정의합니다.",
                "5. 깊은 피부톤을 돋보이게 하는 대담한 립스틱으로 마무리하세요."
            ),
            overallDescription = "This rich warm elegance creates a luxurious look that celebrates your deep skin tone with sophisticated, warm-toned makeup.",
            confidenceScore = 0.93f
        )
    )

    /**
     * Analyze face and skin tone from bitmap and return RGB, skin tone, and undertone
     */
    fun analyzeFaceAndSkinTone(bitmap: Bitmap): Triple<String, String, String> {
        return try {
            val width = bitmap.width
            val height = bitmap.height

            // Sample from multiple facial regions for better accuracy
            // Forehead, cheeks, nose, chin, neck
            val regions = listOf(
                Pair(width / 2, height / 6),       // Forehead
                Pair(width / 3, height / 2),       // Left cheek
                Pair(2 * width / 3, height / 2),   // Right cheek
                Pair(width / 2, height / 2),       // Nose / central face
                Pair(width / 2, 2 * height / 3),   // Chin
                Pair(width / 2, 3 * height / 4)    // Neck
            )

            var totalR = 0
            var totalG = 0
            var totalB = 0
            var totalPixels = 0
            val sampleSize = 30 // smaller patches to avoid background contamination

            // Collect samples from all regions
            for ((centerX, centerY) in regions) {
                for (x in (centerX - sampleSize/2) until (centerX + sampleSize/2)) {
                    for (y in (centerY - sampleSize/2) until (centerY + sampleSize/2)) {
                        if (x in 0 until width && y in 0 until height) {
                            try {
                                val pixel = bitmap.getPixel(x, y)
                                val r = (pixel shr 16) and 0xFF
                                val g = (pixel shr 8) and 0xFF
                                val b = pixel and 0xFF

                                // Filter out non-skin pixels
                                if (isLikelySkinColor(r, g, b)) {
                                    totalR += r
                                    totalG += g
                                    totalB += b
                                    totalPixels++
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Error sampling pixel: ${e.message}")
                            }
                        }
                    }
                }
            }

            // If not enough skin pixels were found, relax sampling (fallback)
            if (totalPixels < 50) {
                val fallbackSize = 80
                for (x in 0 until width step 20) {
                    for (y in 0 until height step 20) {
                        try {
                            val pixel = bitmap.getPixel(x, y)
                            val r = (pixel shr 16) and 0xFF
                            val g = (pixel shr 8) and 0xFF
                            val b = pixel and 0xFF
                            if (isLikelySkinColor(r, g, b) || r > 100) {
                                totalR += r
                                totalG += g
                                totalB += b
                                totalPixels++
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }

            // Calculate average skin color
            val avgR = if (totalPixels > 0) totalR / totalPixels else 150
            val avgG = if (totalPixels > 0) totalG / totalPixels else 100
            val avgB = if (totalPixels > 0) totalB / totalPixels else 80

            val rgbString = "RGB($avgR,$avgG,$avgB)"

            // Convert to YCbCr and HSV for tone and undertone detection
            val y = 0.299 * avgR + 0.587 * avgG + 0.114 * avgB
            val cr = 128 + 0.5 * avgR - 0.418688 * avgG - 0.081312 * avgB
            val hsv = FloatArray(3)
            android.graphics.Color.RGBToHSV(avgR, avgG, avgB, hsv)
            val hue = hsv[0]
            val sat = hsv[1]

            val undertone = if (avgR - avgB > 8) "Warm" else "Cool"

            // More accurate classification using brightness and undertone
            val skinTone = when {
                y > 200 -> "Fair"
                y > 160 -> "Medium"
                y > 120 -> "Medium"
                else -> "Deep"
            }

            Log.d(TAG, "Advanced analysis - RGB: $rgbString, Y=$y, Cr=$cr, Hue=$hue, Sat=$sat, Undertone=$undertone, SkinTone: $skinTone, Pixels: $totalPixels")
            Triple(rgbString, skinTone, undertone)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing face: ${e.message}", e)
            Triple("RGB(150,100,80)", "Medium", "Neutral")
        }
    }

    /**
     * Filter to identify likely skin color pixels
     */
    private fun isLikelySkinColor(r: Int, g: Int, b: Int): Boolean {
        if (r < 50 || g < 30 || b < 15) return false
        if (r > 255 || g > 255 || b > 255) return false
        if (r <= g) return false
        
        val maxChannel = maxOf(r, g, b)
        val minChannel = minOf(r, g, b)
        if (maxChannel - minChannel > 100) return false
        
        return true
    }

    /**
     * Calculate image quality
     */
    fun calculateImageQuality(bitmap: Bitmap): Float {
        return try {
            val width = bitmap.width
            val height = bitmap.height

            val targetWidth = 1080f
            val targetHeight = 1920f
            val resolutionScore = minOf(
                (width / targetWidth) * 100,
                (height / targetHeight) * 100,
                100f
            )

            val clarityScore = detectImageClarity(bitmap)
            val qualityScore = (resolutionScore * 0.7f) + (clarityScore * 0.3f)

            Log.d(TAG, "Image Quality - Resolution: $resolutionScore, Clarity: $clarityScore, Overall: $qualityScore")
            qualityScore
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating quality: ${e.message}", e)
            50f
        }
    }

    private fun detectImageClarity(bitmap: Bitmap): Float {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            var edgeCount = 0
            var totalPixels = 0

            for (y in 0 until (height - 5) step 5) {
                for (x in 0 until (width - 5) step 5) {
                    try {
                        val pixel1 = bitmap.getPixel(x, y)
                        val pixel2 = bitmap.getPixel(x + 5, y)

                        val lum1 = brightness(pixel1)
                        val lum2 = brightness(pixel2)

                        if (abs(lum1 - lum2) > 30) {
                            edgeCount++
                        }
                        totalPixels++
                    } catch (e: Exception) {
                        Log.d(TAG, "Error sampling clarity pixel")
                    }
                }
            }

            val clarityScore = if (totalPixels > 0) {
                minOf((edgeCount.toFloat() / totalPixels) * 300, 100f)
            } else {
                50f
            }

            clarityScore
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting clarity: ${e.message}", e)
            50f
        }
    }

    private fun brightness(pixel: Int): Float {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return 0.299f * r + 0.587f * g + 0.114f * b
    }

    /**
     * Get makeup recommendation based on bitmap analysis
     */
    private fun clamp(v: Int) = when {
        v < 0 -> 0
        v > 255 -> 255
        else -> v
    }

    private fun toHex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", clamp(r), clamp(g), clamp(b))
    }

    private fun shiftColor(r: Int, g: Int, b: Int, rShift: Int, gShift: Int, bShift: Int): String {
        return toHex(clamp(r + rShift), clamp(g + gShift), clamp(b + bShift))
    }

    // Convert RGB to descriptive shade names (EN / KR)
    private fun shadeNameForRGB(r: Int, g: Int, b: Int, undertone: String): Pair<String, String> {
        val y = 0.299 * r + 0.587 * g + 0.114 * b
        val base = when {
            y > 220 -> "Porcelain"
            y > 200 -> "Ivory"
            y > 170 -> "Light Beige"
            y > 140 -> "Beige"
            y > 110 -> "Tan"
            y > 90 -> "Caramel"
            else -> "Deep Cocoa"
        }
        val en = if (undertone == "Warm") "$base (Warm)" else if (undertone == "Cool") "$base (Cool)" else base
        val krMap = mapOf(
            "Porcelain" to "포슬린",
            "Ivory" to "아이보리",
            "Light Beige" to "라이트 베이지",
            "Beige" to "베이지",
            "Tan" to "탄",
            "Caramel" to "카라멜",
            "Deep Cocoa" to "딥 코코아"
        )
        val krBase = krMap[base] ?: base
        val kr = if (undertone == "Warm") "웜 $krBase" else if (undertone == "Cool") "쿨 $krBase" else krBase
        return Pair(en, kr)
    }

    /**
     * Public helper to get color name from hex
     */
    fun colorNameForHex(hex: String): Pair<String, String> {
        return try {
            val clean = hex.replace("#", "")
            val r = Integer.valueOf(clean.substring(0, 2), 16)
            val g = Integer.valueOf(clean.substring(2, 4), 16)
            val b = Integer.valueOf(clean.substring(4, 6), 16)
            // For this helper, compute a simple undertone
            val undertone = if (r - b > 8) "Warm" else "Cool"
            shadeNameForRGB(r, g, b, undertone)
        } catch (e: Exception) {
            Pair("Neutral", "중성")
        }
    }

    fun getMakeupRecommendation(bitmap: Bitmap): MakeupAnalysisResult {
        // Backwards compatible call — uses classic heuristic pipeline
        return getMakeupRecommendation(null, bitmap)
    }

    /**
     * Context-aware inference. If a TFLite model is present in assets, use it for skin tone prediction.
     * Otherwise fallback to the original heuristic implementation.
     */
    fun getMakeupRecommendation(context: android.content.Context?, bitmap: Bitmap): MakeupAnalysisResult {
        return try {
            val modelInterpreter = if (context != null) MakeupModelInterpreter(context) else null

            val (rgbString, heurSkinTone, undertone) = analyzeFaceAndSkinTone(bitmap)
            val qualityScore = calculateImageQuality(bitmap)

            var detectedSkinTone = heurSkinTone
            var modelConfidence = 0f

            // If model exists, use it to improve skin tone estimate
            if (modelInterpreter != null && modelInterpreter.isModelAvailable()) {
                val (label, conf) = modelInterpreter.predictSkinTone(bitmap)
                if (label.isNotEmpty() && conf > 0.3f) {
                    detectedSkinTone = label
                    modelConfidence = conf
                }
                modelInterpreter.close()
            }

            // Parse avg RGB
            val match = Regex("RGB\\((\\d+),(\\d+),(\\d+)\\)").find(rgbString)
            val avgR = match?.groupValues?.get(1)?.toIntOrNull() ?: 150
            val avgG = match?.groupValues?.get(2)?.toIntOrNull() ?: 100
            val avgB = match?.groupValues?.get(3)?.toIntOrNull() ?: 80

            var profile = makeupProfiles[detectedSkinTone] ?: makeupProfiles["Medium"]!!

            // Generate dynamic swatches and neutral shade names
            val foundationHex = toHex((avgR * 0.98).toInt(), (avgG * 0.95).toInt(), (avgB * 0.92).toInt())
            val blushHex = shiftColor(avgR, avgG, avgB, 35, 5, -10) // slightly redder
            val eyeHex = shiftColor(avgR, avgG, avgB, -30, -20, -10) // darker neutral
            val linerHex = shiftColor(avgR, avgG, avgB, -80, -60, -60) // deep brown/black
            val lipHex = shiftColor(avgR, avgG, avgB, 60, 10, 0) // richer lip tone

            // Detected color and readable names
            val detectedHex = toHex(avgR, avgG, avgB)
            val (detectedNameEN, detectedNameKR) = shadeNameForRGB(avgR, avgG, avgB, undertone)

            // Limit application steps to 5 concise steps
            val steps = profile.applicationSteps.take(5)

            val dynamicProfile = profile.copy(
                skinToneRGB = rgbString,
                confidenceScore = (profile.confidenceScore * (qualityScore / 100f)) + modelConfidence,
                foundation = profile.foundation.copy(brand = "", productName = "", hexColor = foundationHex, shade = "Skin Match", description = "A shade matched to your skin tone."),
                blush = profile.blush.copy(brand = "", productName = "", hexColor = blushHex, shade = "Warm Flush", description = "A soft warm flush for your cheeks."),
                eyeShadow = profile.eyeShadow.copy(brand = "", productName = "", hexColor = eyeHex, shade = "Neutral", description = "Neutral lid color for everyday depth."),
                eyeliner = profile.eyeliner.copy(brand = "", productName = "", hexColor = linerHex, shade = "Deep", description = "Soft dark liner for natural definition."),
                lipstick = profile.lipstick.copy(brand = "", productName = "", hexColor = lipHex, shade = "Rich", description = "A rich lip shade that complements your tone."),
                applicationSteps = steps
            )

            Log.d(TAG, "Generated recommendation for $detectedSkinTone with dynamic swatches. Confidence: ${dynamicProfile.confidenceScore}")

            MakeupAnalysisResult(
                imageQualityScore = qualityScore,
                profile = dynamicProfile,
                skinToneEstimate = detectedSkinTone,
                faceDetected = true,
                detectedColorHex = detectedHex,
                detectedColorNameEN = detectedNameEN,
                detectedColorNameKR = detectedNameKR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendation: ${e.message}", e)
            val fallback = makeupProfiles["Medium"]!!
            MakeupAnalysisResult(
                imageQualityScore = 0f,
                profile = fallback,
                skinToneEstimate = "Unknown",
                faceDetected = false
            )
        }
    }
}
