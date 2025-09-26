package com.example.madd_assignment_01

import android.app.DatePickerDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class WorkoutRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: WorkoutType,
    val date: Date,
    val duration: Int, // minutes
    val calories: Int,
    val createdAt: Date = Date()
)

enum class WorkoutType(val displayName: String, val color: String) {
    CARDIO("Cardio", "#FF6B6B"),
    STRENGTH("Strength", "#4ECDC4"),
    FLEXIBILITY("Flexibility", "#45B7D1"),
    HIIT("HIIT", "#96CEB4"),
    ALL("All", "#6C5CE7")
}

enum class WorkoutFilter {
    ALL, CARDIO, STRENGTH, FLEXIBILITY, HIIT
}

class WorkoutActivity : AppCompatActivity() {

    // Views
    private lateinit var addWorkoutFab: CardView
    private lateinit var settingsButton: ImageView

    // Category cards
    private lateinit var cardioCategory: CardView
    private lateinit var strengthCategory: CardView
    private lateinit var flexibilityCategory: CardView
    private lateinit var hiitCategory: CardView

    // Filter buttons
    private lateinit var filterAll: CardView
    private lateinit var filterCardio: CardView
    private lateinit var filterStrength: CardView
    private lateinit var filterFlexibility: CardView
    private lateinit var filterHiit: CardView
    private lateinit var filterButton: CardView

    // Featured workout
    private lateinit var startFeaturedWorkout: Button

    // Workout history
    private lateinit var workoutHistoryRecyclerView: RecyclerView
    private lateinit var workoutHistoryAdapter: WorkoutHistoryAdapter

    // Bottom navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navWorkouts: LinearLayout
    private lateinit var navDiet: LinearLayout
    private lateinit var navGoals: LinearLayout
    private lateinit var navReminders: LinearLayout
    private lateinit var navAnalytics: LinearLayout

    // Data
    private val workoutHistory = mutableListOf<WorkoutRecord>()
    private var currentFilter = WorkoutFilter.ALL

    // Dialog
    private var addWorkoutDialog: AlertDialog? = null

    companion object {
        private const val TAG = "WorkoutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_workouts)
            initializeViews()
            setupClickListeners()
            setupRecyclerView()
            loadSampleData()
            updateWorkoutHistory()

            Log.d(TAG, "WorkoutActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading workout screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header
            addWorkoutFab = findViewById(R.id.add_workout_fab)
            settingsButton = findViewById(R.id.settings_button)

            // Category cards
            cardioCategory = findViewById(R.id.cardio_category)
            strengthCategory = findViewById(R.id.strength_category)
            flexibilityCategory = findViewById(R.id.flexibility_category)
            hiitCategory = findViewById(R.id.hiit_category)

            // Filter buttons
            filterAll = findViewById(R.id.filter_all)
            filterCardio = findViewById(R.id.filter_cardio)
            filterStrength = findViewById(R.id.filter_strength)
            filterFlexibility = findViewById(R.id.filter_flexibility)
            filterHiit = findViewById(R.id.filter_hiit)
            filterButton = findViewById(R.id.filter_button)

            // Featured workout
            startFeaturedWorkout = findViewById(R.id.start_featured_workout)

            // Workout history
            workoutHistoryRecyclerView = findViewById(R.id.workout_history_recyclerview)

            // Bottom navigation
            navHome = findViewById(R.id.nav_home)
            navWorkouts = findViewById(R.id.nav_workouts)
            navDiet = findViewById(R.id.nav_diet)
            navGoals = findViewById(R.id.nav_goals)
            navReminders = findViewById(R.id.nav_reminders)
            navAnalytics = findViewById(R.id.nav_analytics)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Add workout FAB
        addWorkoutFab.setOnClickListener {
            Log.d(TAG, "Add workout FAB clicked")
            showAddWorkoutDialog()
        }

        // Settings button
        settingsButton.setOnClickListener {
            Log.d(TAG, "Settings button clicked")
            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Category cards
        cardioCategory.setOnClickListener {
            Log.d(TAG, "Cardio category clicked")
            filterWorkouts(WorkoutFilter.CARDIO)
            updateFilterButtons(WorkoutFilter.CARDIO)
        }

        strengthCategory.setOnClickListener {
            Log.d(TAG, "Strength category clicked")
            filterWorkouts(WorkoutFilter.STRENGTH)
            updateFilterButtons(WorkoutFilter.STRENGTH)
        }

        flexibilityCategory.setOnClickListener {
            Log.d(TAG, "Flexibility category clicked")
            filterWorkouts(WorkoutFilter.FLEXIBILITY)
            updateFilterButtons(WorkoutFilter.FLEXIBILITY)
        }

        hiitCategory.setOnClickListener {
            Log.d(TAG, "HIIT category clicked")
            filterWorkouts(WorkoutFilter.HIIT)
            updateFilterButtons(WorkoutFilter.HIIT)
        }

        // Filter buttons
        filterAll.setOnClickListener {
            filterWorkouts(WorkoutFilter.ALL)
            updateFilterButtons(WorkoutFilter.ALL)
        }

        filterCardio.setOnClickListener {
            filterWorkouts(WorkoutFilter.CARDIO)
            updateFilterButtons(WorkoutFilter.CARDIO)
        }

        filterStrength.setOnClickListener {
            filterWorkouts(WorkoutFilter.STRENGTH)
            updateFilterButtons(WorkoutFilter.STRENGTH)
        }

        filterFlexibility.setOnClickListener {
            filterWorkouts(WorkoutFilter.FLEXIBILITY)
            updateFilterButtons(WorkoutFilter.FLEXIBILITY)
        }

        filterHiit.setOnClickListener {
            filterWorkouts(WorkoutFilter.HIIT)
            updateFilterButtons(WorkoutFilter.HIIT)
        }

        filterButton.setOnClickListener {
            Log.d(TAG, "Filter button clicked")
            showFilterOptions()
        }

        // Featured workout
        startFeaturedWorkout.setOnClickListener {
            Log.d(TAG, "Start featured workout clicked")
            startFeaturedWorkout()
        }

        // Bottom navigation
        navHome.setOnClickListener {
            Log.d(TAG, "Home navigation clicked")
            finish() // Go back to dashboard
        }

        navWorkouts.setOnClickListener {
            Log.d(TAG, "Workouts navigation clicked - already here")
        }

        navDiet.setOnClickListener {
            Log.d(TAG, "Diet navigation clicked")
            val intent = Intent(this, DietActivity::class.java)
            startActivity(intent)
        }

        navGoals.setOnClickListener {
            Log.d(TAG, "Goals navigation clicked")
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        navReminders.setOnClickListener {
            Log.d(TAG, "Reminders navigation clicked")
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }

        navAnalytics.setOnClickListener {
            Log.d(TAG, "Analytics navigation clicked")
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        workoutHistoryAdapter = WorkoutHistoryAdapter(
            workoutHistory,
            onItemClick = { workout ->
                Log.d(TAG, "Workout item clicked: ${workout.name}")
                showWorkoutDetails(workout)
            },
            onOptionsClick = { workout ->
                Log.d(TAG, "Workout options clicked: ${workout.name}")
                showWorkoutOptions(workout)
            }
        )

        workoutHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WorkoutActivity)
            adapter = workoutHistoryAdapter
        }
    }

    private fun loadSampleData() {
        val calendar = Calendar.getInstance()

        // Today
        workoutHistory.add(
            WorkoutRecord(
                name = "Morning Run",
                type = WorkoutType.CARDIO,
                date = calendar.time,
                duration = 30,
                calories = 320
            )
        )

        // Yesterday
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        workoutHistory.add(
            WorkoutRecord(
                name = "Upper Body Strength",
                type = WorkoutType.STRENGTH,
                date = calendar.time,
                duration = 45,
                calories = 280
            )
        )

        // 2 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        workoutHistory.add(
            WorkoutRecord(
                name = "Yoga Session",
                type = WorkoutType.FLEXIBILITY,
                date = calendar.time,
                duration = 60,
                calories = 220
            )
        )

        // 3 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        workoutHistory.add(
            WorkoutRecord(
                name = "Evening Bike Ride",
                type = WorkoutType.CARDIO,
                date = calendar.time,
                duration = 40,
                calories = 350
            )
        )

        Log.d(TAG, "Sample workout data loaded: ${workoutHistory.size} workouts")
    }

    private fun showAddWorkoutDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout, null)

            val workoutNameInput = dialogView.findViewById<EditText>(R.id.workout_name_input)
            val workoutTypeSpinner = dialogView.findViewById<Spinner>(R.id.workout_type_spinner)
            val workoutDateInput = dialogView.findViewById<EditText>(R.id.workout_date_input)
            val workoutDurationInput = dialogView.findViewById<EditText>(R.id.workout_duration_input)
            val workoutCaloriesInput = dialogView.findViewById<EditText>(R.id.workout_calories_input)
            val closeDialog = dialogView.findViewById<ImageView>(R.id.close_dialog)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
            val saveWorkoutButton = dialogView.findViewById<Button>(R.id.save_workout_button)

            // Setup spinner
            val workoutTypes = WorkoutType.values().filter { it != WorkoutType.ALL }
            val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_workout_type, workoutTypes.map { it.displayName })
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_workout_type)
            workoutTypeSpinner.adapter = spinnerAdapter

            // Setup date picker
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            workoutDateInput.setText(dateFormat.format(calendar.time))

            workoutDateInput.setOnClickListener {
                DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        workoutDateInput.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            addWorkoutDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            addWorkoutDialog?.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
                setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
            }

            // Close dialog listeners
            closeDialog.setOnClickListener {
                addWorkoutDialog?.dismiss()
            }

            cancelButton.setOnClickListener {
                addWorkoutDialog?.dismiss()
            }

            // Save workout listener
            saveWorkoutButton.setOnClickListener {
                val name = workoutNameInput.text.toString().trim()
                val selectedType = workoutTypes[workoutTypeSpinner.selectedItemPosition]
                val duration = workoutDurationInput.text.toString().toIntOrNull() ?: 0
                val calories = workoutCaloriesInput.text.toString().toIntOrNull() ?: 0

                if (validateWorkoutInput(name, duration, calories)) {
                    val newWorkout = WorkoutRecord(
                        name = name,
                        type = selectedType,
                        date = calendar.time,
                        duration = duration,
                        calories = calories
                    )

                    addWorkout(newWorkout)
                    addWorkoutDialog?.dismiss()

                    Toast.makeText(this, "Workout added successfully!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "New workout added: $name")
                }
            }

            addWorkoutDialog?.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing add workout dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening add workout dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateWorkoutInput(name: String, duration: Int, calories: Int): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please enter workout name", Toast.LENGTH_SHORT).show()
                false
            }
            duration <= 0 -> {
                Toast.makeText(this, "Please enter valid duration", Toast.LENGTH_SHORT).show()
                false
            }
            calories < 0 -> {
                Toast.makeText(this, "Please enter valid calories", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun addWorkout(workout: WorkoutRecord) {
        workoutHistory.add(0, workout) // Add to beginning
        updateWorkoutHistory()
    }

    private fun filterWorkouts(filter: WorkoutFilter) {
        currentFilter = filter
        updateWorkoutHistory()
        Log.d(TAG, "Filtered workouts by: $filter")
    }

    private fun updateFilterButtons(activeFilter: WorkoutFilter) {
        // Reset all buttons
        resetFilterButton(filterAll)
        resetFilterButton(filterCardio)
        resetFilterButton(filterStrength)
        resetFilterButton(filterFlexibility)
        resetFilterButton(filterHiit)

        // Set active button
        when (activeFilter) {
            WorkoutFilter.ALL -> setActiveFilterButton(filterAll)
            WorkoutFilter.CARDIO -> setActiveFilterButton(filterCardio)
            WorkoutFilter.STRENGTH -> setActiveFilterButton(filterStrength)
            WorkoutFilter.FLEXIBILITY -> setActiveFilterButton(filterFlexibility)
            WorkoutFilter.HIIT -> setActiveFilterButton(filterHiit)
        }
    }

    private fun resetFilterButton(button: CardView) {
        button.setCardBackgroundColor(Color.parseColor("#3A4A5A"))
        val textView = button.findViewById<TextView>(button.id)
        textView?.setTextColor(resources.getColor(R.color.onboarding_text_secondary, null))
    }

    private fun setActiveFilterButton(button: CardView) {
        button.setCardBackgroundColor(resources.getColor(R.color.primary_blue, null))
        val textView = button.findViewById<TextView>(button.id)
        textView?.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun updateWorkoutHistory() {
        val filteredWorkouts = when (currentFilter) {
            WorkoutFilter.ALL -> workoutHistory
            WorkoutFilter.CARDIO -> workoutHistory.filter { it.type == WorkoutType.CARDIO }
            WorkoutFilter.STRENGTH -> workoutHistory.filter { it.type == WorkoutType.STRENGTH }
            WorkoutFilter.FLEXIBILITY -> workoutHistory.filter { it.type == WorkoutType.FLEXIBILITY }
            WorkoutFilter.HIIT -> workoutHistory.filter { it.type == WorkoutType.HIIT }
        }

        workoutHistoryAdapter.updateWorkouts(filteredWorkouts)
    }

    private fun showFilterOptions() {
        val options = arrayOf("All Workouts", "Recent First", "Oldest First", "Highest Calories", "Longest Duration")

        AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        currentFilter = WorkoutFilter.ALL
                        updateFilterButtons(WorkoutFilter.ALL)
                        updateWorkoutHistory()
                    }
                    1 -> sortWorkouts { w1, w2 -> w2.date.compareTo(w1.date) }
                    2 -> sortWorkouts { w1, w2 -> w1.date.compareTo(w2.date) }
                    3 -> sortWorkouts { w1, w2 -> w2.calories.compareTo(w1.calories) }
                    4 -> sortWorkouts { w1, w2 -> w2.duration.compareTo(w1.duration) }
                }
            }
            .show()
    }

    private fun sortWorkouts(comparator: Comparator<WorkoutRecord>) {
        workoutHistory.sortWith(comparator)
        updateWorkoutHistory()
    }

    private fun startFeaturedWorkout() {
        Toast.makeText(this, "Starting HIIT workout session!", Toast.LENGTH_LONG).show()

        // Add featured workout to history
        val featuredWorkout = WorkoutRecord(
            name = "30-min HIIT Session",
            type = WorkoutType.HIIT,
            date = Date(),
            duration = 30,
            calories = 300
        )

        addWorkout(featuredWorkout)
    }

    private fun showWorkoutDetails(workout: WorkoutRecord) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        AlertDialog.Builder(this)
            .setTitle(workout.name)
            .setMessage(
                "Type: ${workout.type.displayName}\n" +
                "Date: ${dateFormat.format(workout.date)}\n" +
                "Duration: ${workout.duration} minutes\n" +
                "Calories Burned: ${workout.calories} kcal"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showWorkoutOptions(workout: WorkoutRecord) {
        val options = arrayOf("View Details", "Edit Workout", "Delete Workout")

        AlertDialog.Builder(this)
            .setTitle("Workout Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showWorkoutDetails(workout)
                    1 -> editWorkout(workout)
                    2 -> deleteWorkout(workout)
                }
            }
            .show()
    }

    private fun editWorkout(workout: WorkoutRecord) {
        Toast.makeText(this, "Edit workout feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteWorkout(workout: WorkoutRecord) {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete '${workout.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                workoutHistory.remove(workout)
                updateWorkoutHistory()
                Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        addWorkoutDialog?.dismiss()
    }
}

class WorkoutHistoryAdapter(
    private var workouts: List<WorkoutRecord>,
    private val onItemClick: (WorkoutRecord) -> Unit,
    private val onOptionsClick: (WorkoutRecord) -> Unit
) : RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val workoutTypeIndicator: View = view.findViewById(R.id.workout_type_indicator)
        val workoutName: TextView = view.findViewById(R.id.workout_name)
        val workoutType: TextView = view.findViewById(R.id.workout_type)
        val workoutDuration: TextView = view.findViewById(R.id.workout_duration)
        val workoutCalories: TextView = view.findViewById(R.id.workout_calories)
        val workoutDate: TextView = view.findViewById(R.id.workout_date)
        val workoutOptions: ImageView = view.findViewById(R.id.workout_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        holder.workoutTypeIndicator.setBackgroundColor(Color.parseColor(workout.type.color))
        holder.workoutName.text = workout.name
        holder.workoutType.text = workout.type.displayName
        holder.workoutDuration.text = "${workout.duration} min"
        holder.workoutCalories.text = "${workout.calories} kcal"

        // Format date
        val dateFormat = when {
            isToday(workout.date) -> "Today"
            isYesterday(workout.date) -> "Yesterday"
            else -> {
                val daysDiff = daysBetween(workout.date, Date()).toInt()
                if (daysDiff < 7) "$daysDiff days ago"
                else SimpleDateFormat("MMM dd", Locale.US).format(workout.date)
            }
        }
        holder.workoutDate.text = dateFormat

        holder.itemView.setOnClickListener { onItemClick(workout) }
        holder.workoutOptions.setOnClickListener { onOptionsClick(workout) }
    }

    override fun getItemCount() = workouts.size

    fun updateWorkouts(newWorkouts: List<WorkoutRecord>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val targetDate = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        val targetDate = Calendar.getInstance().apply { time = date }
        return yesterday.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
               yesterday.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun daysBetween(startDate: Date, endDate: Date): Long {
        val diffInMillies = endDate.time - startDate.time
        return diffInMillies / (1000 * 60 * 60 * 24)
    }
}