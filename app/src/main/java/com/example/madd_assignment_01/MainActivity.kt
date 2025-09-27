package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var startWorkoutButton: Button
    private lateinit var trackDietButton: Button
    private lateinit var viewGoalsButton: Button

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_main)
            initializeViews()
            setupClickListeners()
            Log.d(TAG, "MainActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading main page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            startWorkoutButton = findViewById(R.id.start_workout_button)
            trackDietButton = findViewById(R.id.track_diet_button)
            viewGoalsButton = findViewById(R.id.view_goals_button)
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        startWorkoutButton.setOnClickListener {
            Log.d(TAG, "Start Workout button clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        trackDietButton.setOnClickListener {
            Log.d(TAG, "Track Diet button clicked")
            val intent = Intent(this, DietActivity::class.java)
            startActivity(intent)
        }

        viewGoalsButton.setOnClickListener {
            Log.d(TAG, "View Goals button clicked")
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }
    }
}