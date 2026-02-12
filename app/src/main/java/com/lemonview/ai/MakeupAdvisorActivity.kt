package com.lemonview.ai

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lemonview.ai.utils.MakeupColorAnalyzer
import com.lemonview.ai.utils.SkinDataStore


class MakeupAdvisorActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var paletteList: LinearLayout
    private lateinit var tvExplanation: TextView

    companion object {
        private const val TAG = "MakeupAdvisor"
        const val EXTRA_IMAGE_PATH = "image_path"
    }

    /**
     * Get the rotation degrees needed to orient the image to north/upright
     */
    private fun getImageRotationDegrees(filePath: String): Float {
        return try {
            val exif = ExifInterface(filePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Rotate bitmap to north/upright orientation
     */
    private fun rotateBitmapToNorth(bitmap: Bitmap, filePath: String): Bitmap {
        val rotationDegrees = getImageRotationDegrees(filePath)
        return if (rotationDegrees != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_makeup_result)

        try {
            btnBack = findViewById(R.id.btnBack)
            paletteList = findViewById(R.id.paletteList)
            // Use runtime lookup for tvExplanation id to avoid R resolution issues
            val tvExplanationId = resources.getIdentifier("tvExplanation", "id", packageName)
            tvExplanation = if (tvExplanationId != 0) {
                findViewById(tvExplanationId)
            } else {
                TextView(this).also { it.text = "설명 없음" }
            }

            val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
            if (!imagePath.isNullOrEmpty()) {
                analyzeAndDisplayMakeup(imagePath)
            } else {
                val sampleBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
                if (sampleBitmap != null) {
                    // Rotate bitmap to vertical if it's horizontal
                    val rotatedBitmap = if (sampleBitmap.width > sampleBitmap.height) {
                        val matrix = Matrix()
                        matrix.postRotate(90f)
                        Bitmap.createBitmap(sampleBitmap, 0, 0, sampleBitmap.width, sampleBitmap.height, matrix, true)
                    } else {
                        sampleBitmap
                    }
                    val result = MakeupColorAnalyzer.getMakeupRecommendation(this, rotatedBitmap)
                    // Display captured/sample image
                    val imgView = findViewById<ImageView>(R.id.imgCaptured)
                    imgView.setImageBitmap(rotatedBitmap)

                    SkinDataStore(this).saveMakeupResult(result)
                    displayMakeupResults(result)
                } else {
                    Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            val btnHome = findViewById<ImageView>(R.id.btnHome)
            try {
                btnHome.setOnClickListener {
                    val homeIntent = Intent(this, MainMenuActivity::class.java)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(homeIntent)
                    finish()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Home button not found or error attaching listener: ${e.message}")
            }

            btnBack.setOnClickListener { finish() }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // initializeViews removed (no longer needed)

    private fun analyzeAndDisplayMakeup(imagePath: String) {
        try {
            Log.d(TAG, "Analyzing image from: $imagePath")
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                return
            }
            // Rotate bitmap to north/upright orientation based on EXIF data
            val orientedBitmap = rotateBitmapToNorth(bitmap, imagePath)
            val result = MakeupColorAnalyzer.getMakeupRecommendation(this, orientedBitmap)
            // Display captured image
            val imgView = findViewById<ImageView>(R.id.imgCaptured)
            imgView.setImageBitmap(orientedBitmap)

            SkinDataStore(this).saveMakeupResult(result)
            displayMakeupResults(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            Toast.makeText(this, "Analysis error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayMakeupResults(result: com.lemonview.ai.model.MakeupAnalysisResult) {
        try {
            val profile = result.profile
            paletteList.removeAllViews()
            val inflater = LayoutInflater.from(this)
            val paletteItems = listOf(
                Triple(profile.foundation.hexColor, "Foundation", profile.foundation.shade),
                Triple(profile.blush.hexColor, "Blush", profile.blush.shade),
                Triple(profile.eyeShadow.hexColor, "Eye Shadow", profile.eyeShadow.shade),
                Triple(profile.eyeliner.hexColor, "Eyeliner", profile.eyeliner.shade),
                Triple(profile.lipstick.hexColor, "Lipstick", profile.lipstick.shade)
            )
            for ((colorHex, label, shadeName) in paletteItems) {
                val itemView = inflater.inflate(R.layout.item_color_palette, paletteList, false)
                val swatch = itemView.findViewById<View>(R.id.colorSwatch)
                val tvColorHeader = itemView.findViewById<TextView>(R.id.tvColorHeader)
                val tvColorValue = itemView.findViewById<TextView>(R.id.tvColorValue)
                try {
                    swatch.background.setTint(Color.parseColor(colorHex))
                } catch (_: Exception) {
                    swatch.setBackgroundColor(Color.LTGRAY)
                }
                tvColorHeader.text = translateLabelToKorean(label)
                tvColorValue.text = translateShadeToKorean(shadeName)
                paletteList.addView(itemView)
            }
            // Set detected color swatch and name (if available)
            val swatchView = findViewById<View>(R.id.detectedColorSwatch)
            val tvDetectedColorName = findViewById<TextView>(R.id.tvDetectedColorName)
            try {
                swatchView.background.setTint(Color.parseColor(result.detectedColorHex ?: profile.foundation.hexColor))
            } catch (_: Exception) {
                swatchView.setBackgroundColor(Color.LTGRAY)
            }
            val detectedName = if (!result.detectedColorNameEN.isNullOrEmpty()) result.detectedColorNameEN else profile.skinTone
            tvDetectedColorName.text = detectedName

            // Makeup match confidence section
            try {
                val scoreContainer = findViewById<LinearLayout>(R.id.makeupScoreContainer)
                scoreContainer.removeAllViews()

                val titleView = TextView(this)
                titleView.text = "전체 매칭 신뢰도: ${(profile.confidenceScore * 100).toInt()}%"
                titleView.textSize = 13f
                titleView.setTextColor(Color.WHITE)
                titleView.setPadding(0,0,0,6)
                scoreContainer.addView(titleView)

                // Add confidence bar
                val bar = createConfidenceBar(profile.confidenceScore)
                scoreContainer.addView(bar)

                // Image quality
                val imgQuality = TextView(this)
                imgQuality.text = "이미지 품질: ${result.imageQualityScore.toInt()}"
                imgQuality.textSize = 11f
                imgQuality.setTextColor(Color.parseColor("#C0C0C0"))
                imgQuality.setPadding(0,8,0,0)
                scoreContainer.addView(imgQuality)

            } catch (_: Exception) {
                // ignore
            }

            // Application steps (max 5)
            val stepsContainer = findViewById<LinearLayout>(R.id.applicationStepsList)
            stepsContainer.removeAllViews()
            for (step in profile.applicationSteps.take(5)) {
                val t = TextView(this)
                t.text = translateStepToKorean(step)
                t.setTextColor(Color.WHITE)
                t.textSize = 14f
                t.setPadding(0,6,0,6)
                stepsContainer.addView(t)
            }

            // Set explanation (single section at end) - translated
            tvExplanation.text = translateExplanationToKorean(profile.overallDescription)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying makeup results", e)
            Toast.makeText(this, "Display error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Create a visual confidence bar (copied style from SkinResultActivity)
    private fun createConfidenceBar(confidence: Float): View {
        val barContainer = LinearLayout(this)
        barContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            40
        )
        barContainer.orientation = LinearLayout.HORIZONTAL
        barContainer.setPadding(0, 4, 0, 8)

        val filledPercent = (confidence * 100).toInt()
        val emptyPercent = 100 - filledPercent

        val filledBar = View(this)
        filledBar.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            filledPercent.toFloat()
        )
        filledBar.setBackgroundColor(when {
            confidence >= 0.85f -> Color.parseColor("#4CAF50") // Green
            confidence >= 0.70f -> Color.parseColor("#8BC34A") // Light green
            confidence >= 0.55f -> Color.parseColor("#FFC107") // Amber
            else -> Color.parseColor("#F44336")               // Red
        })
        barContainer.addView(filledBar)

        val emptyBar = View(this)
        emptyBar.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            emptyPercent.toFloat()
        )
        emptyBar.setBackgroundColor(Color.parseColor("#333333"))
        barContainer.addView(emptyBar)

        return barContainer
    }

    // Simple translation helpers - best-effort, use a mapping for common labels/values
    private fun translateLabelToKorean(label: String): String {
        return when (label.toLowerCase()) {
            "foundation" -> "파운데이션 (Foundation)"
            "blush" -> "블러셔 (Blush)"
            "eye shadow" -> "아이섀도우 (Eye Shadow)"
            "eyeshadow" -> "아이섀도우 (Eye Shadow)"
            "eyeliner" -> "아이라이너 (Eyeliner)"
            "lipstick" -> "립스틱 (Lipstick)"
            else -> "${label} (${label})"
        }
    }

    private fun translateShadeToKorean(shade: String?): String {
        if (shade.isNullOrEmpty()) return "정보 없음"
        val nonNullShade = shade!!
        val s = nonNullShade.toLowerCase()
        val map = mapOf(
            "skin match" to "스킨 매치",
            "warm flush" to "웜 플러시",
            "neutral" to "뉴트럴",
            "deep" to "딥",
            "rich" to "리치",
            "cool" to "쿨",
            "light" to "라이트",
            "medium" to "미디엄",
            "dark" to "다크"
        )
        // Try exact map then fallback to word-level conversion
        map[s]?.let { return it }
        var translated = nonNullShade
        for ((k, v) in map) {
            translated = translated.replace(Regex(k, RegexOption.IGNORE_CASE), v)
        }
        return translated
    }

    private fun translateStepToKorean(step: String): String {
        var t = step
        val replacements = listOf(
            "apply" to "바르세요",
            "foundation" to "파운데이션",
            "blush" to "블러셔",
            "eye shadow" to "아이섀도우",
            "eyeshadow" to "아이섀도우",
            "eyeliner" to "아이라이너",
            "lipstick" to "립스틱",
            "blend" to "블렌딩하세요",
            "evenly" to "고르게",
            "use" to "사용하세요"
        )
        for ((k, v) in replacements) {
            t = t.replace(Regex(k, RegexOption.IGNORE_CASE), v)
        }
        return t
    }

    private fun translateExplanationToKorean(text: String?): String {
        if (text.isNullOrEmpty()) return "설명 없음"
        // Normalize and translate common phrases
        var t = text!!.lowercase()
        val replacements = listOf(
            "based on your skin tone" to "피부 톤을 기반으로",
            "based on skin tone" to "피부 톤을 기반으로",
            "recommended" to "추천",
            "recommend" to "추천",
            "use" to "사용",
            "apply" to "바르세요",
            "blend" to "블렌딩하세요",
            "coverage" to "커버력",
            "finish" to "마무리",
            "tone" to "톤",
            "texture" to "텍스처",
            "hydration" to "수분",
            "mattifying" to "매트",
            "dewy" to "광택",
            "match" to "매치",
            "suitable" to "적합",
            "for" to "~에"
        )
        for ((k, v) in replacements) {
            t = t.replace(Regex(k, RegexOption.IGNORE_CASE), v)
        }
        // Remove any remaining English letters, digits, and punctuation that might remain
        t = t.replace(Regex("[A-Za-z0-9@#\\$%&=_<>\\^/|*\\[\\](){}\\-\\.,:;\"'?!]"), " ")
        // Collapse multiple spaces and trim
        t = t.replace(Regex("\\s+"), " ").trim()
        if (t.isEmpty()) return "설명 없음"
        // Capitalization is not required for Korean; return result
        return t
    }

}
