package com.lemonview.ai.config

import android.content.Context

/**
 * Dynamic Feature Manager
 * Controls feature availability at runtime
 */
class FeatureManager(private val context: Context) {

    /**
     * Check if a feature is enabled
     */
    fun isFeatureEnabled(featureName: String): Boolean {
        return when (featureName.lowercase()) {
            "makeup_advisor", "makeup" -> AppConfig.ENABLE_MAKEUP_ADVISOR
            "skin_analysis", "skin" -> AppConfig.ENABLE_SKIN_ANALYSIS
            "shopping_mall", "shop" -> AppConfig.ENABLE_SHOPPING_MALL
            "routine_planner", "routine" -> AppConfig.ENABLE_ROUTINE_PLANNER
            "offline_mode", "offline" -> AppConfig.ENABLE_OFFLINE_MODE
            else -> false
        }
    }

    /**
     * Get list of enabled features
     */
    fun getEnabledFeatures(): List<String> {
        val features = mutableListOf<String>()
        if (AppConfig.ENABLE_MAKEUP_ADVISOR) features.add("makeup_advisor")
        if (AppConfig.ENABLE_SKIN_ANALYSIS) features.add("skin_analysis")
        if (AppConfig.ENABLE_SHOPPING_MALL) features.add("shopping_mall")
        if (AppConfig.ENABLE_ROUTINE_PLANNER) features.add("routine_planner")
        if (AppConfig.ENABLE_OFFLINE_MODE) features.add("offline_mode")
        return features
    }

    /**
     * Get list of disabled features
     */
    fun getDisabledFeatures(): List<String> {
        val allFeatures = listOf("makeup_advisor", "skin_analysis", "shopping_mall", "routine_planner", "offline_mode")
        val enabledFeatures = getEnabledFeatures()
        return allFeatures.filter { it !in enabledFeatures }
    }

    /**
     * Enable feature
     */
    fun enableFeature(featureName: String) {
        AppConfig.toggleFeature(featureName, true)
    }

    /**
     * Disable feature
     */
    fun disableFeature(featureName: String) {
        AppConfig.toggleFeature(featureName, false)
    }

    /**
     * Get feature count
     */
    fun getEnabledFeatureCount(): Int {
        return getEnabledFeatures().size
    }
}
