package com.lemonview.ai.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized App Configuration System
 * Manages all app settings dynamically with SharedPreferences persistence
 */
object AppConfig {

    // ================= API KEYS =================
    const val GEMINI_API_KEY = "AIzaSyAvEsV46GWztjXvD87MNUO7rVPyK5bwMCo"

    // ================= APP CONSTANTS =================
    const val APP_NAME = "Lemonview"
    const val APP_VERSION = "1.0.0"
    const val MIN_SDK = 23
    const val TARGET_SDK = 34

    // ================= FEATURE FLAGS =================
    var ENABLE_MAKEUP_ADVISOR = true
    var ENABLE_SKIN_ANALYSIS = true
    var ENABLE_SHOPPING_MALL = true
    var ENABLE_ROUTINE_PLANNER = true
    var ENABLE_OFFLINE_MODE = true

    // ================= THRESHOLDS & LIMITS =================
    const val MIN_IMAGE_QUALITY_SCORE = 0.6f
    const val MAX_FACE_ANALYSIS_TIME_MS = 5000L
    const val IMAGE_COMPRESSION_QUALITY = 85

    // ================= LANGUAGE SETTINGS =================
    var DEFAULT_LANGUAGE = "ko"  // Korean
    var SUPPORTED_LANGUAGES = listOf("ko", "en")

    // ================= UI SETTINGS =================
    var ENABLE_ANIMATIONS = true
    var THEME_MODE = "light"  // light, dark, system

    // ================= CACHE & STORAGE =================
    const val CACHE_SIZE_MB = 100L
    const val MAX_STORED_RESULTS = 50
    const val AUTO_CLEAR_CACHE_DAYS = 30

    // ================= LOGGING =================
    var ENABLE_DETAILED_LOGGING = true
    var LOG_TAG = "Lemonview"

    // ================= SHARED PREFERENCES =================
    private const val PREF_NAME = "lemonview_config"
    private lateinit var prefs: SharedPreferences

    /**
     * Initialize configuration with context
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadSavedConfig()
    }

    /**
     * Load saved configuration from SharedPreferences
     */
    private fun loadSavedConfig() {
        try {
            ENABLE_MAKEUP_ADVISOR = prefs.getBoolean("feature_makeup", ENABLE_MAKEUP_ADVISOR)
            ENABLE_SKIN_ANALYSIS = prefs.getBoolean("feature_skin", ENABLE_SKIN_ANALYSIS)
            ENABLE_SHOPPING_MALL = prefs.getBoolean("feature_shop", ENABLE_SHOPPING_MALL)
            ENABLE_ROUTINE_PLANNER = prefs.getBoolean("feature_routine", ENABLE_ROUTINE_PLANNER)
            ENABLE_OFFLINE_MODE = prefs.getBoolean("feature_offline", ENABLE_OFFLINE_MODE)
            DEFAULT_LANGUAGE = prefs.getString("language", DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
            ENABLE_ANIMATIONS = prefs.getBoolean("animations", ENABLE_ANIMATIONS)
            THEME_MODE = prefs.getString("theme", THEME_MODE) ?: THEME_MODE
            ENABLE_DETAILED_LOGGING = prefs.getBoolean("logging", ENABLE_DETAILED_LOGGING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save configuration to SharedPreferences
     */
    fun saveConfig() {
        try {
            prefs.edit().apply {
                putBoolean("feature_makeup", ENABLE_MAKEUP_ADVISOR)
                putBoolean("feature_skin", ENABLE_SKIN_ANALYSIS)
                putBoolean("feature_shop", ENABLE_SHOPPING_MALL)
                putBoolean("feature_routine", ENABLE_ROUTINE_PLANNER)
                putBoolean("feature_offline", ENABLE_OFFLINE_MODE)
                putString("language", DEFAULT_LANGUAGE)
                putBoolean("animations", ENABLE_ANIMATIONS)
                putString("theme", THEME_MODE)
                putBoolean("logging", ENABLE_DETAILED_LOGGING)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Toggle a feature flag
     */
    fun toggleFeature(featureName: String, enabled: Boolean) {
        when (featureName) {
            "makeup_advisor" -> ENABLE_MAKEUP_ADVISOR = enabled
            "skin_analysis" -> ENABLE_SKIN_ANALYSIS = enabled
            "shopping_mall" -> ENABLE_SHOPPING_MALL = enabled
            "routine_planner" -> ENABLE_ROUTINE_PLANNER = enabled
            "offline_mode" -> ENABLE_OFFLINE_MODE = enabled
        }
        saveConfig()
    }

    /**
     * Set language dynamically
     */
    fun setLanguage(lang: String) {
        if (lang in SUPPORTED_LANGUAGES) {
            DEFAULT_LANGUAGE = lang
            saveConfig()
        }
    }

    /**
     * Get app info as a map
     */
    fun getAppInfo(): Map<String, Any> {
        return mapOf(
            "name" to APP_NAME,
            "version" to APP_VERSION,
            "language" to DEFAULT_LANGUAGE,
            "theme" to THEME_MODE,
            "offline_mode" to ENABLE_OFFLINE_MODE
        )
    }

    /**
     * Reset to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        ENABLE_MAKEUP_ADVISOR = true
        ENABLE_SKIN_ANALYSIS = true
        ENABLE_SHOPPING_MALL = true
        ENABLE_ROUTINE_PLANNER = true
        ENABLE_OFFLINE_MODE = true
        DEFAULT_LANGUAGE = "ko"
        ENABLE_ANIMATIONS = true
        THEME_MODE = "light"
        ENABLE_DETAILED_LOGGING = true
    }
}
