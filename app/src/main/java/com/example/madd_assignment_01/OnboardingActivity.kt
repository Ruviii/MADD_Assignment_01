package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.example.madd_assignment_01.data.DataManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private var currentPage = 0
    private val maxPages = 4

    companion object {
        private const val TAG = "OnboardingActivity"
    }

    private val layoutIds = arrayOf(
        R.layout.onboarding_screen_1,
        R.layout.onboarding_screen_2,
        R.layout.onboarding_screen_3,
        R.layout.onboarding_screen_4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_onboarding)
            initializeViews()
            setupOnboardingScreens()
            setupClickListeners()
            updateBackButtonVisibility()
            Log.d(TAG, "OnboardingActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading onboarding", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        viewFlipper = findViewById(R.id.onboarding_view_flipper)
    }

    private fun setupOnboardingScreens() {
        try {
            // Add all onboarding screens to ViewFlipper
            for (layoutId in layoutIds) {
                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(layoutId, viewFlipper, false)
                viewFlipper.addView(view)
            }
            Log.d(TAG, "All onboarding screens added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up onboarding screens: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            // We need to set up click listeners for each screen individually
            // since they are separate layouts
            for (i in 0 until viewFlipper.childCount) {
                val view = viewFlipper.getChildAt(i)
                val nextBtn = view.findViewById<Button>(R.id.next_button)
                val backBtn = view.findViewById<TextView>(R.id.back_button)

                if (nextBtn != null && backBtn != null) {
                    nextBtn.setOnClickListener {
                        if (currentPage < maxPages - 1) {
                            currentPage++
                            viewFlipper.displayedChild = currentPage
                            animateTransition()
                            updateBackButtonVisibility()
                            Log.d(TAG, "Moved to page $currentPage")
                        } else {
                            // Navigate to SignUp when onboarding is complete
                            navigateToSignUp()
                        }
                    }

                    backBtn.setOnClickListener {
                        if (currentPage > 0) {
                            currentPage--
                            viewFlipper.displayedChild = currentPage
                            animateTransition()
                            updateBackButtonVisibility()
                            Log.d(TAG, "Moved back to page $currentPage")
                        }
                    }
                } else {
                    Log.w(TAG, "Could not find buttons in screen $i")
                }
            }
            Log.d(TAG, "Click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}", e)
            throw e
        }
    }

    private fun animateTransition() {
        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
    }

    private fun updateBackButtonVisibility() {
        try {
            for (i in 0 until viewFlipper.childCount) {
                val view = viewFlipper.getChildAt(i)
                val backBtn = view.findViewById<TextView>(R.id.back_button)

                if (backBtn != null) {
                    // Hide back button on first screen, show on others
                    backBtn.visibility = if (i == 0) View.GONE else View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating back button visibility: ${e.message}", e)
        }
    }

    private fun navigateToSignUp() {
        try {
            // Mark first launch as complete
            val dataManager = DataManager.getInstance(this)
            dataManager.setFirstLaunchComplete()

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Successfully navigated to SignUpActivity")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SignUp: ${e.message}", e)
            Toast.makeText(this, "Error proceeding to sign up", Toast.LENGTH_SHORT).show()
        }
    }
}