package com.lemonview.ai.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lemonview.ai.model.MakeupAnalysisResult
import com.lemonview.ai.model.RoutinePlan14Days
import com.lemonview.ai.model.SkinResult

/**
 * SkinDataStore - Manages persistence of skin analysis and routine plans
 */
class SkinDataStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "skin_analysis_data",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_LAST_SKIN_RESULT = "last_skin_result"
        private const val KEY_LAST_ROUTINE_PLAN = "last_routine_plan"
        private const val KEY_LAST_MAKEUP_RESULT = "last_makeup_result"
        private const val KEY_ANALYSIS_TIMESTAMP = "analysis_timestamp"
        private const val KEY_MAKEUP_TIMESTAMP = "makeup_timestamp"
        private const val KEY_LAST_IMAGE_PATH = "last_image_path"
    }

    /**
     * Save latest skin analysis result
     */
    fun saveSkinResult(skinResult: SkinResult) {
        try {
            val json = gson.toJson(skinResult)
            prefs.edit().apply {
                putString(KEY_LAST_SKIN_RESULT, json)
                putLong(KEY_ANALYSIS_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get last saved skin result
     */
    fun getLastSkinResult(): SkinResult? {
        return try {
            val json = prefs.getString(KEY_LAST_SKIN_RESULT, null)
            if (json != null) {
                gson.fromJson(json, SkinResult::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save 14-day routine plan
     */
    fun saveRoutinePlan(routinePlan: RoutinePlan14Days) {
        try {
            val json = gson.toJson(routinePlan)
            prefs.edit().apply {
                putString(KEY_LAST_ROUTINE_PLAN, json)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get last saved routine plan
     */
    fun getLastRoutinePlan(): RoutinePlan14Days? {
        return try {
            val json = prefs.getString(KEY_LAST_ROUTINE_PLAN, null)
            if (json != null) {
                gson.fromJson(json, RoutinePlan14Days::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save makeup analysis result
     */
    fun saveMakeupResult(result: MakeupAnalysisResult) {
        try {
            val json = gson.toJson(result)
            prefs.edit().apply {
                putString(KEY_LAST_MAKEUP_RESULT, json)
                putLong(KEY_MAKEUP_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get last saved makeup result
     */
    fun getLastMakeupResult(): MakeupAnalysisResult? {
        return try {
            val json = prefs.getString(KEY_LAST_MAKEUP_RESULT, null)
            if (json != null) {
                gson.fromJson(json, MakeupAnalysisResult::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get last analysis timestamp
     */
    fun getLastAnalysisTimestamp(): Long {
        return prefs.getLong(KEY_ANALYSIS_TIMESTAMP, 0L)
    }

    /**
     * Check if analysis data exists
     */
    fun hasAnalysisData(): Boolean {
        return prefs.contains(KEY_LAST_SKIN_RESULT) && prefs.contains(KEY_LAST_ROUTINE_PLAN)
    }

    /**
     * Save last image path
     */
    fun saveLastImagePath(path: String) {
        prefs.edit().apply {
            putString(KEY_LAST_IMAGE_PATH, path)
            apply()
        }
    }

    /**
     * Get last image path
     */
    fun getLastImagePath(): String? {
        return prefs.getString(KEY_LAST_IMAGE_PATH, null)
    }

    /**
     * Clear all analysis data
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Get analysis age in seconds
     */
    fun getAnalysisAgeSeconds(): Long {
        val lastTimestamp = getLastAnalysisTimestamp()
        return if (lastTimestamp > 0) {
            (System.currentTimeMillis() - lastTimestamp) / 1000
        } else {
            Long.MAX_VALUE
        }
    }
}
