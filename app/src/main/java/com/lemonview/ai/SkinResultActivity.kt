package com.lemonview.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.view.View
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.lemonview.ai.model.SkinResult
import java.io.File

class SkinResultActivity : AppCompatActivity() {

    private lateinit var skinResult: SkinResult
    private var imagePath: String = ""
    
    /**
     * Cap severity at 90% maximum - no randomization, consistent values
     * If value is above 90, cap it at 90
     */
    private fun capSeverityAt90(severity: Int): Int {
        return severity.coerceIn(0, 90)
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
        android.util.Log.d("SkinResult", "=== onCreate STARTED ===")
        
        try {
            setContentView(R.layout.activity_skin_result)
            android.util.Log.d("SkinResult", "✓ Layout set successfully")
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ FATAL - Error setting layout: ${e.message}", e)
            Toast.makeText(this, "UI 로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Get all UI references FIRST
        android.util.Log.d("SkinResult", "Step 1: Finding UI elements...")
        val imgCaptured = findViewById<ImageView>(R.id.imgCaptured)
        val healthScoreContainer = findViewById<LinearLayout>(R.id.healthScoreContainer)
        val chartsContainer = findViewById<LinearLayout>(R.id.chartsContainer)
        val btnRoutinePlan = findViewById<Button>(R.id.btnRoutinePlan)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val diseaseListContainer = findViewById<LinearLayout>(R.id.diseaseListContainer)
        val personalReportContainer = findViewById<LinearLayout>(R.id.personalReportContainer)
        val primaryConditionsContainer = findViewById<LinearLayout>(R.id.primaryConditionsContainer)
        val professionalRecommendationsContainer = findViewById<LinearLayout>(R.id.professionalRecommendationsContainer)
        android.util.Log.d("SkinResult", "✓ All UI elements found")

        // Setup button listeners immediately
        try {
            btnHome.setOnClickListener {
                val homeIntent = Intent(this, MainMenuActivity::class.java)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(homeIntent)
                finish()
            }
            btnBack.setOnClickListener { finish() }
            btnRoutinePlan.setOnClickListener {
                // Set flag for fresh analysis before navigating
                RoutinePlannerActivity.isFreshAnalysis = true
                val routineIntent = Intent(this, RoutinePlannerActivity::class.java)
                startActivity(routineIntent)
            }
            android.util.Log.d("SkinResult", "✓ Button listeners set")
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error setting buttons: ${e.message}", e)
        }

        // Get skin result from intent
        android.util.Log.d("SkinResult", "Step 2: Getting skin result from intent...")
        try {
            val result = intent.getSerializableExtra("skin_result") as? SkinResult
            
            if (result == null) {
                android.util.Log.e("SkinResult", "✗ Skin result is NULL from intent!")
                Toast.makeText(this, "분석 결과를 받을 수 없습니다.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            skinResult = result
            android.util.Log.d("SkinResult", "✓ Skin result obtained: ${skinResult.skinHealthPercentage}% health, Tone: ${skinResult.skinTone}")
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error getting skin result: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "결과 데이터 오류: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Load and display captured image
        android.util.Log.d("SkinResult", "Step 3: Loading captured image...")
        try {
            intent.getStringExtra("image_path")?.let {
                imagePath = it
                val file = File(it)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        // Rotate bitmap to north/upright orientation based on EXIF data
                        val orientedBitmap = rotateBitmapToNorth(bitmap, file.absolutePath)
                        imgCaptured.setImageBitmap(orientedBitmap)
                        android.util.Log.d("SkinResult", "✓ Image displayed: ${orientedBitmap.width}x${orientedBitmap.height}")
                    } else {
                        android.util.Log.w("SkinResult", "⚠ Failed to decode bitmap")
                    }
                } else {
                    android.util.Log.w("SkinResult", "⚠ Image file not found")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error loading image: ${e.message}", e)
        }

        // Populate health score and basic info
        android.util.Log.d("SkinResult", "Step 4: Populating health score and basic info...")
        try {
            // Health Score - Beautiful Card Design
            healthScoreContainer.removeAllViews()
            val healthScore = skinResult.skinHealthPercentage.toInt()
            
            // Score number (large and centered)
            val scoreNumberView = TextView(this)
            scoreNumberView.text = "$healthScore"
            scoreNumberView.textSize = 56f
            scoreNumberView.setTextColor(Color.parseColor("#FFD700"))
            scoreNumberView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            scoreNumberView.gravity = android.view.Gravity.CENTER_HORIZONTAL
            scoreNumberView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            healthScoreContainer.addView(scoreNumberView)
            
            // Score label
            val labelView = TextView(this)
            labelView.text = "피부 건강도 (Health Score)"
            labelView.textSize = 12f
            labelView.setTextColor(Color.parseColor("#B0B0B0"))
            labelView.gravity = android.view.Gravity.CENTER_HORIZONTAL
            labelView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
            healthScoreContainer.addView(labelView)
            
            // Progress bar with animation
            val progressView = TextView(this)
            progressView.text = createHealthBar(healthScore)
            progressView.textSize = 13f
            progressView.setTextColor(Color.parseColor("#888888"))
            progressView.typeface = android.graphics.Typeface.MONOSPACE
            progressView.gravity = android.view.Gravity.CENTER_HORIZONTAL
            progressView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                topMargin = 12
                bottomMargin = 4
            }
            healthScoreContainer.addView(progressView)
            
            // Health status
            val statusText = when {
                healthScore >= 80 -> "✨ Excellent | 뛰어남"
                healthScore >= 60 -> "👍 Good | 좋음"
                healthScore >= 40 -> "⚠️ Fair | 보통"
                else -> "🔴 Poor | 나쁨"
            }
            
            val statusView = TextView(this)
            statusView.text = statusText
            statusView.textSize = 12f
            statusView.setTextColor(
                when {
                    healthScore >= 80 -> Color.parseColor("#00FF00")
                    healthScore >= 60 -> Color.parseColor("#FFD700")
                    healthScore >= 40 -> Color.parseColor("#FFA500")
                    else -> Color.parseColor("#FF6B6B")
                }
            )
            statusView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            statusView.gravity = android.view.Gravity.CENTER_HORIZONTAL
            statusView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            healthScoreContainer.addView(statusView)
            
            android.util.Log.d("SkinResult", "✓ Health score added: $healthScore%")
            
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error populating basic info: ${e.message}", e)
            e.printStackTrace()
        }

        // Categorize and populate diseases
        android.util.Log.d("SkinResult", "Step 5: Categorizing diseases...")
        try {
            val lessAffected = mutableListOf<Pair<String, Int>>()
            val mild = mutableListOf<Pair<String, Int>>()
            val severe = mutableListOf<Pair<String, Int>>()
            
            for ((disease, percentage) in skinResult.diseasesLevel) {
                // Apply 90% cap to all severity values
                val cappedPercentage = capSeverityAt90(percentage)
                when {
                    cappedPercentage <= 25 -> lessAffected.add(disease to cappedPercentage)
                    cappedPercentage <= 60 -> mild.add(disease to cappedPercentage)
                    else -> severe.add(disease to cappedPercentage)
                }
            }
            
            lessAffected.sortByDescending { it.second }
            mild.sortByDescending { it.second }
            severe.sortByDescending { it.second }
            
            android.util.Log.d("SkinResult", "✓ Diseases categorized - Severe: ${severe.size}, Mild: ${mild.size}, Less: ${lessAffected.size}")
            
            // Populate disease list
            diseaseListContainer.removeAllViews()
            populateCategorizedDiseaseList(diseaseListContainer, lessAffected, mild, severe)
            android.util.Log.d("SkinResult", "✓ Disease list populated")
            
            // Create and setup chart
            if (mild.isNotEmpty() || severe.isNotEmpty()) {
                try {
                    chartsContainer.removeAllViews()
                    
                    val chartTitle = TextView(this)
                    chartTitle.text = "【상세 피부 상태 분석】"
                    chartTitle.textSize = 16f
                    chartTitle.setTextColor(Color.parseColor("#FFD400"))
                    chartTitle.typeface = android.graphics.Typeface.DEFAULT_BOLD
                    chartTitle.setPadding(0, 16, 0, 12)
                    chartsContainer.addView(chartTitle)
                    
                    val barChart = BarChart(this)
                    val barChartParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        520
                    )
                    barChartParams.setMargins(16, 12, 16, 16)
                    barChart.layoutParams = barChartParams
                    chartsContainer.addView(barChart)
                    
                    val barEntries = ArrayList<BarEntry>()
                    val xLabels = ArrayList<String>()
                    val colors = ArrayList<Int>()
                    
                    var xIndex = 0f
                    
                    // Add severe
                    for ((index, pair) in severe.take(4).withIndex()) {
                        barEntries.add(BarEntry(xIndex, pair.second.toFloat()))
                        xLabels.add(pair.first)
                        colors.add(when(index) {
                            0 -> Color.parseColor("#FF4444")
                            1 -> Color.parseColor("#FF6B6B")
                            2 -> Color.parseColor("#FF8888")
                            else -> Color.parseColor("#FF9999")
                        })
                        xIndex++
                    }
                    
                    // Add mild
                    for ((index, pair) in mild.take(4).withIndex()) {
                        barEntries.add(BarEntry(xIndex, pair.second.toFloat()))
                        xLabels.add(pair.first)
                        colors.add(when(index) {
                            0 -> Color.parseColor("#FF9500")
                            1 -> Color.parseColor("#FFA500")
                            2 -> Color.parseColor("#FFB84D")
                            else -> Color.parseColor("#FFC66D")
                        })
                        xIndex++
                    }
                    
                    setupBarChart(barChart, barEntries, colors, xLabels)
                    android.util.Log.d("SkinResult", "✓ Chart setup complete")
                } catch (chartError: Exception) {
                    android.util.Log.e("SkinResult", "✗ Chart error: ${chartError.message}", chartError)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error with diseases/chart: ${e.message}", e)
            e.printStackTrace()
        }

        // Populate new report sections
        try {
            android.util.Log.d("SkinResult", "Step 6: Populating detailed report sections...")
            
            // Personal Skin Analysis Report
            personalReportContainer.removeAllViews()
            val reportView = TextView(this)
            reportView.text = skinResult.skinAnalysisExplanation
            reportView.textSize = 12f
            reportView.setTextColor(Color.parseColor("#FFFFFF"))
            reportView.setLineSpacing(2f, 1.5f)
            personalReportContainer.addView(reportView)
            
            // 68-Point Facial Landmark Analysis
            // facialLandmarkContainer.removeAllViews()
            val landmarkView = TextView(this)
            landmarkView.text = "✓ 68-point facial landmark detection system activated\n" +
                    "• Forehead: Detected & Analyzed\n" +
                    "• Cheeks: Mapped and Assessed\n" +
                    "• Nose Bridge: Scanned\n" +
                    "• Chin: Evaluated\n" +
                    "• Eye Region: Detailed Analysis\n" +
                    "• Overall Symmetry: ${skinResult.overallConfidence.times(100).toInt()}% Confidence"
            landmarkView.textSize = 12f
            landmarkView.setTextColor(Color.parseColor("#FFFFFF"))
            landmarkView.setLineSpacing(2f, 1.4f)
            // facialLandmarkContainer.addView(landmarkView)
            
            // Primary Skin Conditions (top 5)
            primaryConditionsContainer.removeAllViews()
            val topDiseases = skinResult.diseasesLevel.entries
                .map { it.key to capSeverityAt90(it.value) }
                .sortedByDescending { it.second }
                .take(5)
            
            if (topDiseases.isNotEmpty()) {
                val conditionsText = StringBuilder()
                for ((index, pair) in topDiseases.withIndex()) {
                    val confidence = skinResult.diseaseDetectionConfidence[pair.first]?.times(100)?.toInt() ?: 85
                    conditionsText.append("${index + 1}. ${pair.first}\n")
                    conditionsText.append("   Severity: ${pair.second}% | Confidence: $confidence%\n")
                    if (index < topDiseases.size - 1) {
                        conditionsText.append("\n")
                    }
                }
                
                val conditionsView = TextView(this)
                conditionsView.text = conditionsText.toString()
                conditionsView.textSize = 12f
                conditionsView.setTextColor(Color.parseColor("#FFFFFF"))
                conditionsView.setLineSpacing(2f, 1.4f)
                primaryConditionsContainer.addView(conditionsView)
            }
            
            // Professional Recommendations
            professionalRecommendationsContainer.removeAllViews()
            if (skinResult.recommendations.isNotEmpty()) {
                for ((index, recommendation) in skinResult.recommendations.withIndex()) {
                    val recView = TextView(this)
                    recView.text = "${index + 1}. $recommendation"
                    recView.textSize = 12f
                    recView.setTextColor(Color.parseColor("#FFFFFF"))
                    recView.setPadding(0, 8, 0, 8)
                    professionalRecommendationsContainer.addView(recView)
                    
                    if (index < skinResult.recommendations.size - 1) {
                        val separator = View(this)
                        val separatorParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                        )
                        separatorParams.setMargins(0, 6, 0, 6)
                        separator.layoutParams = separatorParams
                        separator.setBackgroundColor(Color.parseColor("#333333"))
                        professionalRecommendationsContainer.addView(separator)
                    }
                }
            }
            
            android.util.Log.d("SkinResult", "✓ All report sections populated")
        } catch (e: Exception) {
            android.util.Log.e("SkinResult", "✗ Error populating report sections: ${e.message}", e)
            e.printStackTrace()
        }
        
        android.util.Log.d("SkinResult", "=== onCreate COMPLETED SUCCESSFULLY ===")
    }

    /**
     * Populate disease list divided into three categories (Severe, Mild, Less Affected)
     */
    private fun populateCategorizedDiseaseList(
        container: LinearLayout,
        lessAffected: List<Pair<String, Int>>,
        mild: List<Pair<String, Int>>,
        severe: List<Pair<String, Int>>
    ) {
        container.removeAllViews()
        var itemCount = 1
        
        // SEVERE SECTION (shown first)
        if (severe.isNotEmpty()) {
            addCategoryHeader(container, "🔴 심각함 (Severe)", Color.parseColor("#FF6B6B"))
            for ((index, item) in severe.withIndex()) {
                val diseaseRow = createDiseaseRow(item.first, item.second, itemCount, Color.parseColor("#FF6B6B"))
                container.addView(diseaseRow)
                itemCount++
                if (index < severe.size - 1) {
                    addSeparator(container)
                }
            }
            addCategoryDivider(container)
        }
        
        // MILD SECTION
        if (mild.isNotEmpty()) {
            addCategoryHeader(container, "🟡 주의 필요 (Mild)", Color.parseColor("#FFA500"))
            for ((index, item) in mild.withIndex()) {
                val diseaseRow = createDiseaseRow(item.first, item.second, itemCount, Color.parseColor("#FFA500"))
                container.addView(diseaseRow)
                itemCount++
                if (index < mild.size - 1) {
                    addSeparator(container)
                }
            }
            addCategoryDivider(container)
        }
        
        // LESS AFFECTED SECTION
        if (lessAffected.isNotEmpty()) {
            addCategoryHeader(container, "🟢 좋은 상태 (Good)", Color.parseColor("#00FF00"))
            for ((index, item) in lessAffected.withIndex()) {
                val diseaseRow = createDiseaseRow(item.first, item.second, itemCount, Color.parseColor("#00FF00"))
                container.addView(diseaseRow)
                itemCount++
                if (index < lessAffected.size - 1) {
                    addSeparator(container)
                }
            }
        }
    }
    
    /**
     * Add category header with emoji and beautiful styling
     */
    private fun addCategoryHeader(container: LinearLayout, title: String, color: Int) {
        val header = TextView(this)
        header.text = title
        header.textSize = 15f
        header.setTextColor(color)
        header.typeface = android.graphics.Typeface.DEFAULT_BOLD
        header.setPadding(16, 18, 16, 14)
        header.letterSpacing = 0.05f
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        header.layoutParams = params
        
        // Add subtle background
        header.setBackgroundColor(Color.parseColor("#1A1A1A"))
        container.addView(header)
    }
    
    /**
     * Add separator between items with refined styling
     */
    private fun addSeparator(container: LinearLayout) {
        val separator = View(this)
        val separatorParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separatorParams.setMargins(70, 8, 16, 8)  // Align with number badge
        separator.layoutParams = separatorParams
        separator.setBackgroundColor(Color.parseColor("#333333"))
        container.addView(separator)
    }
    
    /**
     * Add thick elegant divider between categories
     */
    private fun addCategoryDivider(container: LinearLayout) {
        val divider = View(this)
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        dividerParams.setMargins(0, 16, 0, 16)
        divider.layoutParams = dividerParams
        divider.setBackgroundColor(Color.parseColor("#FFD70040"))
        container.addView(divider)
    }

    /**
     * Populate disease list with professional card-style rows
     */
    private fun populateDiseaseList(container: LinearLayout, diseaseData: Map<String, Int>) {
        container.removeAllViews()
        
        // Sort diseases by percentage (descending)
        val sortedDiseases = diseaseData.entries.sortedByDescending { it.value }
        
        for ((index, entry) in sortedDiseases.withIndex()) {
            val disease = entry.key
            val percentage = entry.value
            
            // Create disease row
            val diseaseRow = createDiseaseRow(disease, percentage, index)
            container.addView(diseaseRow)
            
            // Add separator between rows
            if (index < sortedDiseases.size - 1) {
                val separator = View(this)
                val separatorParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                separator.layoutParams = separatorParams
                separator.setBackgroundColor(Color.parseColor("#FFD400"))
                container.addView(separator)
            }
        }
    }
    
    /**
     * Create premium disease row card with gradient badge
     */
    private fun createDiseaseRow(disease: String, percentage: Int, index: Int, color: Int = Color.parseColor("#FFD400")): android.view.View {
        // Cap severity at 90% for display
        val displayPercentage = capSeverityAt90(percentage)
        // Main container with padding
        val container = LinearLayout(this)
        val containerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.layoutParams = containerParams
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(8, 10, 8, 10)
        
        // Row layout
        val rowLayout = LinearLayout(this)
        rowLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rowLayout.orientation = LinearLayout.HORIZONTAL
        rowLayout.setPadding(8, 12, 12, 12)
        rowLayout.setBackgroundColor(Color.parseColor("#1A1A1A"))
        
        // Severity color badge (circular)
        val badgeView = TextView(this)
        badgeView.text = (index + 1).toString()
        badgeView.textSize = 12f
        badgeView.setTextColor(Color.BLACK)
        badgeView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        badgeView.setBackgroundColor(color)
        badgeView.gravity = android.view.Gravity.CENTER
        badgeView.setPadding(12, 8, 12, 8)
        
        val badgeParams = LinearLayout.LayoutParams(
            48,
            48
        ).apply { 
            rightMargin = 12
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        badgeView.layoutParams = badgeParams
        rowLayout.addView(badgeView)
        
        // Main info container
        val infoContainer = LinearLayout(this)
        infoContainer.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        infoContainer.orientation = LinearLayout.VERTICAL
        infoContainer.setPadding(4, 0, 0, 0)
        
        // Disease name with bold styling
        val nameView = TextView(this)
        nameView.text = disease
        nameView.textSize = 14f
        nameView.setTextColor(Color.parseColor("#FFFFFF"))
        nameView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        nameView.letterSpacing = 0.02f
        infoContainer.addView(nameView)
        
        // Status and percentage row
        val statusRow = LinearLayout(this)
        statusRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        statusRow.orientation = LinearLayout.HORIZONTAL
        statusRow.setPadding(0, 6, 0, 0)
        
        // Percentage text
        val percentView = TextView(this)
        percentView.text = "$displayPercentage%"
        percentView.textSize = 13f
        percentView.setTextColor(color)
        percentView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        percentView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        statusRow.addView(percentView)
        
        // Status badge
        val levelText = when {
            displayPercentage >= 70 -> "🔴 심각"
            displayPercentage >= 40 -> "🟡 주의"
            else -> "🟢 양호"
        }
        
        val levelView = TextView(this)
        levelView.text = levelText
        levelView.textSize = 11f
        levelView.setTextColor(Color.parseColor("#FFFFFF"))
        levelView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { leftMargin = 12 }
        statusRow.addView(levelView)
        
        infoContainer.addView(statusRow)
        rowLayout.addView(infoContainer)
        
        container.addView(rowLayout)
        return container
    }

    /**
     * Setup beautiful Bar chart with gradient colors
     */
    private fun setupBarChart(
        chart: BarChart,
        entries: List<BarEntry>,
        colors: List<Int>,
        xLabels: List<String>
    ) {
        val dataSet = BarDataSet(entries, "Skin Conditions")
        dataSet.colors = colors
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 11f
        dataSet.valueTextColor = Color.WHITE
        dataSet.barBorderWidth = 1.5f
        dataSet.barBorderColor = Color.parseColor("#222222")
        
        val data = BarData(dataSet)
        data.barWidth = 0.7f
        
        chart.data = data
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textColor = Color.WHITE
        chart.legend.textSize = 12f
        chart.legend.setDrawInside(false)
        chart.legend.xOffset = 10f
        chart.legend.yOffset = 10f
        
        // X-axis configuration
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        chart.xAxis.textColor = Color.WHITE
        chart.xAxis.textSize = 11f
        chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        chart.xAxis.labelRotationAngle = -45f  // Diagonal labels for better readability
        chart.xAxis.granularity = 1f
        chart.xAxis.isGranularityEnabled = true
        
        // Y-axis configuration
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f
        chart.axisLeft.textColor = Color.parseColor("#888888")
        chart.axisLeft.textSize = 11f
        chart.axisLeft.gridColor = Color.parseColor("#444444")
        chart.axisLeft.gridLineWidth = 0.5f
        
        // Disable right axis
        chart.axisRight.isEnabled = false
        
        // General styling
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.setDoubleTapToZoomEnabled(true)
        chart.animateY(1500)  // Smooth animation
        chart.invalidate()
    }

    private fun createHealthBar(percentage: Int): String {
        val filled = percentage / 10
        val empty = 10 - filled
        val bar = "█".repeat(filled) + "░".repeat(empty)
        return "[$bar] $percentage%"
    }

    /**
     * Calculate image quality as a percentile score based on resolution
     * Standard good resolution for selfie: ~2000x2000 pixels = 4MP
     * Excellent resolution: ~3000x3000 pixels = 9MP
     * Percentile scale: 0-100% based on pixel count
     */
    private fun calculateImageQualityPercentile(bitmap: android.graphics.Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        
        // Calculate percentile (based on common smartphone camera quality)
        // Low: < 1MP (30th percentile)
        // Medium: 1-2MP (50th percentile)
        // Good: 2-4MP (70th percentile)
        // Excellent: 4-8MP (85th percentile)
        // Premium: 8MP+ (95th percentile)
        
        return when {
            totalPixels < 500000 -> 25        // Very low
            totalPixels < 1000000 -> 40       // Low
            totalPixels < 2000000 -> 55       // Medium-low
            totalPixels < 3000000 -> 70       // Medium-good (THRESHOLD)
            totalPixels < 5000000 -> 80       // Good
            totalPixels < 8000000 -> 90       // Excellent
            else -> 95                        // Premium
        }
    }

    /**
     * Display confidence and accuracy metrics for the analysis
     */
    private fun displayConfidenceMetrics(skinResult: SkinResult) {
        try {
            // Create scrollable container for metrics
            val metricsScroll = findViewById<LinearLayout>(R.id.diseaseListContainer)?.parent as? LinearLayout
            if (metricsScroll == null) return
            
            // Overall Confidence Section
            val confidenceSection = LinearLayout(this)
            confidenceSection.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            confidenceSection.orientation = LinearLayout.VERTICAL
            confidenceSection.setBackgroundColor(Color.parseColor("#1A1A1A"))
            confidenceSection.setPadding(16, 12, 16, 12)
            
            // Title
            val titleView = TextView(this)
            titleView.text = "【분석 신뢰도】(Analysis Confidence)"
            titleView.textSize = 14f
            titleView.setTextColor(Color.parseColor("#FFD400"))
            titleView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            titleView.setPadding(0, 0, 0, 8)
            confidenceSection.addView(titleView)
            
            // Overall Confidence Score
            val overallConfidencePercent = (skinResult.overallConfidence * 100).toInt()
            val overallView = TextView(this)
            overallView.text = "📊 전체 신뢰도: $overallConfidencePercent%\n" +
                "Overall Confidence: $overallConfidencePercent%"
            overallView.textSize = 12f
            overallView.setTextColor(Color.WHITE)
            overallView.setPadding(0, 4, 0, 4)
            confidenceSection.addView(overallView)
            
            // Confidence Indicator Bar
            val confidenceBar = createConfidenceBar(skinResult.overallConfidence)
            confidenceSection.addView(confidenceBar)
            
            // Image Quality Score
            val imageQualityPercent = (skinResult.imageQualityScore * 100).toInt()
            val imageQualityView = TextView(this)
            imageQualityView.text = "🖼️ 이미지 품질: $imageQualityPercent%\nImage Quality: $imageQualityPercent%"
            imageQualityView.textSize = 11f
            imageQualityView.setTextColor(Color.parseColor("#C0C0C0"))
            imageQualityView.setPadding(0, 8, 0, 4)
            confidenceSection.addView(imageQualityView)
            
            // Analysis Quality Rating
            val qualityView = TextView(this)
            val qualityEmoji = when (skinResult.analysisQuality) {
                "EXCELLENT" -> "⭐⭐⭐⭐⭐"
                "GOOD" -> "⭐⭐⭐⭐"
                "FAIR" -> "⭐⭐⭐"
                else -> "⭐⭐"
            }
            qualityView.text = "평가: $qualityEmoji ${skinResult.analysisQuality}\nRating: $qualityEmoji ${skinResult.analysisQuality}"
            qualityView.textSize = 11f
            qualityView.setTextColor(Color.parseColor("#FFD400"))
            qualityView.setPadding(0, 4, 0, 8)
            confidenceSection.addView(qualityView)
            
            // Confidence Recommendation
            val warningText = com.lemonview.ai.utils.ConfidenceMetrics.generateConfidenceWarning(
                skinResult.overallConfidence
            )
            val warningView = TextView(this)
            warningView.text = warningText
            warningView.textSize = 10f
            warningView.setTextColor(Color.parseColor("#FFB6C6"))
            warningView.gravity = android.view.Gravity.CENTER
            warningView.setPadding(8, 8, 8, 8)
            confidenceSection.addView(warningView)
            
            // Per-Disease Confidence (if available)
            if (skinResult.diseaseDetectionConfidence.isNotEmpty()) {
                val diseaseConfidenceTitle = TextView(this)
                diseaseConfidenceTitle.text = "【질환별 신뢰도】(Disease-Specific Confidence)"
                diseaseConfidenceTitle.textSize = 12f
                diseaseConfidenceTitle.setTextColor(Color.parseColor("#FFD400"))
                diseaseConfidenceTitle.typeface = android.graphics.Typeface.DEFAULT_BOLD
                diseaseConfidenceTitle.setPadding(0, 12, 0, 8)
                confidenceSection.addView(diseaseConfidenceTitle)
                
                for ((disease, confidence) in skinResult.diseaseDetectionConfidence.toList().take(5)) {
                    val diseaseView = createDiseaseConfidenceRow(disease, confidence)
                    confidenceSection.addView(diseaseView)
                }
            }
            
            // Model Info
            val modelInfoView = TextView(this)
            modelInfoView.text = "📱 Model: ${skinResult.modelVersions}\n분석시간: ${skinResult.analysisTimeMs}ms"
            modelInfoView.textSize = 9f
            modelInfoView.setTextColor(Color.parseColor("#808080"))
            modelInfoView.setPadding(0, 8, 0, 0)
            confidenceSection.addView(modelInfoView)
            
            // Add to parent - find the scroll view and add after disease list
            val scrollView = findViewById<android.widget.ScrollView>(android.R.id.content)
            (scrollView?.getChildAt(0) as? LinearLayout)?.addView(confidenceSection)
            
        } catch (e: Exception) {
            android.util.Log.e("SkinResultActivity", "Error displaying confidence metrics: ${e.message}")
        }
    }
    
    /**
     * Create a visual confidence bar
     */
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
    
    /**
     * Create disease-specific confidence row
     */
    private fun createDiseaseConfidenceRow(disease: String, confidence: Float): View {
        val row = LinearLayout(this)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(8, 4, 8, 4)
        
        // Disease name
        val nameView = TextView(this)
        nameView.text = disease
        nameView.textSize = 10f
        nameView.setTextColor(Color.WHITE)
        nameView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.4f
        )
        row.addView(nameView)
        
        // Confidence percentage
        val percentView = TextView(this)
        val confidencePercent = (confidence * 100).toInt()
        percentView.text = "$confidencePercent%"
        percentView.textSize = 10f
        percentView.setTextColor(Color.parseColor("#FFD400"))
        percentView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        row.addView(percentView)
        
        // Confidence level description
        val levelView = TextView(this)
        levelView.text = com.lemonview.ai.utils.ConfidenceMetrics.getConfidenceDescription(confidence)
        levelView.textSize = 9f
        levelView.setTextColor(Color.parseColor("#CCCCCC"))
        levelView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.4f
        )
        row.addView(levelView)
        
        return row
    }
}

