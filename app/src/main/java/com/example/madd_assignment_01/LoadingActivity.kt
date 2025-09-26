package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var heartIcon: ImageView
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "LoadingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_loading)
            initializeViews()
            startHeartAnimation()
            startProgressAnimation()
            Log.d(TAG, "LoadingActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading app", Toast.LENGTH_SHORT).show()
            // Fallback - navigate directly to onboarding
            navigateToOnboarding()
        }
    }

    private fun initializeViews() {
        try {
            progressBar = findViewById(R.id.progress_bar)
            heartIcon = findViewById(R.id.heart_icon)
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun startHeartAnimation() {
        val scaleAnimation = ScaleAnimation(
            1.0f, 1.2f,
            1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 800
        scaleAnimation.repeatCount = Animation.INFINITE
        scaleAnimation.repeatMode = Animation.REVERSE
        heartIcon.startAnimation(scaleAnimation)
    }

    private fun startProgressAnimation() {
        Thread {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                }
                try {
                    Thread.sleep(30)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            handler.postDelayed({
                startMainActivity()
            }, 500)
        }.start()
    }

    private fun startMainActivity() {
        navigateToOnboarding()
    }

    private fun navigateToOnboarding() {
        try {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Successfully navigated to OnboardingActivity")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to onboarding: ${e.message}", e)
            Toast.makeText(this, "Error starting app", Toast.LENGTH_SHORT).show()
        }
    }
}