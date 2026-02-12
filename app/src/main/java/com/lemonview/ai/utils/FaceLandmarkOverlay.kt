package com.lemonview.ai.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

class FaceLandmarkOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // Circle guide paint (cyan with stroke)
    private val circlePaint = Paint().apply {
        color = Color.parseColor("#00FFFF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    // Circle center point paint (small dot)
    private val centerPointPaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var detectedFaces: List<Face> = emptyList()
    private var scaleX = 1f
    private var scaleY = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    /**
     * Update faces and transformation parameters for drawing
     */
    fun setFaces(
        faces: List<Face>,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        this.detectedFaces = faces
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.offsetX = offsetX
        this.offsetY = offsetY
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw center circle guide
        drawCenterCircleGuide(canvas)
    }

    /**
     * Draw a centered circle guide for face positioning
     */
    private fun drawCenterCircleGuide(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Circle radius - increased to be larger (about 40% of smaller dimension)
        val radius = (Math.min(width, height) / 2.5f)

        // Draw cyan circle
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Draw center point (golden dot)
        canvas.drawCircle(centerX, centerY, 8f, centerPointPaint)
    }
}
