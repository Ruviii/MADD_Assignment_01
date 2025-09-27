package com.example.madd_assignment_01

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madd_assignment_01.utils.NavigationUtils
import java.text.SimpleDateFormat
import java.util.*

data class ProgressJourney(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val isCompleted: Boolean = false
)

data class WorkoutAnalytics(
    val day: String,
    val minutes: Int
)

data class CalorieData(
    val day: String,
    val consumed: Int,
    val burned: Int
)

data class MacroData(
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class GoalProgress(
    val title: String,
    val progress: Int,
    val color: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Date? = null
)

enum class TimePeriod(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

class AnalyticsActivity : AppCompatActivity() {

    // Views
    private lateinit var analyticsSettingsButton: ImageView
    private lateinit var timePeriodSpinner: Spinner

    // Progress Journey
    private lateinit var progressJourneyRecyclerView: RecyclerView
    private lateinit var progressJourneyAdapter: ProgressJourneyAdapter

    // Workout Activity Chart
    private lateinit var workoutChartView: LinearLayout

    // Calories Chart
    private lateinit var caloriesChartView: LinearLayout

    // Macronutrient Chart
    private lateinit var proteinProgress: ProgressBar
    private lateinit var carbsProgress: ProgressBar
    private lateinit var fatProgress: ProgressBar
    private lateinit var proteinText: TextView
    private lateinit var carbsText: TextView
    private lateinit var fatText: TextView

    // Goal Progress
    private lateinit var weightLossProgress: ProgressBar
    private lateinit var workoutFrequencyProgress: ProgressBar
    private lateinit var waterIntakeProgress: ProgressBar
    private lateinit var weightLossText: TextView
    private lateinit var workoutFrequencyText: TextView
    private lateinit var waterIntakeText: TextView

    // Transformation Tracking
    private lateinit var transformationCard: CardView
    private lateinit var uploadPhotoButton: Button

    // Achievements
    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter
    private lateinit var viewAllAchievements: TextView

    // Motivational Message
    private lateinit var motivationalMessage: TextView

    // Bottom navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navWorkouts: LinearLayout
    private lateinit var navDiet: LinearLayout
    private lateinit var navGoals: LinearLayout
    private lateinit var navReminders: LinearLayout
    private lateinit var navAnalytics: LinearLayout

    // Data
    private val progressJourneys = mutableListOf<ProgressJourney>()
    private val workoutData = mutableListOf<WorkoutAnalytics>()
    private val calorieData = mutableListOf<CalorieData>()
    private val achievements = mutableListOf<Achievement>()
    private var currentTimePeriod = TimePeriod.WEEK

    companion object {
        private const val TAG = "AnalyticsActivity"
        private val DAYS_OF_WEEK = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_analytics)
            initializeViews()
            setupClickListeners()
            setupRecyclerViews()
            setupSpinner()
            loadAnalyticsData()
            updateCharts()
            updateProgressBars()

            Log.d(TAG, "AnalyticsActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading analytics screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header
            analyticsSettingsButton = findViewById(R.id.analytics_settings_button)
            timePeriodSpinner = findViewById(R.id.time_period_spinner)

            // Progress Journey
            progressJourneyRecyclerView = findViewById(R.id.progress_journey_recyclerview)

            // Charts
            workoutChartView = findViewById(R.id.workout_chart_view)
            caloriesChartView = findViewById(R.id.calories_chart_view)

            // Macronutrients
            proteinProgress = findViewById(R.id.protein_progress_bar)
            carbsProgress = findViewById(R.id.carbs_progress_bar)
            fatProgress = findViewById(R.id.fat_progress_bar)
            proteinText = findViewById(R.id.protein_percentage_text)
            carbsText = findViewById(R.id.carbs_percentage_text)
            fatText = findViewById(R.id.fat_percentage_text)

            // Goal Progress
            weightLossProgress = findViewById(R.id.weight_loss_progress)
            workoutFrequencyProgress = findViewById(R.id.workout_frequency_progress)
            waterIntakeProgress = findViewById(R.id.water_intake_progress)
            weightLossText = findViewById(R.id.weight_loss_text)
            workoutFrequencyText = findViewById(R.id.workout_frequency_text)
            waterIntakeText = findViewById(R.id.water_intake_text)

            // Transformation
            transformationCard = findViewById(R.id.transformation_card)
            uploadPhotoButton = findViewById(R.id.upload_photo_button)

            // Achievements
            achievementsRecyclerView = findViewById(R.id.achievements_recyclerview)
            viewAllAchievements = findViewById(R.id.view_all_achievements)

            // Motivational Message
            motivationalMessage = findViewById(R.id.motivational_message)


            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Settings button
        analyticsSettingsButton.setOnClickListener {
            Log.d(TAG, "Analytics settings button clicked")
            showAnalyticsSettings()
        }

        // Transformation card
        transformationCard.setOnClickListener {
            Log.d(TAG, "Transformation card clicked")
            showTransformationDetails()
        }

        uploadPhotoButton.setOnClickListener {
            Log.d(TAG, "Upload photo button clicked")
            showPhotoUploadOptions()
        }

        // View all achievements
        viewAllAchievements.setOnClickListener {
            Log.d(TAG, "View all achievements clicked")
            showAllAchievements()
        }

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        navHome = findViewById(R.id.nav_home)
        navWorkouts = findViewById(R.id.nav_workouts)
        navDiet = findViewById(R.id.nav_diet)
        navGoals = findViewById(R.id.nav_goals)
        navReminders = findViewById(R.id.nav_reminders)
        navAnalytics = findViewById(R.id.nav_analytics)

        navHome.setOnClickListener {
            NavigationUtils.navigateToHome(this)
        }

        navWorkouts.setOnClickListener {
            NavigationUtils.navigateToWorkouts(this)
        }

        navDiet.setOnClickListener {
            NavigationUtils.navigateToDiet(this)
        }

        navGoals.setOnClickListener {
            NavigationUtils.navigateToGoals(this)
        }

        navReminders.setOnClickListener {
            NavigationUtils.navigateToReminders(this)
        }

        navAnalytics.setOnClickListener {
            // Already on analytics page
        }
    }

    private fun setupSpinner() {
        val timePeriods = TimePeriod.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter

        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentTimePeriod = TimePeriod.values()[position]
                Log.d(TAG, "Time period changed to: ${currentTimePeriod.displayName}")
                updateChartsForTimePeriod()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerViews() {
        // Progress Journey
        progressJourneyAdapter = ProgressJourneyAdapter(progressJourneys) { journey ->
            Log.d(TAG, "Progress journey clicked: ${journey.title}")
            showJourneyDetails(journey)
        }
        progressJourneyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AnalyticsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = progressJourneyAdapter
        }

        // Achievements
        achievementsAdapter = AchievementsAdapter(achievements) { achievement ->
            Log.d(TAG, "Achievement clicked: ${achievement.title}")
            showAchievementDetails(achievement)
        }
        achievementsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AnalyticsActivity, 3)
            adapter = achievementsAdapter
        }
    }

    private fun loadAnalyticsData() {
        // Load Progress Journey
        progressJourneys.addAll(listOf(
            ProgressJourney("First Month", "Your journey started strong with consistent workouts", R.drawable.workout_image, true),
            ProgressJourney("Weight Milestone", "You've lost 5kg of your journey", R.drawable.goals_icon)
        ))

        // Load Workout Data
        workoutData.addAll(listOf(
            WorkoutAnalytics("Mon", 45),
            WorkoutAnalytics("Tue", 30),
            WorkoutAnalytics("Wed", 60),
            WorkoutAnalytics("Thu", 40),
            WorkoutAnalytics("Fri", 50),
            WorkoutAnalytics("Sat", 25),
            WorkoutAnalytics("Sun", 0)
        ))

        // Load Calorie Data
        calorieData.addAll(listOf(
            CalorieData("Mon", 1800, 400),
            CalorieData("Tue", 1950, 350),
            CalorieData("Wed", 1700, 500),
            CalorieData("Thu", 2000, 300),
            CalorieData("Fri", 1850, 450),
            CalorieData("Sat", 2100, 250),
            CalorieData("Sun", 1900, 200)
        ))

        // Load Achievements
        achievements.addAll(listOf(
            Achievement("7day_streak", "7-Day Streak", "Completed workouts for 7 consecutive days", R.drawable.goals_icon, true, Date()),
            Achievement("10_workouts", "10 Workouts", "Completed your first 10 workouts", R.drawable.workout_icon, true, Date()),
            Achievement("hydration_pro", "Hydration Pro", "Met your daily water goal for 30 days", R.drawable.home, true, Date()),
            Achievement("weight_loss", "Weight Loss", "Lost 5kg towards your goal", R.drawable.goals_icon, false),
            Achievement("consistency", "Consistency King", "30 days of consistent workouts", R.drawable.workout_icon, false),
            Achievement("macro_master", "Macro Master", "Perfect macronutrient balance for 14 days", R.drawable.diet_icon, false)
        ))

        progressJourneyAdapter.notifyDataSetChanged()
        achievementsAdapter.notifyDataSetChanged()

        Log.d(TAG, "Analytics data loaded successfully")
    }

    private fun updateCharts() {
        updateWorkoutChart()
        updateCaloriesChart()
    }

    private fun updateWorkoutChart() {
        workoutChartView.removeAllViews()

        val maxMinutes = workoutData.maxOfOrNull { it.minutes } ?: 60
        val chartHeight = 200 // dp

        workoutData.forEach { data ->
            val dayLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val barHeight = if (maxMinutes > 0) (data.minutes.toFloat() / maxMinutes * chartHeight).toInt() else 0

            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(40, barHeight)
                setBackgroundColor(Color.parseColor("#FF7043"))
            }

            val dayLabel = TextView(this).apply {
                text = data.day
                textSize = 10f
                setTextColor(Color.parseColor("#95A5A6"))
                gravity = android.view.Gravity.CENTER
            }

            dayLayout.addView(bar)
            dayLayout.addView(dayLabel)
            workoutChartView.addView(dayLayout)
        }
    }

    private fun updateCaloriesChart() {
        caloriesChartView.removeAllViews()

        val maxCalories = calorieData.maxOfOrNull { maxOf(it.consumed, it.burned) } ?: 2200
        val chartHeight = 150

        calorieData.forEach { data ->
            val dayLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val consumedHeight = (data.consumed.toFloat() / maxCalories * chartHeight).toInt()
            val burnedHeight = (data.burned.toFloat() / maxCalories * chartHeight).toInt()

            val consumedBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(30, consumedHeight)
                setBackgroundColor(Color.parseColor("#42A5F5"))
            }

            val burnedBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(30, burnedHeight)
                setBackgroundColor(Color.parseColor("#FF7043"))
            }

            val barsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.BOTTOM
            }

            val dayLabel = TextView(this).apply {
                text = data.day
                textSize = 10f
                setTextColor(Color.parseColor("#95A5A6"))
                gravity = android.view.Gravity.CENTER
            }

            barsLayout.addView(consumedBar)
            barsLayout.addView(burnedBar)
            dayLayout.addView(barsLayout)
            dayLayout.addView(dayLabel)
            caloriesChartView.addView(dayLayout)
        }
    }

    private fun updateProgressBars() {
        // Macronutrients (from design: Protein 30%, Carbs 44%, Fat 26%)
        proteinProgress.progress = 30
        carbsProgress.progress = 44
        fatProgress.progress = 26

        proteinText.text = "Protein 30%"
        carbsText.text = "Carbs 44%"
        fatText.text = "Fat 26%"

        // Goal Progress (from design)
        weightLossProgress.progress = 68
        workoutFrequencyProgress.progress = 40
        waterIntakeProgress.progress = 85

        weightLossText.text = "Weight Loss - 68%"
        workoutFrequencyText.text = "Workout Frequency - 40%"
        waterIntakeText.text = "Water Intake - 85%"

        // Motivational Message
        motivationalMessage.text = "You're on track!\nYou've been consistent with your workouts and nutrition this week."
    }

    private fun updateChartsForTimePeriod() {
        // In a real app, this would fetch different data based on time period
        when (currentTimePeriod) {
            TimePeriod.WEEK -> {
                // Already loaded with weekly data
                updateCharts()
            }
            TimePeriod.MONTH -> {
                // Would load monthly aggregated data
                Toast.makeText(this, "Loading monthly analytics...", Toast.LENGTH_SHORT).show()
            }
            TimePeriod.YEAR -> {
                // Would load yearly aggregated data
                Toast.makeText(this, "Loading yearly analytics...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showJourneyDetails(journey: ProgressJourney) {
        AlertDialog.Builder(this)
            .setTitle(journey.title)
            .setMessage(journey.subtitle + "\n\n" + if (journey.isCompleted) "✅ Completed" else "🏃‍♂️ In Progress")
            .setPositiveButton("Continue Journey") { _, _ ->
                Toast.makeText(this, "Keep up the great work!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAchievementDetails(achievement: Achievement) {
        val status = if (achievement.isUnlocked) "🏆 Unlocked" else "🔒 Locked"
        val message = "${achievement.description}\n\nStatus: $status"

        AlertDialog.Builder(this)
            .setTitle(achievement.title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAllAchievements() {
        val unlockedCount = achievements.count { it.isUnlocked }
        val totalCount = achievements.size

        AlertDialog.Builder(this)
            .setTitle("All Achievements")
            .setMessage("You've unlocked $unlockedCount out of $totalCount achievements!\n\nKeep pushing towards your fitness goals to unlock more.")
            .setPositiveButton("View Details") { _, _ ->
                Toast.makeText(this, "Detailed achievements view coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showTransformationDetails() {
        AlertDialog.Builder(this)
            .setTitle("Track Your Transformation")
            .setMessage("Upload progress photos to visualize your fitness journey over time.")
            .setPositiveButton("Upload Photo") { _, _ ->
                showPhotoUploadOptions()
            }
            .setNegativeButton("View Gallery") { _, _ ->
                Toast.makeText(this, "Photo gallery coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun showPhotoUploadOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Upload Progress Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Camera functionality coming soon!", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Gallery selection coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showAnalyticsSettings() {
        val options = arrayOf(
            "Export Analytics Data",
            "Reset Progress Data",
            "Privacy Settings",
            "Data Sync Settings",
            "Notification Preferences"
        )

        AlertDialog.Builder(this)
            .setTitle("Analytics Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAnalyticsData()
                    1 -> showResetDataConfirmation()
                    2 -> showPrivacySettings()
                    3 -> showDataSyncSettings()
                    4 -> showNotificationPreferences()
                }
            }
            .show()
    }

    private fun exportAnalyticsData() {
        Toast.makeText(this, "Exporting analytics data...", Toast.LENGTH_SHORT).show()
    }

    private fun showResetDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Reset Progress Data")
            .setMessage("Are you sure you want to reset all progress data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                Toast.makeText(this, "Progress data reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPrivacySettings() {
        Toast.makeText(this, "Privacy settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showDataSyncSettings() {
        Toast.makeText(this, "Data sync settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationPreferences() {
        Toast.makeText(this, "Notification preferences coming soon!", Toast.LENGTH_SHORT).show()
    }
}

// Adapter Classes
class ProgressJourneyAdapter(
    private var journeys: List<ProgressJourney>,
    private val onJourneyClick: (ProgressJourney) -> Unit
) : RecyclerView.Adapter<ProgressJourneyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val journeyImage: ImageView = view.findViewById(R.id.journey_image)
        val journeyTitle: TextView = view.findViewById(R.id.journey_title)
        val journeySubtitle: TextView = view.findViewById(R.id.journey_subtitle)
        val completedBadge: View = view.findViewById(R.id.completed_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress_journey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val journey = journeys[position]

        holder.journeyImage.setImageResource(journey.imageRes)
        holder.journeyTitle.text = journey.title
        holder.journeySubtitle.text = journey.subtitle
        holder.completedBadge.visibility = if (journey.isCompleted) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onJourneyClick(journey)
        }
    }

    override fun getItemCount() = journeys.size
}

class AchievementsAdapter(
    private var achievements: List<Achievement>,
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val achievementIcon: ImageView = view.findViewById(R.id.achievement_icon)
        val achievementTitle: TextView = view.findViewById(R.id.achievement_title)
        val lockedOverlay: View = view.findViewById(R.id.locked_overlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]

        holder.achievementIcon.setImageResource(achievement.iconRes)
        holder.achievementTitle.text = achievement.title
        holder.lockedOverlay.visibility = if (achievement.isUnlocked) View.GONE else View.VISIBLE

        val alpha = if (achievement.isUnlocked) 1.0f else 0.5f
        holder.itemView.alpha = alpha

        holder.itemView.setOnClickListener {
            onAchievementClick(achievement)
        }
    }

    override fun getItemCount() = achievements.size
}