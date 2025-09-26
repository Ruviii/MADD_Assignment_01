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

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var logInButton: Button
    private lateinit var forgotPasswordLink: TextView
    private lateinit var signUpLink: TextView
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_sign_in)
            initializeViews()
            setupViews()
            setupClickListeners()
            Log.d(TAG, "SignInActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading sign in page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            emailEditText = findViewById(R.id.email_edit_text)
            passwordEditText = findViewById(R.id.password_edit_text)
            passwordToggle = findViewById(R.id.password_toggle)
            logInButton = findViewById(R.id.log_in_button)
            forgotPasswordLink = findViewById(R.id.forgot_password_link)
            signUpLink = findViewById(R.id.sign_up_link)
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
        // Log In button click
        logInButton.setOnClickListener {
            handleSignIn()
        }

        // Password visibility toggle
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Forgot password link click
        forgotPasswordLink.setOnClickListener {
            handleForgotPassword()
        }

        // Sign Up link click
        signUpLink.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun handleSignIn() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }

        // TODO: Implement actual sign in logic with backend/database
        // For now, simulate login validation
        if (isValidCredentials(email, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

            // Navigate to dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
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
            passwordEditText.error = "Please enter your password"
            passwordEditText.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        // TODO: Replace with actual authentication logic
        // For demo purposes, accept any valid email with password "123456"
        // In real app, this would check against database or API
        return password.length >= 6
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

    private fun handleForgotPassword() {
        val email = emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email first"
            emailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return
        }

        // TODO: Implement forgot password functionality
        Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_LONG).show()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}