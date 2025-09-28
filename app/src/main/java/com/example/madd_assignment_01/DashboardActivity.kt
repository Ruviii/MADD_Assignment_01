package com.example.madd_assignment_01

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.madd_assignment_01.data.DataManager
import com.example.madd_assignment_01.data.DatabaseDataManager
import com.example.madd_assignment_01.repository.UserRepository
import com.example.madd_assignment_01.repository.WorkoutRepository
import com.example.madd_assignment_01.repository.NutritionRepository
import com.example.madd_assignment_01.repository.GoalRepository
import com.example.madd_assignment_01.repository.ReminderRepository
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

    // Progress bars
    private lateinit var weightLossProgressBar: ProgressBar
    private lateinit var workoutProgressBar: ProgressBar
    private lateinit var waterIntakeProgressBar: ProgressBar

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

    // Data Manager and Repositories
    private lateinit var dataManager: DataManager
    private lateinit var databaseDataManager: DatabaseDataManager
    private lateinit var userRepository: UserRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var nutritionRepository: NutritionRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var reminderRepository: ReminderRepository

    // Image handling
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            updateProfileImagePreview(it)
        }
    }

    companion object {
        private const val TAG = "DashboardActivity"
        private const val READ_EXTERNAL_STORAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_dashboard)

            // Check if user is logged in
            val app = application as HealthFitnessApplication
            userRepository = app.userRepository
            workoutRepository = app.workoutRepository
            nutritionRepository = app.nutritionRepository
            goalRepository = app.goalRepository
            reminderRepository = app.reminderRepository

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

    override fun onResume() {
        super.onResume()
        // Refresh dashboard data when user returns to the screen
        updateDashboardData()
        Log.d(TAG, "Dashboard refreshed on resume")
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

            // Progress bars
            weightLossProgressBar = findViewById(R.id.weight_loss_progress_bar)
            workoutProgressBar = findViewById(R.id.workout_progress_bar)
            waterIntakeProgressBar = findViewById(R.id.water_intake_progress_bar)

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
        lifecycleScope.launch {
            try {
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

                // Random motivational quotes
                val quotes = listOf(
                    "\"One workout at a time, one meal at a time, one day at a time.\"",
                    "\"Progress, not perfection.\"",
                    "\"Your body can do it. It's your mind you have to convince.\"",
                    "\"Stronger than yesterday.\"",
                    "\"Every step counts towards your goal.\""
                )
                motivationQuoteTextView.text = quotes.random()

                // Load real data from repositories
                loadTodaysSummaryData()
                loadProgressData()
                loadFeaturedWorkout()

                Log.d(TAG, "Dashboard data updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating dashboard data: ${e.message}", e)
                // Fall back to default values
                setDefaultValues()
            }
        }
    }

    private suspend fun loadTodaysSummaryData() {
        try {
            // Get today's meals and calculate total calories
            val todaysMeals = try {
                nutritionRepository.getTodaysMeals()
            } catch (e: Exception) {
                Log.w(TAG, "Could not load today's meals, using fallback", e)
                emptyList()
            }
            val todaysCalories = todaysMeals.sumOf { meal ->
                meal.foodItems.sumOf { foodItem ->
                    foodItem.totalCalories
                }
            }

            // Get today's workouts
            val todaysWorkouts = try {
                workoutRepository.getTodaysWorkouts()
            } catch (e: Exception) {
                Log.w(TAG, "Could not load today's workouts, using fallback", e)
                emptyList()
            }
            val workoutCount = todaysWorkouts.size

            // Get today's water intake (placeholder - implement when water tracking is added)
            val waterGlasses = 6 // TODO: Implement water tracking from goals/habits

            // Update UI
            runOnUiThread {
                caloriesValueTextView.text = todaysCalories.toString()
                workoutsValueTextView.text = workoutCount.toString()
                waterValueTextView.text = waterGlasses.toString()
            }

            Log.d(TAG, "Today's summary: $todaysCalories cal, $workoutCount workouts, $waterGlasses glasses")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading today's summary: ${e.message}", e)
            runOnUiThread {
                caloriesValueTextView.text = "0"
                workoutsValueTextView.text = "0"
                waterValueTextView.text = "0"
            }
        }
    }

    private suspend fun loadProgressData() {
        try {
            // Calculate weekly progress
            val weeklyWorkouts = try {
                workoutRepository.getWeeklyWorkouts()
            } catch (e: Exception) {
                Log.w(TAG, "Could not load weekly workouts, using fallback", e)
                emptyList()
            }
            val weeklyCaloriesBurned = weeklyWorkouts.sumOf { it.calories }
            val weeklyGoal = 2000 // Example weekly calorie burn goal
            val weeklyProgress = if (weeklyGoal > 0) (weeklyCaloriesBurned * 100 / weeklyGoal).coerceAtMost(100) else 0

            // Get active goals from database
            val activeGoals = try {
                goalRepository.getActiveGoalsSync()
            } catch (e: Exception) {
                Log.w(TAG, "Could not load active goals, using fallback", e)
                emptyList()
            }
            val weightGoal = activeGoals.find { it.category == "WEIGHT" }
            val workoutGoal = activeGoals.find { it.category == "ACTIVITY" }
            val hydrationGoal = activeGoals.find { it.category == "HYDRATION" }

            // Calculate goal progress percentages with real data
            val weightProgress = if (weightGoal != null && weightGoal.targetNumericValue > 0) {
                // Calculate real progress percentage
                val progress = ((weightGoal.currentNumericValue / weightGoal.targetNumericValue) * 100).toInt()
                progress.coerceAtMost(100).coerceAtLeast(0)
            } else {
                // No weight goal found, calculate based on weekly workout progress
                (weeklyProgress * 0.75).toInt() // 75% of weekly progress as weight progress
            }

            val workoutProgress = if (workoutGoal != null && workoutGoal.targetNumericValue > 0) {
                // Calculate real progress percentage
                val progress = ((workoutGoal.currentNumericValue / workoutGoal.targetNumericValue) * 100).toInt()
                progress.coerceAtMost(100).coerceAtLeast(0)
            } else {
                // No workout goal found, calculate based on weekly workouts
                val weeklyWorkoutTarget = 5 // 5 workouts per week
                val actualWorkouts = weeklyWorkouts.size
                ((actualWorkouts.toDouble() / weeklyWorkoutTarget) * 100).toInt().coerceAtMost(100)
            }

            val waterProgress = if (hydrationGoal != null && hydrationGoal.targetNumericValue > 0) {
                // Calculate real progress percentage
                val progress = ((hydrationGoal.currentNumericValue / hydrationGoal.targetNumericValue) * 100).toInt()
                progress.coerceAtMost(100).coerceAtLeast(0)
            } else {
                // No hydration goal found, use daily water intake estimation
                val dailyWaterIntake = try {
                    nutritionRepository.getDailyWaterIntake(java.util.Date())
                } catch (e: Exception) {
                    0.0
                }
                val dailyWaterTarget = 2.0 // 2 liters per day
                ((dailyWaterIntake / dailyWaterTarget) * 100).toInt().coerceAtMost(100).coerceAtLeast(0)
            }

            // Update UI
            runOnUiThread {
                weekProgressTextView.text = "Keep pushing! You're $weeklyProgress% towards your weekly goal"
                weightLossProgressTextView.text = "$weightProgress%"
                workoutProgressTextView.text = "$workoutProgress%"
                waterIntakeProgressTextView.text = "$waterProgress%"

                // Update progress bars
                weightLossProgressBar.progress = weightProgress
                workoutProgressBar.progress = workoutProgress
                waterIntakeProgressBar.progress = waterProgress
            }

            Log.d(TAG, "Progress data: Weekly: $weeklyProgress%, Weight: $weightProgress%, Workout: $workoutProgress%, Water: $waterProgress%")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading progress data: ${e.message}", e)
            runOnUiThread {
                weekProgressTextView.text = "Keep pushing! You're on track."
                weightLossProgressTextView.text = "0%"
                workoutProgressTextView.text = "0%"
                waterIntakeProgressTextView.text = "0%"

                // Update progress bars to 0
                weightLossProgressBar.progress = 0
                workoutProgressBar.progress = 0
                waterIntakeProgressBar.progress = 0
            }
        }
    }

    private suspend fun loadFeaturedWorkout() {
        try {
            // Get recent workouts to recommend similar ones, or use predefined list
            val recentWorkouts = try {
                workoutRepository.getRecentWorkouts(limit = 5)
            } catch (e: Exception) {
                Log.w(TAG, "Could not load recent workouts, using fallback", e)
                emptyList()
            }

            val featuredWorkouts = if (recentWorkouts.isNotEmpty()) {
                // Create personalized recommendations based on recent workout types
                val workoutTypes = recentWorkouts.map { it.type.name }.distinct()
                createPersonalizedWorkouts(workoutTypes)
            } else {
                // Default featured workouts for new users
                listOf(
                    "30-min HIIT Session: Burn 300+ calories",
                    "45-min Strength Training: Build muscle",
                    "60-min Yoga Flow: Improve flexibility",
                    "20-min Core Blast: Strengthen your core",
                    "25-min Cardio Blast: Heart-pumping workout",
                    "40-min Full Body: Complete workout routine"
                )
            }

            val randomWorkout = featuredWorkouts.random()

            runOnUiThread {
                featuredWorkoutTitleTextView.text = "Today's Featured Workout"
                featuredWorkoutDescTextView.text = randomWorkout
            }

            Log.d(TAG, "Featured workout: $randomWorkout")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading featured workout: ${e.message}", e)
            runOnUiThread {
                featuredWorkoutTitleTextView.text = "Today's Featured Workout"
                featuredWorkoutDescTextView.text = "30-min HIIT Session: Burn 300+ calories"
            }
        }
    }

    private fun createPersonalizedWorkouts(workoutTypes: List<String>): List<String> {
        val recommendations = mutableListOf<String>()

        workoutTypes.forEach { type ->
            when (type.uppercase()) {
                "CARDIO" -> {
                    recommendations.addAll(listOf(
                        "35-min Cardio Intervals: Boost endurance",
                        "20-min HIIT Cardio: Maximum burn",
                        "40-min Steady State: Build stamina"
                    ))
                }
                "STRENGTH" -> {
                    recommendations.addAll(listOf(
                        "45-min Upper Body: Build arm strength",
                        "40-min Lower Body: Leg day workout",
                        "50-min Full Body: Complete strength training"
                    ))
                }
                "FLEXIBILITY" -> {
                    recommendations.addAll(listOf(
                        "30-min Yoga Flow: Increase flexibility",
                        "20-min Stretching: Recovery session",
                        "25-min Pilates: Core and flexibility"
                    ))
                }
                else -> {
                    recommendations.addAll(listOf(
                        "30-min Mixed Training: Variety workout",
                        "25-min Functional Fitness: Real-world strength"
                    ))
                }
            }
        }

        return if (recommendations.isNotEmpty()) recommendations.distinct() else listOf(
            "30-min HIIT Session: Burn 300+ calories",
            "45-min Strength Training: Build muscle"
        )
    }

    private fun setDefaultValues() {
        runOnUiThread {
            caloriesValueTextView.text = "0"
            workoutsValueTextView.text = "0"
            waterValueTextView.text = "0"
            weekProgressTextView.text = "Start your fitness journey today!"
            weightLossProgressTextView.text = "0%"
            workoutProgressTextView.text = "0%"
            waterIntakeProgressTextView.text = "0%"

            // Set default progress bars
            weightLossProgressBar.progress = 0
            workoutProgressBar.progress = 0
            waterIntakeProgressBar.progress = 0

            featuredWorkoutTitleTextView.text = "Today's Featured Workout"
            featuredWorkoutDescTextView.text = "30-min HIIT Session: Burn 300+ calories"
        }
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

                    // Get all input fields
                    val profileImagePreview = dialogView.findViewById<ImageView>(R.id.profile_image_preview)
                    val selectImageButton = dialogView.findViewById<Button>(R.id.select_image_button)
                    val removeImageButton = dialogView.findViewById<Button>(R.id.remove_image_button)
                    val nameInput = dialogView.findViewById<EditText>(R.id.edit_profile_name)
                    val ageInput = dialogView.findViewById<EditText>(R.id.edit_profile_age)
                    val heightInput = dialogView.findViewById<EditText>(R.id.edit_profile_height)
                    val currentWeightInput = dialogView.findViewById<EditText>(R.id.edit_profile_current_weight)
                    val targetWeightInput = dialogView.findViewById<EditText>(R.id.edit_profile_target_weight)
                    val phoneInput = dialogView.findViewById<EditText>(R.id.edit_profile_phone)
                    val dateOfBirthInput = dialogView.findViewById<EditText>(R.id.edit_profile_date_of_birth)
                    val genderGroup = dialogView.findViewById<RadioGroup>(R.id.edit_profile_gender_group)
                    val genderMale = dialogView.findViewById<RadioButton>(R.id.edit_profile_gender_male)
                    val genderFemale = dialogView.findViewById<RadioButton>(R.id.edit_profile_gender_female)
                    val genderOther = dialogView.findViewById<RadioButton>(R.id.edit_profile_gender_other)
                    val fitnessGoalInput = dialogView.findViewById<EditText>(R.id.edit_profile_fitness_goal)
                    val activitySpinner = dialogView.findViewById<Spinner>(R.id.edit_profile_activity_level)

                    // Reset selected image
                    selectedImageUri = null

                    // Populate current values
                    nameInput.setText(currentUser.name)
                    currentUser.age?.let { age -> ageInput.setText(age.toString()) }
                    currentUser.height?.let { height -> heightInput.setText(height.toString()) }
                    currentUser.currentWeight?.let { weight -> currentWeightInput.setText(weight.toString()) }
                    currentUser.targetWeight?.let { target -> targetWeightInput.setText(target.toString()) }
                    currentUser.phoneNumber?.let { phone -> phoneInput.setText(phone) }
                    currentUser.dateOfBirth?.let { dob -> dateOfBirthInput.setText(dob) }
                    currentUser.fitnessGoal?.let { goal -> fitnessGoalInput.setText(goal) }

                    // Load current profile image
                    if (!currentUser.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@DashboardActivity)
                            .load(currentUser.profileImageUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(profileImagePreview)
                    }

                    // Set gender selection
                    when (currentUser.gender) {
                        "Male" -> genderMale.isChecked = true
                        "Female" -> genderFemale.isChecked = true
                        "Other" -> genderOther.isChecked = true
                    }

                    // Setup activity level spinner
                    val activityLevels = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Super Active")
                    val spinnerAdapter = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_spinner_item, activityLevels)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    activitySpinner.adapter = spinnerAdapter

                    // Select current activity level
                    val currentIndex = activityLevels.indexOf(currentUser.activityLevel ?: "Moderately Active")
                    if (currentIndex >= 0) activitySpinner.setSelection(currentIndex)

                    // Setup image selection
                    selectImageButton.setOnClickListener {
                        if (ContextCompat.checkSelfPermission(this@DashboardActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this@DashboardActivity,
                                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                                READ_EXTERNAL_STORAGE_REQUEST)
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    }

                    removeImageButton.setOnClickListener {
                        selectedImageUri = null
                        profileImagePreview.setImageResource(R.drawable.default_profile)
                    }

                    // Setup date picker
                    dateOfBirthInput.setOnClickListener {
                        showDatePicker(dateOfBirthInput)
                    }

                    val dialog = AlertDialog.Builder(this@DashboardActivity)
                        .setTitle("Edit Profile")
                        .setView(dialogView)
                        .setPositiveButton("Save") { dialog, which ->
                            val selectedGender = when (genderGroup.checkedRadioButtonId) {
                                R.id.edit_profile_gender_male -> "Male"
                                R.id.edit_profile_gender_female -> "Female"
                                R.id.edit_profile_gender_other -> "Other"
                                else -> null
                            }

                            saveEnhancedProfileChanges(
                                nameInput.text.toString().trim(),
                                ageInput.text.toString().toIntOrNull(),
                                heightInput.text.toString().toIntOrNull(),
                                currentWeightInput.text.toString().toDoubleOrNull(),
                                targetWeightInput.text.toString().toDoubleOrNull(),
                                activityLevels[activitySpinner.selectedItemPosition],
                                phoneInput.text.toString().trim().takeIf { it.isNotEmpty() },
                                dateOfBirthInput.text.toString().trim().takeIf { it.isNotEmpty() },
                                selectedGender,
                                fitnessGoalInput.text.toString().trim().takeIf { it.isNotEmpty() },
                                currentUser.profileImageUrl
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .create()

                    dialog.show()
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

    private fun saveEnhancedProfileChanges(
        name: String,
        age: Int?,
        height: Int?,
        currentWeight: Double?,
        targetWeight: Double?,
        activityLevel: String,
        phoneNumber: String?,
        dateOfBirth: String?,
        gender: String?,
        fitnessGoal: String?,
        currentImageUrl: String?
    ) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Handle image upload if new image is selected
                val finalImageUrl = if (selectedImageUri != null) {
                    saveImageToInternalStorage(selectedImageUri!!)
                } else {
                    currentImageUrl
                }

                val result = userRepository.updateUserProfile(
                    name = name,
                    age = age,
                    height = height,
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    activityLevel = activityLevel,
                    profileImageUrl = finalImageUrl,
                    phoneNumber = phoneNumber,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    fitnessGoal = fitnessGoal
                )

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

    private fun showDatePicker(dateInput: EditText) {
        val calendar = Calendar.getInstance()

        // Try to parse existing date
        val currentText = dateInput.text.toString()
        if (currentText.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val existingDate = dateFormat.parse(currentText)
                existingDate?.let { calendar.time = it }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                dateInput.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateProfileImagePreview(uri: Uri) {
        // This method will be called by the lambda in the image picker
        // We'll update the preview in the dialog if it's open
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image: ${e.message}", e)
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT).show()
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