package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.madd_assignment_01.data.DataManager
import com.example.madd_assignment_01.data.DatabaseDataManager
import com.example.madd_assignment_01.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    // Header views
    private lateinit var greetingTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var motivationQuoteTextView: TextView
    private lateinit var settingsButton: ImageView

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

    // Data Manager
    private lateinit var dataManager: DataManager
    private lateinit var databaseDataManager: DatabaseDataManager
    private lateinit var userRepository: UserRepository

    companion object {
        private const val TAG = "DashboardActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_dashboard)

            // Check if user is logged in
            userRepository = (application as HealthFitnessApplication).userRepository
            if (!userRepository.isLoggedIn()) {
                // Redirect to sign in if not logged in
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }

            dataManager = DataManager.getInstance(this)
            databaseDataManager = DatabaseDataManager.getInstance(this)
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
            settingsButton = findViewById(R.id.settings_button)

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
        // Settings button
        settingsButton.setOnClickListener {
            Log.d(TAG, "Settings button clicked")
            showSettingsDialog()
        }

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

        // Add long click listener to user name for settings/logout
        userNameTextView.setOnLongClickListener {
            showUserMenu()
            true
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
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..23 -> "Good Evening"
            else -> "Good Morning" // fallback
        }

        greetingTextView.text = "$greeting,"

        // Get user name from UserRepository and extract first name only
        val fullName = userRepository.getCurrentUserName() ?: "User"
        val firstName = fullName.split(" ").firstOrNull() ?: "User"
        userNameTextView.text = firstName

        motivationQuoteTextView.text = "\"One workout at a time, one meal at a time, one day at a time.\""

        // Update today's summary with real data
        val todaysCalories = dataManager.getTodaysCaloriesConsumed()
        val weeklyWorkoutMinutes = dataManager.getWeeklyWorkoutMinutes()
        val workoutCount = if (weeklyWorkoutMinutes > 0) weeklyWorkoutMinutes / 30 else 0 // Assuming 30 min average

        caloriesValueTextView.text = todaysCalories.toString()
        workoutsValueTextView.text = workoutCount.toString()
        waterValueTextView.text = "6" // TODO: Implement water tracking

        // Calculate weekly progress
        val weeklyCaloriesBurned = dataManager.getWeeklyCaloriesBurned()
        val weeklyGoal = 2000 // Example weekly calorie burn goal
        val weeklyProgress = if (weeklyGoal > 0) (weeklyCaloriesBurned * 100 / weeklyGoal).coerceAtMost(100) else 0

        weekProgressTextView.text = "Keep pushing! You're $weeklyProgress% towards your weekly goal"

        // Update progress with real goal data
        val activeGoals = dataManager.getActiveGoals()
        val weightGoal = activeGoals.find { it.category == GoalCategory.WEIGHT }
        val workoutGoal = activeGoals.find { it.category == GoalCategory.ACTIVITY }
        val hydrationGoal = activeGoals.find { it.category == GoalCategory.HYDRATION }

        weightLossProgressTextView.text = "${weightGoal?.progressPercentage ?: 0}%"
        workoutProgressTextView.text = "${workoutGoal?.progressPercentage ?: 0}%"
        waterIntakeProgressTextView.text = "${hydrationGoal?.progressPercentage ?: 85}%"

        // Update featured workout
        featuredWorkoutTitleTextView.text = "Today's Featured Workout"
        val featuredWorkouts = listOf(
            "30-min HIIT Session: Burn 300+ calories",
            "45-min Strength Training: Build muscle",
            "60-min Yoga Flow: Improve flexibility",
            "20-min Core Blast: Strengthen your core"
        )
        val randomWorkout = featuredWorkouts.random()
        featuredWorkoutDescTextView.text = randomWorkout

        Log.d(TAG, "Dashboard data updated successfully")
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            "Edit Profile",
            "Account Settings",
            "Logout"
        )

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditProfileDialog()
                    1 -> showAccountSettings()
                    2 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun showUserMenu() {
        val options = arrayOf(
            "Profile Settings",
            "Account Settings",
            "Sign Out"
        )

        AlertDialog.Builder(this)
            .setTitle("Account Menu")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditProfileDialog()
                    1 -> showAccountSettings()
                    2 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun showEditProfileDialog() {
        lifecycleScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    val dialogView = LayoutInflater.from(this@DashboardActivity).inflate(R.layout.dialog_edit_profile, null)

                    val nameInput = dialogView.findViewById<EditText>(R.id.edit_profile_name)
                    val ageInput = dialogView.findViewById<EditText>(R.id.edit_profile_age)
                    val heightInput = dialogView.findViewById<EditText>(R.id.edit_profile_height)
                    val currentWeightInput = dialogView.findViewById<EditText>(R.id.edit_profile_current_weight)
                    val targetWeightInput = dialogView.findViewById<EditText>(R.id.edit_profile_target_weight)
                    val activitySpinner = dialogView.findViewById<Spinner>(R.id.edit_profile_activity_level)

                    // Populate current values
                    nameInput.setText(currentUser.name)
                    currentUser.age?.let { age -> ageInput.setText(age.toString()) }
                    currentUser.height?.let { height -> heightInput.setText(height.toString()) }
                    currentUser.currentWeight?.let { weight -> currentWeightInput.setText(weight.toString()) }
                    currentUser.targetWeight?.let { target -> targetWeightInput.setText(target.toString()) }

                    // Setup activity level spinner
                    val activityLevels = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Super Active")
                    val spinnerAdapter = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_spinner_item, activityLevels)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    activitySpinner.adapter = spinnerAdapter

                    // Select current activity level
                    val currentIndex = activityLevels.indexOf(currentUser.activityLevel ?: "Moderately Active")
                    if (currentIndex >= 0) activitySpinner.setSelection(currentIndex)

                    AlertDialog.Builder(this@DashboardActivity)
                        .setTitle("Edit Profile")
                        .setView(dialogView)
                        .setPositiveButton("Save") { dialog, which ->
                            saveProfileChanges(
                                nameInput.text.toString().trim(),
                                ageInput.text.toString().toIntOrNull(),
                                heightInput.text.toString().toIntOrNull(),
                                currentWeightInput.text.toString().toDoubleOrNull(),
                                targetWeightInput.text.toString().toDoubleOrNull(),
                                activityLevels[activitySpinner.selectedItemPosition]
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing edit profile dialog: ${e.message}", e)
                Toast.makeText(this@DashboardActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileChanges(name: String, age: Int?, height: Int?, currentWeight: Double?, targetWeight: Double?, activityLevel: String) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = userRepository.updateUserProfile(name, age, height, currentWeight, targetWeight, activityLevel)
                if (result.isSuccess) {
                    Toast.makeText(this@DashboardActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    updateDashboardData() // Refresh the dashboard with new data
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to update profile: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile changes: ${e.message}", e)
                Toast.makeText(this@DashboardActivity, "Error saving changes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAccountSettings() {
        val options = arrayOf(
            "Change Password",
            "Delete Account"
        )

        AlertDialog.Builder(this)
            .setTitle("Account Settings")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showChangePasswordDialog()
                    1 -> showDeleteAccountConfirmation()
                }
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)

        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.current_password_input)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.new_password_input)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirm_password_input)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { dialog, which ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    changePassword(currentPassword, newPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validatePasswordChange(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return when {
            currentPassword.isEmpty() -> {
                Toast.makeText(this, "Please enter current password", Toast.LENGTH_SHORT).show()
                false
            }
            newPassword.length < 6 -> {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            newPassword != confirmPassword -> {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val result = userRepository.changePassword(currentPassword, newPassword)
                if (result.isSuccess) {
                    Toast.makeText(this@DashboardActivity, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to change password: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password: ${e.message}", e)
                Toast.makeText(this@DashboardActivity, "Error changing password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            try {
                val result = userRepository.deleteAccount()
                if (result.isSuccess) {
                    Toast.makeText(this@DashboardActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    // Navigate to sign in
                    val intent = Intent(this@DashboardActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to delete account: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting account: ${e.message}", e)
                Toast.makeText(this@DashboardActivity, "Error deleting account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, which ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        userRepository.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to sign in
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}