package com.lemonview.ai

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.lemonview.ai.utils.RoutinePlanGenerator
import com.lemonview.ai.utils.SkinDataStore
import com.lemonview.ai.model.RoutinePlan14Days
import com.lemonview.ai.model.SkinResult
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import java.util.ArrayList

class RoutinePlannerActivity : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private lateinit var save: Button
    private lateinit var back: ImageView

    companion object {
        var isFreshAnalysis = false  // Flag to track if analysis was just completed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_planner)
        container = findViewById(R.id.routineContainer)
        save = findViewById(R.id.btnSavePdf)
        back = findViewById(R.id.btnBack)
        save.setOnClickListener { share() }
        back.setOnClickListener {
            isFreshAnalysis = false  // Clear flag when going back
            finish()
        }
        show()
    }

    override fun onResume() {
        super.onResume()
        show()
    }

    private fun show() {
        container.removeAllViews()
        val store = SkinDataStore(this)
        val skin = store.getLastSkinResult()
        val savedPlan = store.getLastRoutinePlan()

        if (skin == null) {
            // Show notification card when no skin analysis ever done
            showNoAnalysisNotification()
            return
        }

        // If we have a saved routine plan, show it (whether fresh analysis or not)
        if (savedPlan != null) {
            displaySavedRoutinePlan(savedPlan, skin)
            return
        }

        // If no saved plan but we have skin data and it's a fresh analysis, generate new plan
        if (isFreshAnalysis) {
            val gen = RoutinePlanGenerator()
            val plan = gen.generateRoutinePlan(skin)
            store.saveRoutinePlan(plan)
            displaySavedRoutinePlan(plan, skin)
            return
        }

        // If we have skin data but no saved plan and not fresh analysis, show notification to re-analyze
        showReanalyzeNotification()
    }

    private fun showNoAnalysisNotification() {
        // Show message to analyze skin before showing routine planner
        val emptyCard = LinearLayout(this)
        emptyCard.orientation = LinearLayout.VERTICAL
        emptyCard.setBackgroundResource(R.drawable.card_bg_rounded)
        emptyCard.setPadding(24, 24, 24, 24)
        val lpEmpty = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lpEmpty.topMargin = 60
        lpEmpty.leftMargin = 14
        lpEmpty.rightMargin = 14
        emptyCard.layoutParams = lpEmpty

        val title = TextView(this)
        title.text = "Please Analyze your skin for Routine planner"
        title.setTextColor(Color.parseColor("#FFD400"))
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setGravity(android.view.Gravity.CENTER)
        title.setPadding(0, 0, 0, 12)
        emptyCard.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "Complete skin analysis to get your personalized skincare routine"
        subtitle.setTextColor(Color.WHITE)
        subtitle.textSize = 14f
        subtitle.setGravity(android.view.Gravity.CENTER)
        subtitle.setLineSpacing(2f, 1.3f)
        emptyCard.addView(subtitle)

        container.addView(emptyCard)
    }

    private fun showReanalyzeNotification() {
        val notificationCard = LinearLayout(this)
        notificationCard.orientation = LinearLayout.VERTICAL
        notificationCard.setBackgroundResource(R.drawable.card_bg_rounded)
        notificationCard.setPadding(24, 24, 24, 24)
        val lpNotif = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lpNotif.topMargin = 60
        lpNotif.leftMargin = 14
        lpNotif.rightMargin = 14
        notificationCard.layoutParams = lpNotif

        val emoji = TextView(this)
        emoji.text = "🔄"
        emoji.textSize = 48f
        emoji.setGravity(android.view.Gravity.CENTER)
        emoji.setPadding(0, 0, 0, 16)
        notificationCard.addView(emoji)

        val title = TextView(this)
        title.text = "새로운 분석이 필요합니다"
        title.setTextColor(Color.parseColor("#FFD400"))
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setGravity(android.view.Gravity.CENTER)
        title.setPadding(0, 0, 0, 12)
        notificationCard.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "최신 피부 상태에 맞는 루틴을 받으려면 다시 분석해주세요"
        subtitle.setTextColor(Color.WHITE)
        subtitle.textSize = 14f
        subtitle.setGravity(android.view.Gravity.CENTER)
        subtitle.setLineSpacing(2f, 1.3f)
        subtitle.setPadding(0, 0, 0, 20)
        notificationCard.addView(subtitle)

        val description = TextView(this)
        description.text = "피부 상태는 변하므로 정기적인 분석을 권장합니다.\n\nGo back → Analyze Face → Get Updated Routine"
        description.setTextColor(Color.parseColor("#B0B0B0"))
        description.textSize = 12f
        description.setGravity(android.view.Gravity.CENTER)
        description.setLineSpacing(3f, 1.4f)
        notificationCard.addView(description)

        container.addView(notificationCard)
    }

    private fun displaySavedRoutinePlan(plan: RoutinePlan14Days, skin: SkinResult) {
        val h = TextView(this)
        h.text = "피부 건강 지수: " + skin.skinHealthPercentage.toInt() + "%"
        h.setTextColor(Color.parseColor("#FFD400"))
        h.textSize = 16f
        h.setTypeface(null, android.graphics.Typeface.BOLD)
        h.setPadding(0, 0, 0, 16)
        container.addView(h)

        // Show only the first day's routine - KOREAN VERSION FIRST
        if (plan.dailyRoutines.isNotEmpty()) {
            val d = plan.dailyRoutines[0]

            // ========== KOREAN VERSION ==========
            val koreanCard = LinearLayout(this)
            koreanCard.orientation = LinearLayout.VERTICAL
            koreanCard.setBackgroundResource(R.drawable.card_bg_rounded)
            koreanCard.setPadding(16, 16, 16, 16)
            val lpKorean = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lpKorean.bottomMargin = 12
            koreanCard.layoutParams = lpKorean

            // Day header
            val dhKorean = TextView(this)
            dhKorean.text = "🌟 1일차 스킨케어 루틴"
            dhKorean.setTextColor(Color.parseColor("#FFD400"))
            dhKorean.textSize = 14f
            dhKorean.setTypeface(null, android.graphics.Typeface.BOLD)
            dhKorean.setPadding(0, 0, 0, 10)
            koreanCard.addView(dhKorean)

            // Morning section Korean
            if (d.morning.isNotEmpty()) {
                val morningLabel = TextView(this)
                morningLabel.text = "🌅 아침 (아침 스킨케어)"
                morningLabel.setTextColor(Color.parseColor("#FFB6C1"))
                morningLabel.textSize = 12f
                morningLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                morningLabel.setPadding(0, 8, 0, 6)
                koreanCard.addView(morningLabel)

                for (s in d.morning) {
                    val tv = TextView(this)
                    tv.text = s
                    tv.textSize = 11f
                    tv.setTextColor(Color.WHITE)
                    tv.setPadding(0, 3, 0, 3)
                    koreanCard.addView(tv)
                }
            }

            // Afternoon section Korean
            if (d.afternoon.isNotEmpty()) {
                val afternoonLabel = TextView(this)
                afternoonLabel.text = "☀️ 낮 (점심시간 스킨케어)"
                afternoonLabel.setTextColor(Color.parseColor("#FFD400"))
                afternoonLabel.textSize = 12f
                afternoonLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                afternoonLabel.setPadding(0, 10, 0, 6)
                koreanCard.addView(afternoonLabel)

                for (s in d.afternoon) {
                    val tv = TextView(this)
                    tv.text = s
                    tv.textSize = 11f
                    tv.setTextColor(Color.WHITE)
                    tv.setPadding(0, 3, 0, 3)
                    koreanCard.addView(tv)
                }
            }

            // Evening section Korean
            if (d.evening.isNotEmpty()) {
                val eveningLabel = TextView(this)
                eveningLabel.text = "🌙 저녁 (저녁 스킨케어)"
                eveningLabel.setTextColor(Color.parseColor("#87CEEB"))
                eveningLabel.textSize = 12f
                eveningLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                eveningLabel.setPadding(0, 10, 0, 6)
                koreanCard.addView(eveningLabel)

                for (s in d.evening) {
                    val tv = TextView(this)
                    tv.text = s
                    tv.textSize = 11f
                    tv.setTextColor(Color.WHITE)
                    tv.setPadding(0, 3, 0, 3)
                    koreanCard.addView(tv)
                }
            }

            container.addView(koreanCard)

            // Add instruction text
            val instructionCard = LinearLayout(this)
            instructionCard.orientation = LinearLayout.VERTICAL
            instructionCard.setBackgroundResource(R.drawable.card_bg_rounded)
            instructionCard.setPadding(16, 16, 16, 16)
            val lpInstruction = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lpInstruction.topMargin = 12
            instructionCard.layoutParams = lpInstruction

            val instructionTitle = TextView(this)
            instructionTitle.text = "📋 14일 루틴 안내"
            instructionTitle.setTextColor(Color.parseColor("#FFD400"))
            instructionTitle.textSize = 13f
            instructionTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            instructionTitle.setPadding(0, 0, 0, 8)
            instructionCard.addView(instructionTitle)

            val instruction = TextView(this)
            instruction.text = "최적의 결과를 위해 14일 동안 이 루틴을 매일 따르세요."
            instruction.setTextColor(Color.WHITE)
            instruction.textSize = 11f
            instruction.setLineSpacing(4f, 1.4f)
            instruction.setPadding(0, 0, 0, 0)
            instructionCard.addView(instruction)

            container.addView(instructionCard)
        }
    }

    private fun share() {
        try {
            val store = SkinDataStore(this)
            val plan = store.getLastRoutinePlan()
            if (plan == null) {
                Toast.makeText(this, "데이터 없음", Toast.LENGTH_SHORT).show()
                return
            }
            // Compose routine text (FULL 14-day routine)
            val routineText = StringBuilder()
            routineText.append("스킨케어 루틴 (14일 계획)\n\n")
            var dayIndex = 1
            for (d in plan.dailyRoutines) {
                routineText.append("=== Day ").append(dayIndex).append(" ===\n")
                if (d.morning.isNotEmpty()) {
                    routineText.append("아침:\n")
                    for (s in d.morning) routineText.append(" - ").append(s).append("\n")
                    routineText.append("\n")
                }
                if (d.afternoon.isNotEmpty()) {
                    routineText.append("낮:\n")
                    for (s in d.afternoon) routineText.append(" - ").append(s).append("\n")
                    routineText.append("\n")
                }
                if (d.evening.isNotEmpty()) {
                    routineText.append("저녁:\n")
                    for (s in d.evening) routineText.append(" - ").append(s).append("\n")
                    routineText.append("\n")
                }
                dayIndex++
            }

            // Fetch skin result and append summary to routine text so PDF includes analysis
            val skin = store.getLastSkinResult()
            if (skin != null) {
                val skinSummary = StringBuilder()
                skinSummary.append("\n\n=== Skin Analysis Summary ===\n")
                skinSummary.append("Health: ${skin.skinHealthPercentage}%\n")
                skinSummary.append("Skin Tone: ${skin.skinTone}\n")
                skinSummary.append("Analysis Quality: ${skin.analysisQuality}\n")
                skinSummary.append("Overall Confidence: ${skin.overallConfidence}\n")
                skinSummary.append("\nRecommendations:\n")
                for (r in skin.recommendations) skinSummary.append(" - ").append(r).append("\n")
                skinSummary.append("\nExplanation:\n").append(skin.skinAnalysisExplanation).append("\n")
                routineText.append(skinSummary.toString())
            }

            // Create PDF file with the routine (multi-page)
            val pdfName = "Skincare_Routine_" + Date().time + ".pdf"
            val pdfFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfName)

            val pageWidth = 595
            val pageHeight = 842
            val pdfDocument = PdfDocument()
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f

            val lines = routineText.toString().split("\n")
            var y = 40f
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            for (line in lines) {
                // Draw line
                canvas.drawText(line, 40f, y, paint)
                y += 18f

                // If we overflow current page, finish it and start a new one
                if (y > pageHeight - 40) {
                    pdfDocument.finishPage(page)
                    pageNumber += 1
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
            }

            // Finish last page (if not finished)
            pdfDocument.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Share only the PDF (contains both routine + skin summary)
            val pdfUri = FileProvider.getUriForFile(this, "com.lemonview.ai.fileprovider", pdfFile)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri)
            intent.type = "application/pdf"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show()
        }
    }
}