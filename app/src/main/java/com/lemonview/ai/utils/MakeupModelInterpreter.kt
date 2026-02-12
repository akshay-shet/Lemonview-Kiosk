package com.lemonview.ai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Loads TFLite model from assets/models/makeup_advisor.tflite and performs inference.
 * Expected simple model for POC: single output softmax for skin tone classification.
 */
class MakeupModelInterpreter(private val context: Context) {
    companion object {
        private const val TAG = "MakeupModelInterp"
        private const val MODEL_PATH = "models/makeup_advisor.tflite"
        private const val COLORS_PATH = "models/colors.json"
    }

    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private var colorMap: JSONObject? = null

    init {
        try {
            val am = context.assets
            // Load colors mapping if present
            try {
                val isr: InputStream = am.open(COLORS_PATH)
                val txt = isr.bufferedReader().use { it.readText() }
                colorMap = JSONObject(txt)
            } catch (_: Exception) {
                // missing colors.json is fine
            }

            // Load TFLite model if present
            try {
                val fd = am.openFd(MODEL_PATH)
                val input = fd.createInputStream()
                val modelBytes = input.readBytes()
                val byteBuffer = java.nio.ByteBuffer.allocateDirect(modelBytes.size).order(java.nio.ByteOrder.nativeOrder())
                byteBuffer.put(modelBytes)
                byteBuffer.rewind()
                val opts = org.tensorflow.lite.Interpreter.Options()
                interpreter = org.tensorflow.lite.Interpreter(byteBuffer, opts)
                Log.d(TAG, "Loaded TFLite model from assets: $MODEL_PATH")
            } catch (e: Exception) {
                Log.w(TAG, "TFLite model not found or failed to load: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing interpreter: ${e.message}", e)
        }
    }

    fun isModelAvailable(): Boolean = interpreter != null

    // Simple helper: predict skin tone class from bitmap and return class name and confidence
    fun predictSkinTone(bitmap: Bitmap): Pair<String, Float> {
        try {
            if (interpreter == null) return Pair("", 0f)
            val input = preprocess(bitmap)
            val output = Array(1) { FloatArray(3) } // expecting 3 classes for POC (Deep, Medium, Fair)
            interpreter!!.run(input, output)
            val probs = output[0]
            var bestIdx = 0
            var best = probs[0]
            for (i in probs.indices) {
                if (probs[i] > best) {
                    best = probs[i]
                    bestIdx = i
                }
            }
            val labels = listOf("Deep", "Medium", "Fair")
            return Pair(labels.getOrNull(bestIdx) ?: "Medium", best)
        } catch (e: Exception) {
            Log.e(TAG, "Prediction error: ${e.message}", e)
            return Pair("", 0f)
        }
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val size = 224
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val input = Array(1) { Array(size) { Array(size) { FloatArray(3) } } }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val p = scaled.getPixel(x, y)
                input[0][y][x][0] = ((p shr 16) and 0xFF) / 255f
                input[0][y][x][1] = ((p shr 8) and 0xFF) / 255f
                input[0][y][x][2] = (p and 0xFF) / 255f
            }
        }
        return input
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
