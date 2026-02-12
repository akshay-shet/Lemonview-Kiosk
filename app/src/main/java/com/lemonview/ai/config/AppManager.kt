package com.lemonview.ai.config

import android.content.Context
import android.util.Log

/**
 * App Manager - Central Hub for App Control
 * Provides easy access to all app configurations and features
 */
object AppManager {

    private const val TAG = "AppManager"
    private lateinit var featureManager: FeatureManager

    /**
     * Initialize App Manager
     */
    fun init(context: Context) {
        featureManager = FeatureManager(context)
        Log.d(TAG, "✓ AppManager initialized")
        logAppStatus()
    }

    /**
     * Get feature manager instance
     */
    fun getFeatureManager(): FeatureManager {
        return featureManager
    }

    /**
     * Log current app status
     */
    fun logAppStatus() {
        Log.d(TAG, "=== APP STATUS ===")
        Log.d(TAG, "App Version: ${AppConfig.APP_VERSION}")
        Log.d(TAG, "Language: ${AppConfig.DEFAULT_LANGUAGE}")
        Log.d(TAG, "Theme: ${AppConfig.THEME_MODE}")
        Log.d(TAG, "Enabled Features: ${featureManager.getEnabledFeatureCount()}/5")
        Log.d(TAG, "Offline Mode: ${AppConfig.ENABLE_OFFLINE_MODE}")
        Log.d(TAG, "Animations: ${AppConfig.ENABLE_ANIMATIONS}")
        Log.d(TAG, "Features: ${featureManager.getEnabledFeatures()}")
    }

    /**
     * Get detailed app info
     */
    fun getDetailedAppInfo(): String {
        return buildString {
            append("📱 Lemonview v${AppConfig.APP_VERSION}\n")
            append("🌍 Language: ${AppConfig.DEFAULT_LANGUAGE}\n")
            append("🎨 Theme: ${AppConfig.THEME_MODE}\n")
            append("✨ Animations: ${AppConfig.ENABLE_ANIMATIONS}\n")
            append("🔌 Offline Mode: ${AppConfig.ENABLE_OFFLINE_MODE}\n")
            append("📊 Enabled Features: ${featureManager.getEnabledFeatureCount()}/5\n")
            append("\n📋 Feature Status:\n")
            featureManager.getEnabledFeatures().forEach {
                append("  ✅ $it\n")
            }
            featureManager.getDisabledFeatures().forEach {
                append("  ❌ $it\n")
            }
        }
    }

    /**
     * Enable all features
     */
    fun enableAllFeatures() {
        featureManager.enableFeature("makeup_advisor")
        featureManager.enableFeature("skin_analysis")
        featureManager.enableFeature("shopping_mall")
        featureManager.enableFeature("routine_planner")
        featureManager.enableFeature("offline_mode")
        AppConfig.saveConfig()
        Log.d(TAG, "✓ All features enabled")
    }

    /**
     * Disable all features
     */
    fun disableAllFeatures() {
        featureManager.disableFeature("makeup_advisor")
        featureManager.disableFeature("skin_analysis")
        featureManager.disableFeature("shopping_mall")
        featureManager.disableFeature("routine_planner")
        featureManager.disableFeature("offline_mode")
        AppConfig.saveConfig()
        Log.d(TAG, "✓ All features disabled")
    }

    /**
     * Maintenance mode - disable all but essential features
     */
    fun enableMaintenanceMode() {
        featureManager.disableFeature("makeup_advisor")
        featureManager.disableFeature("shopping_mall")
        featureManager.disableFeature("routine_planner")
        featureManager.enableFeature("skin_analysis")
        featureManager.enableFeature("offline_mode")
        AppConfig.saveConfig()
        Log.d(TAG, "✓ Maintenance mode enabled")
    }

    /**
     * Production mode - enable all features
     */
    fun enableProductionMode() {
        enableAllFeatures()
        Log.d(TAG, "✓ Production mode enabled")
    }

    /**
     * Demo mode - disable features that require real data
     */
    fun enableDemoMode() {
        featureManager.enableFeature("makeup_advisor")
        featureManager.enableFeature("skin_analysis")
        featureManager.disableFeature("shopping_mall")
        featureManager.disableFeature("routine_planner")
        featureManager.enableFeature("offline_mode")
        AppConfig.saveConfig()
        Log.d(TAG, "✓ Demo mode enabled")
    }

    /**
     * Get summary statistics
     */
    fun getStats(): Map<String, Any> {
        return mapOf(
            "appName" to AppConfig.APP_NAME,
            "version" to AppConfig.APP_VERSION,
            "language" to AppConfig.DEFAULT_LANGUAGE,
            "theme" to AppConfig.THEME_MODE,
            "enabledFeatures" to featureManager.getEnabledFeatures(),
            "disabledFeatures" to featureManager.getDisabledFeatures(),
            "featureCount" to featureManager.getEnabledFeatureCount(),
            "offlineMode" to AppConfig.ENABLE_OFFLINE_MODE,
            "animationsEnabled" to AppConfig.ENABLE_ANIMATIONS,
            "minImageQuality" to AppConfig.MIN_IMAGE_QUALITY_SCORE,
            "imageQuality" to AppConfig.IMAGE_COMPRESSION_QUALITY
        )
    }
}
