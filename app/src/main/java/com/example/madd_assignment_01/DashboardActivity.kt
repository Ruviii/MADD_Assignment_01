package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    // Header views
    private lateinit var greetingTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var motivationQuoteTextView: TextView

    // Today's Summary views
    private lateinit var caloriesValueTextView: TextView
    private lateinit var workoutsValueTextView: TextView
    private lateinit var waterValueTextView: TextView

    // Progress views
    private lateinit var weekProgressTextView: TextView
    private lateinit var weightLossProgressTextView: TextView
    private lateinit var workoutProgressTextView: TextView
    private lateinit var waterIntakeProgressTextView: TextView

    // Featured workout views
    private lateinit var featuredWorkoutTitleTextView: TextView
    private lateinit var featuredWorkoutDescTextView: TextView
    private lateinit var startWorkoutButton: Button

    // Quick action cards
    private lateinit var logWorkoutCard: CardView
    private lateinit var logMealCard: CardView
    private lateinit var remindersCard: CardView
    private lateinit var goalsCard: CardView

    // Analytics
    private lateinit var analyticsCard: CardView

    // Bottom navigation
    private lateinit var homeNavButton: LinearLayout
    private lateinit var workoutsNavButton: LinearLayout
    private lateinit var dietNavButton: LinearLayout
    private lateinit var goalsNavButton: LinearLayout
    private lateinit var remindersNavButton: LinearLayout
    private lateinit var analyticsNavButton: LinearLayout

    companion object {
        private const val TAG = "DashboardActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_dashboard)
            initializeViews()
            setupClickListeners()
            updateDashboardData()
            Log.d(TAG, "DashboardActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading dashboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header views
            greetingTextView = findViewById(R.id.greeting_text)
            userNameTextView = findViewById(R.id.user_name_text)
            motivationQuoteTextView = findViewById(R.id.motivation_quote)

            // Today's Summary views
            caloriesValueTextView = findViewById(R.id.calories_value)
            workoutsValueTextView = findViewById(R.id.workouts_value)
            waterValueTextView = findViewById(R.id.water_value)

            // Progress views
            weekProgressTextView = findViewById(R.id.week_progress_text)
            weightLossProgressTextView = findViewById(R.id.weight_loss_progress)
            workoutProgressTextView = findViewById(R.id.workout_progress)
            waterIntakeProgressTextView = findViewById(R.id.water_intake_progress)

            // Featured workout views
            featuredWorkoutTitleTextView = findViewById(R.id.featured_workout_title)
            featuredWorkoutDescTextView = findViewById(R.id.featured_workout_desc)
            startWorkoutButton = findViewById(R.id.start_workout_button)

            // Quick action cards
            logWorkoutCard = findViewById(R.id.log_workout_card)
            logMealCard = findViewById(R.id.log_meal_card)
            remindersCard = findViewById(R.id.reminders_card)
            goalsCard = findViewById(R.id.goals_card)

            // Analytics
            analyticsCard = findViewById(R.id.analytics_card)

            // Bottom navigation
            homeNavButton = findViewById(R.id.nav_home)
            workoutsNavButton = findViewById(R.id.nav_workouts)
            dietNavButton = findViewById(R.id.nav_diet)
            goalsNavButton = findViewById(R.id.nav_goals)
            remindersNavButton = findViewById(R.id.nav_reminders)
            analyticsNavButton = findViewById(R.id.nav_analytics)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Featured workout
        startWorkoutButton.setOnClickListener {
            Log.d(TAG, "Start workout button clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        // Quick action cards
        logWorkoutCard.setOnClickListener {
            Log.d(TAG, "Log workout card clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        logMealCard.setOnClickListener {
            Log.d(TAG, "Log meal card clicked")
            val intent = Intent(this, DietActivity::class.java)
            startActivity(intent)
        }

        remindersCard.setOnClickListener {
            Log.d(TAG, "Reminders card clicked")
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }

        goalsCard.setOnClickListener {
            Log.d(TAG, "Goals card clicked")
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        // Analytics
        analyticsCard.setOnClickListener {
            Log.d(TAG, "Analytics card clicked")
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        // Bottom navigation
        homeNavButton.setOnClickListener {
            // Already on home, do nothing or refresh
            Log.d(TAG, "Home nav clicked - already on home")
        }

        workoutsNavButton.setOnClickListener {
            Log.d(TAG, "Workouts nav clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        dietNavButton.setOnClickListener {
            Log.d(TAG, "Diet nav clicked")
            val intent = Intent(this, DietActivity::class.java)
            startActivity(intent)
        }

        goalsNavButton.setOnClickListener {
            Log.d(TAG, "Goals nav clicked")
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        remindersNavButton.setOnClickListener {
            Log.d(TAG, "Reminders nav clicked")
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }

        analyticsNavButton.setOnClickListener {
            Log.d(TAG, "Analytics nav clicked")
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateDashboardData() {
        // Update greeting based on time of day
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }

        greetingTextView.text = "$greeting,"
        userNameTextView.text = "Alex" // TODO: Get actual user name
        motivationQuoteTextView.text = "\"One workout at a time, one meal at a time, one day at a time.\""

        // Update today's summary (using sample data)
        caloriesValueTextView.text = "1,450"
        workoutsValueTextView.text = "1"
        waterValueTextView.text = "6"

        // Update progress text
        weekProgressTextView.text = "Keep pushing! You're 65% towards your weekly goal"

        // Update progress percentages (using sample data)
        weightLossProgressTextView.text = "75%"
        workoutProgressTextView.text = "40%"
        waterIntakeProgressTextView.text = "85%"

        // Update featured workout
        featuredWorkoutTitleTextView.text = "Today's Featured Workout"
        featuredWorkoutDescTextView.text = "30-min HIIT Session: Burn 300+ calories"

        Log.d(TAG, "Dashboard data updated successfully")
    }
}