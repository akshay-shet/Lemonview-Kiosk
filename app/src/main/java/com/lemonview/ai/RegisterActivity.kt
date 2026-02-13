package com.lemonview.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences("lemonview_user", MODE_PRIVATE)

        // Check if already registered
        val isRegistered = sharedPreferences.getBoolean("is_registered", false)
        if (isRegistered) {
            // User already registered, go to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDob = findViewById<EditText>(R.id.etDob)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val checkConsent = findViewById<CheckBox>(R.id.checkConsent)
        val btnNext = findViewById<Button>(R.id.btnNext)

        // Setup DOB date picker (click to open)
        val dateFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        etDob.setOnClickListener {
            try {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val picker = android.app.DatePickerDialog(this, { _, y, m, d ->
                    val selCal = Calendar.getInstance()
                    selCal.set(y, m, d)
                    etDob.setText(dateFormat.format(selCal.time))
                }, year, month, day)
                picker.datePicker.maxDate = Calendar.getInstance().timeInMillis
                picker.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // NEXT BUTTON
        btnNext.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val dob = etDob.text.toString().trim()

            val selectedGenderId = rgGender.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                R.id.rbOther -> "Other"
                else -> ""
            }

            when {
                name.isEmpty() -> showToast(getString(R.string.error_name_empty))
                email.isEmpty() -> showToast(getString(R.string.error_email_empty))
                selectedGenderId == -1 -> showToast(getString(R.string.error_gender_empty))
                !checkConsent.isChecked -> showToast(getString(R.string.error_consent_empty))
                dob.isEmpty() -> showToast(getString(R.string.error_dob_empty))
                !isAgeValid(dob) -> showToast(getString(R.string.error_age_invalid))

                else -> {
                    // ✅ SAVE USER DATA TO SHARED PREFERENCES
                    val editor = sharedPreferences.edit()
                    editor.putString("user_name", name)
                    editor.putString("user_email", email)
                    editor.putString("user_dob", dob)
                    editor.putString("user_gender", gender)
                    editor.putBoolean("is_registered", true)
                    editor.putBoolean("is_logged_in", true)
                    editor.putLong("registration_time", System.currentTimeMillis())
                    editor.apply()

                    // Save user details to users.csv (Excel-readable) without altering other app behavior
                    saveUserToCsv(name, email, dob, gender)

                    showToast(getString(R.string.success_registration))

                    // ✅ AUTO-LOGIN - MOVE DIRECTLY TO MAIN MENU
                    val intent = Intent(this, MainMenuActivity::class.java)
                    intent.putExtra("user_name", name)
                    intent.putExtra("user_email", email)
                    intent.putExtra("user_dob", dob)
                    intent.putExtra("user_gender", gender)
                    startActivity(intent)

                    // ✅ PREVENT BACK TO REGISTER
                    finish()
                }
            }
        }
    }

    private fun isAgeValid(dobString: String): Boolean {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
            sdf.isLenient = false
            val dob = sdf.parse(dobString) ?: return false

            val dobCal = Calendar.getInstance().apply { time = dob }
            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
            if (today.get(Calendar.MONTH) < dobCal.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dobCal.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH))) {
                age--
            }

            return age >= 16
        } catch (e: Exception) {
            return false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Save or update user entry in users.csv inside app documents.
     * CSV columns: name,email,dob,gender,registration_time,last_login
     */
    private fun saveUserToCsv(name: String, email: String, dob: String, gender: String) {
        try {
            val docs = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docs == null) return
            val csvFile = File(docs, "users.csv")
            val now = System.currentTimeMillis()

            // Prepare CSV row with quoted fields
            val row = StringBuilder()
            row.append('"').append(name.replace('"', '\"')).append('"').append(',')
            row.append('"').append(email.replace('"', '\"')).append('"').append(',')
            row.append('"').append(dob.replace('"', '\"')).append('"').append(',')
            row.append('"').append(gender.replace('"', '\"')).append('"').append(',')
            row.append(now).append(',').append(now).append('\n')

            if (!csvFile.exists()) {
                // Write header + row
                FileWriter(csvFile).use { fw ->
                    fw.append("name,email,dob,gender,registration_time,last_login\n")
                    fw.append(row.toString())
                    fw.flush()
                }
            } else {
                // If user with same email exists, update line; else append
                val lines = mutableListOf<String>()
                BufferedReader(FileReader(csvFile)).use { br ->
                    var line: String? = br.readLine()
                    while (line != null) {
                        lines.add(line)
                        line = br.readLine()
                    }
                }

                var updated = false
                for (i in 1 until lines.size) { // skip header
                    val l = lines[i]
                    if (l.contains('"' + email + '"') || l.contains("," + email + ",") || l.contains(email)) {
                        // Replace with new row (keep registration_time if present)
                        // Try to preserve original registration_time if possible
                        val parts = l.split(',')
                        val regTime = if (parts.size >= 5) parts[4] else now.toString()
                        val newRow = StringBuilder()
                        newRow.append('"').append(name.replace('"', '\"')).append('"').append(',')
                        newRow.append('"').append(email.replace('"', '\"')).append('"').append(',')
                        newRow.append('"').append(dob.replace('"', '\"')).append('"').append(',')
                        newRow.append('"').append(gender.replace('"', '\"')).append('"').append(',')
                        newRow.append(regTime).append(',').append(now).append('\n')
                        lines[i] = newRow.toString().trimEnd()
                        updated = true
                        break
                    }
                }

                if (!updated) {
                    // Append
                    FileWriter(csvFile, true).use { fw ->
                        fw.append(row.toString())
                        fw.flush()
                    }
                } else {
                    // Rewrite whole file
                    FileWriter(csvFile, false).use { fw ->
                        for (ln in lines) {
                            fw.append(ln).append('\n')
                        }
                        fw.flush()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
