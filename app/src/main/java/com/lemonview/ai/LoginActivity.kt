package com.lemonview.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("lemonview_user", MODE_PRIVATE)

        val etLoginName = findViewById<EditText>(R.id.etLoginName)
        val etLoginDob = findViewById<EditText>(R.id.etLoginDob)
        val checkTerms = findViewById<CheckBox>(R.id.checkTerms)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegisterNew = findViewById<Button>(R.id.btnRegisterNew)

        // Setup DOB date picker for login
        val loginDateFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        etLoginDob.setOnClickListener {
            try {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val picker = android.app.DatePickerDialog(this, { _, y, m, d ->
                    val selCal = Calendar.getInstance()
                    selCal.set(y, m, d)
                    etLoginDob.setText(loginDateFormat.format(selCal.time))
                }, year, month, day)
                picker.datePicker.maxDate = Calendar.getInstance().timeInMillis
                picker.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener {
            val name = etLoginName.text.toString().trim()
            val dob = etLoginDob.text.toString().trim()

            when {
                name.isEmpty() -> showToast(getString(R.string.error_name_empty))
                !checkTerms.isChecked -> showToast(getString(R.string.error_terms_not_accepted))
                dob.isEmpty() -> showToast(getString(R.string.error_dob_empty))
                !isAgeValid(dob) -> showToast(getString(R.string.error_age_invalid))
                else -> {
                    // Verify against saved data
                    val savedName = sharedPreferences.getString("user_name", "")
                    val savedDob = sharedPreferences.getString("user_dob", "")

                    if (name == savedName && dob == savedDob) {
                        // ✅ LOGIN SUCCESSFUL
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("is_logged_in", true)
                        editor.apply()

                        // update last_login in users.csv for this user (keeps CSV in sync)
                        updateUserLastLoginCsv(name)

                        showToast(getString(R.string.success_login))

                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.putExtra("user_name", name)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast(getString(R.string.error_login_mismatch))
                    }
                }
            }
        }

        // REGISTER NEW BUTTON
        btnRegisterNew.setOnClickListener {
            // Clear login data
            val editor = sharedPreferences.edit()
            editor.putBoolean("is_registered", false)
            editor.putBoolean("is_logged_in", false)
            editor.apply()

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
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
     * Update last_login timestamp for given email/name in users.csv
     */
    private fun updateUserLastLoginCsv(emailOrName: String) {
        try {
            val docs = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docs == null) return
            val csvFile = File(docs, "users.csv")
            if (!csvFile.exists()) return

            val lines = mutableListOf<String>()
            BufferedReader(FileReader(csvFile)).use { br ->
                var line: String? = br.readLine()
                while (line != null) {
                    lines.add(line)
                    line = br.readLine()
                }
            }

            var changed = false
            for (i in 1 until lines.size) {
                val l = lines[i]
                if (l.contains("\"$emailOrName\"") || l.contains(emailOrName)) {
                    // Attempt preserve registration_time
                    val parts = l.split(',')
                    val regTime = if (parts.size >= 5) parts[4] else System.currentTimeMillis().toString()
                    // Extract name,email,dob,gender if possible
                    val nameField = if (parts.size >= 1) parts[0] else "\"$emailOrName\""
                    val emailField = if (parts.size >= 2) parts[1] else "\"$emailOrName\""
                    val dobField = if (parts.size >= 3) parts[2] else "\"\""
                    val genderField = if (parts.size >= 4) parts[3] else "\"\""
                    val newRow = StringBuilder()
                    newRow.append(nameField).append(',')
                    newRow.append(emailField).append(',')
                    newRow.append(dobField).append(',')
                    newRow.append(genderField).append(',')
                    newRow.append(regTime).append(',')
                    newRow.append(System.currentTimeMillis())
                    lines[i] = newRow.toString()
                    changed = true
                    break
                }
            }

            if (changed) {
                    FileWriter(csvFile, false).use { fw ->
                        for (ln in lines) {
                            fw.append(ln).append('\n')
                        }
                        fw.flush()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
