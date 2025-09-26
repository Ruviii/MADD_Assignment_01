package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var continueButton: Button
    private lateinit var logInLink: TextView
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_sign_up)
            initializeViews()
            setupViews()
            setupClickListeners()
            Log.d(TAG, "SignUpActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading sign up page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            fullNameEditText = findViewById(R.id.full_name_edit_text)
            emailEditText = findViewById(R.id.email_edit_text)
            passwordEditText = findViewById(R.id.password_edit_text)
            passwordToggle = findViewById(R.id.password_toggle)
            continueButton = findViewById(R.id.continue_button)
            logInLink = findViewById(R.id.log_in_link)
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupViews() {
        // Initially hide password
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    private fun setupClickListeners() {
        // Continue button click
        continueButton.setOnClickListener {
            handleSignUp()
        }

        // Password visibility toggle
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Log In link click
        logInLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun handleSignUp() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // Validate inputs
        if (!validateInputs(fullName, email, password)) {
            return
        }

        // TODO: Implement actual sign up logic with backend/database
        // For now, show success message and navigate to main app
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to dashboard
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun validateInputs(fullName: String, email: String, password: String): Boolean {
        // Validate full name
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Please enter your full name"
            fullNameEditText.requestFocus()
            return false
        }

        if (fullName.length < 2) {
            fullNameEditText.error = "Name must be at least 2 characters"
            fullNameEditText.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email"
            emailEditText.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordEditText.error = "Please enter a password"
            passwordEditText.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return false
        }

        if (!password.any { it.isDigit() }) {
            passwordEditText.error = "Password must contain at least one number"
            passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            isPasswordVisible = false
        } else {
            // Show password
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility)
            isPasswordVisible = true
        }

        // Move cursor to end
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}