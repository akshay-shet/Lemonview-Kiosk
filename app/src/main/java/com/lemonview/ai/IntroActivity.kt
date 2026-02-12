package com.lemonview.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lemonview.ai.config.AppConfig
import com.lemonview.ai.config.AppManager

class IntroActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Initialize AppConfig and AppManager on app startup
        AppConfig.init(this)
        AppManager.init(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("lemonview_user", MODE_PRIVATE)

        // ✅ ALWAYS SHOW INTRO PAGE FIRST - Then navigate on button click
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            // Check login state when user clicks Next button
            val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
            val isRegistered = sharedPreferences.getBoolean("is_registered", false)

            when {
                isLoggedIn -> {
                    // User is already logged in - go to main menu
                    val intent = Intent(this, MainMenuActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                isRegistered && !isLoggedIn -> {
                    // User is registered but not logged in - show login page
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    // New user - show register page
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                }
            }
        }
    }
}
