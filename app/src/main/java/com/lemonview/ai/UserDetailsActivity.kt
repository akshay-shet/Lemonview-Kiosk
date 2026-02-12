package com.lemonview.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        sharedPreferences = getSharedPreferences("lemonview_user", MODE_PRIVATE)

        // ================= BACK BUTTON =================
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // ================= RETRIEVE USER DATA =================
        val userName = sharedPreferences.getString("user_name", "N/A") ?: "N/A"
        val userEmail = sharedPreferences.getString("user_email", "N/A") ?: "N/A"
        val userDob = sharedPreferences.getString("user_dob", "N/A") ?: "N/A"
        val userGender = sharedPreferences.getString("user_gender", "N/A") ?: "N/A"
        val registrationTime = sharedPreferences.getLong("registration_time", 0L)

        // ================= FORMAT DOB =================
        val formattedDob = try {
            val storedFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = storedFormat.parse(userDob)
            if (date != null) {
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)
            } else {
                userDob
            }
        } catch (e: Exception) {
            userDob
        }

        // ================= FORMAT REGISTRATION TIME =================
        val formattedRegTime = if (registrationTime > 0) {
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(registrationTime)
        } else {
            "N/A"
        }

        // ================= DISPLAY USER DATA =================
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val tvGender = findViewById<TextView>(R.id.tvGender)
        val tvRegDate = findViewById<TextView>(R.id.tvRegDate)

        tvUserName.text = userName
        tvEmail.text = userEmail
        tvDob.text = formattedDob
        tvGender.text = userGender
        tvRegDate.text = formattedRegTime

        // ================= LOGOUT BUTTON =================
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        try {
            val editor = sharedPreferences.edit()
            editor.putBoolean("is_logged_in", false)
            editor.apply()

            // Redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
