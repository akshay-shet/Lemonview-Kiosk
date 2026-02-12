package com.lemonview.ai

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lemonview.ai.config.AppConfig
import com.lemonview.ai.config.FeatureManager
import com.lemonview.ai.utils.SkinDataStore

class MainMenuActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var featureManager: FeatureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        sharedPreferences = getSharedPreferences("lemonview_user", MODE_PRIVATE)
        featureManager = FeatureManager(this)

        // ================= CAMERA ICON =================
        val icCamera = findViewById<ImageView>(R.id.icCamera)
        icCamera.setOnClickListener {
            if (featureManager.isFeatureEnabled("skin_analysis")) {
                val intent = Intent(this, SkinAnalysisActivity::class.java)
                intent.putExtra("mode", "skin_analysis")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Skin Analysis is currently disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // ================= USER PROFILE ICON =================
        val icUser = findViewById<ImageView>(R.id.icUser)
        icUser.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        // ================= BOTTOM NAV =================
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_makeup -> {
                    if (featureManager.isFeatureEnabled("makeup_advisor")) {
                        val intent = Intent(this, SkinAnalysisActivity::class.java)
                        intent.putExtra("mode", "makeup_advisor")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Makeup Advisor is currently disabled", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.nav_skin -> {
                    if (featureManager.isFeatureEnabled("skin_analysis")) {
                        startActivity(Intent(this, SkinAnalysisActivity::class.java))
                    } else {
                        Toast.makeText(this, "Skin Analysis is currently disabled", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.nav_shop -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://share.google/WAPzGxxknmoqOdrkx"))
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // ================= MAIN MENU CARDS =================

        val cardSkin = findViewById<LinearLayout>(R.id.cardSkin)
        val cardMakeup = findViewById<LinearLayout>(R.id.cardMakeup)
        val cardShop = findViewById<LinearLayout>(R.id.cardShop)
        val cardRoutine = findViewById<LinearLayout>(R.id.cardRoutine)

        // AI Skin Analysis
        cardSkin.setOnClickListener {
            if (featureManager.isFeatureEnabled("skin_analysis")) {
                val intent = Intent(this, SkinAnalysisActivity::class.java)
                intent.putExtra("mode", "skin_analysis")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Skin Analysis is currently disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Makeup Advisor - captures fresh image each time
        cardMakeup.setOnClickListener {
            if (featureManager.isFeatureEnabled("makeup_advisor")) {
                val intent = Intent(this, SkinAnalysisActivity::class.java)
                intent.putExtra("mode", "makeup_advisor")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Makeup Advisor is currently disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Shopping Mall (redirect to web link)
        cardShop.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://share.google/WAPzGxxknmoqOdrkx"))
            startActivity(intent)
        }

        // Routine Planner
        cardRoutine.setOnClickListener {
            if (featureManager.isFeatureEnabled("routine_planner")) {
                // Check if skin analysis has been completed
                val dataStore = SkinDataStore(this)
                if (dataStore.getLastSkinResult() != null) {
                    // Don't clear the fresh analysis flag - allow viewing saved routines
                    startActivity(Intent(this, RoutinePlannerActivity::class.java))
                } else {
                    Toast.makeText(this, "Please complete skin analysis first to access your personalized routine planner", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Routine Planner is currently disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}