package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madd_assignment_01.repository.UserRepository
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var logInButton: Button
    private lateinit var forgotPasswordLink: TextView
    private lateinit var signUpLink: TextView
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false

    private lateinit var userRepository: UserRepository

    companion object {
        private const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_sign_in)

            // Initialize user repository
            userRepository = (application as HealthFitnessApplication).userRepository

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
            progressBar = findViewById(R.id.progress_bar)
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

        // Show loading
        setLoadingState(true)

        // Perform sign in
        lifecycleScope.launch {
            try {
                val result = userRepository.signIn(email, password)

                result.onSuccess { user ->
                    runOnUiThread {
                        setLoadingState(false)
                        Toast.makeText(this@SignInActivity, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()

                        // Navigate to dashboard
                        val intent = Intent(this@SignInActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }.onFailure { error ->
                    runOnUiThread {
                        setLoadingState(false)
                        handleSignInError(error.message ?: "Sign in failed")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setLoadingState(false)
                    handleSignInError("An unexpected error occurred")
                    Log.e(TAG, "Sign in error: ${e.message}", e)
                }
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        logInButton.isEnabled = !loading
        logInButton.text = if (loading) "Signing In..." else "Log In"

        // Disable form inputs during loading
        emailEditText.isEnabled = !loading
        passwordEditText.isEnabled = !loading
    }

    private fun handleSignInError(message: String) {
        when {
            message.contains("password", ignoreCase = true) || message.contains("invalid", ignoreCase = true) -> {
                passwordEditText.error = "Invalid email or password"
                passwordEditText.requestFocus()
            }
            message.contains("email", ignoreCase = true) -> {
                emailEditText.error = message
                emailEditText.requestFocus()
            }
            else -> {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
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