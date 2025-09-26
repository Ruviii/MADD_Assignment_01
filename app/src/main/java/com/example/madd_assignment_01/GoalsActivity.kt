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

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: GoalCategory,
    val currentValue: String,
    val targetValue: String,
    val currentNumericValue: Double = 0.0,
    val targetNumericValue: Double = 100.0,
    val deadline: Date,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val completedAt: Date? = null
) {
    val progressPercentage: Int
        get() = if (targetNumericValue > 0) {
            val progress = when (category) {
                GoalCategory.WEIGHT -> {
                    // For weight loss, progress is based on reduction
                    val totalToLose = Math.abs(targetNumericValue - if (currentNumericValue > targetNumericValue) currentNumericValue else targetNumericValue)
                    val actualLost = Math.abs(currentNumericValue - if (currentNumericValue > targetNumericValue) currentNumericValue else targetNumericValue)
                    if (totalToLose > 0) (actualLost / totalToLose * 100).toInt() else 0
                }
                else -> {
                    // For other categories, progress is based on achievement
                    ((currentNumericValue / targetNumericValue) * 100).toInt()
                }
            }
            progress.coerceIn(0, 100)
        } else 0

    val daysRemaining: Long
        get() = ((deadline.time - Date().time) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    val isOverdue: Boolean
        get() = !isCompleted && Date().after(deadline)
}

enum class GoalCategory(
    val displayName: String,
    val color: String,
    val unit: String = ""
) {
    WEIGHT("Weight", "#4ECDC4", "kg"),
    CARDIO("Cardio", "#FF9500", "km"),
    STRENGTH("Strength", "#45B7D1", "reps"),
    NUTRITION("Nutrition", "#96CEB4", "kcal"),
    HYDRATION("Hydration", "#00C851", "L"),
    ACTIVITY("Activity", "#8E44AD", "times")
}

class GoalsActivity : AppCompatActivity() {

    // Views
    private lateinit var addGoalFab: CardView
    private lateinit var addGoalButtonCard: CardView

    // RecyclerViews
    private lateinit var activeGoalsRecyclerView: RecyclerView
    private lateinit var completedGoalsRecyclerView: RecyclerView
    private lateinit var activeGoalsAdapter: ActiveGoalsAdapter
    private lateinit var completedGoalsAdapter: CompletedGoalsAdapter

    // Bottom navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navWorkouts: LinearLayout
    private lateinit var navDiet: LinearLayout
    private lateinit var navGoals: LinearLayout
    private lateinit var navReminders: LinearLayout
    private lateinit var navAnalytics: LinearLayout

    // Data
    private val activeGoals = mutableListOf<Goal>()
    private val completedGoals = mutableListOf<Goal>()

    // Dialog
    private var addGoalDialog: AlertDialog? = null
    private var selectedCategory: GoalCategory = GoalCategory.WEIGHT

    companion object {
        private const val TAG = "GoalsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_goals)
            initializeViews()
            setupClickListeners()
            setupRecyclerViews()
            loadSampleGoals()

            Log.d(TAG, "GoalsActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading goals screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header
            addGoalFab = findViewById(R.id.add_goal_fab)
            addGoalButtonCard = findViewById(R.id.add_goal_button_card)

            // RecyclerViews
            activeGoalsRecyclerView = findViewById(R.id.active_goals_recyclerview)
            completedGoalsRecyclerView = findViewById(R.id.completed_goals_recyclerview)

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
        // Add goal buttons
        addGoalFab.setOnClickListener {
            Log.d(TAG, "Add goal FAB clicked")
            showAddGoalDialog()
        }

        addGoalButtonCard.setOnClickListener {
            Log.d(TAG, "Add goal button card clicked")
            showAddGoalDialog()
        }

        // Bottom navigation
        navHome.setOnClickListener {
            Log.d(TAG, "Home navigation clicked")
            finish() // Go back to dashboard
        }

        navWorkouts.setOnClickListener {
            Log.d(TAG, "Workouts navigation clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        navDiet.setOnClickListener {
            Log.d(TAG, "Diet navigation clicked")
            val intent = Intent(this, DietActivity::class.java)
            startActivity(intent)
        }

        navGoals.setOnClickListener {
            Log.d(TAG, "Goals navigation clicked - already here")
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

    private fun setupRecyclerViews() {
        // Active goals adapter
        activeGoalsAdapter = ActiveGoalsAdapter(
            goals = activeGoals,
            onUpdateProgress = { goal ->
                Log.d(TAG, "Update progress for goal: ${goal.name}")
                showUpdateProgressDialog(goal)
            },
            onCompleteGoal = { goal ->
                Log.d(TAG, "Complete goal: ${goal.name}")
                completeGoal(goal)
            },
            onGoalClick = { goal ->
                Log.d(TAG, "Goal clicked: ${goal.name}")
                showGoalDetails(goal)
            }
        )

        activeGoalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GoalsActivity)
            adapter = activeGoalsAdapter
        }

        // Completed goals adapter
        completedGoalsAdapter = CompletedGoalsAdapter(
            goals = completedGoals,
            onGoalClick = { goal ->
                Log.d(TAG, "Completed goal clicked: ${goal.name}")
                showGoalDetails(goal)
            }
        )

        completedGoalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GoalsActivity)
            adapter = completedGoalsAdapter
        }
    }

    private fun loadSampleGoals() {
        val calendar = Calendar.getInstance()

        // Active goals
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        activeGoals.add(
            Goal(
                name = "Lose 5kg",
                category = GoalCategory.WEIGHT,
                currentValue = "73.5kg",
                targetValue = "70kg",
                currentNumericValue = 73.5,
                targetNumericValue = 70.0,
                deadline = calendar.time
            )
        )

        calendar.add(Calendar.DAY_OF_MONTH, -15)
        activeGoals.add(
            Goal(
                name = "Run 5km",
                category = GoalCategory.CARDIO,
                currentValue = "3.2km",
                targetValue = "5km",
                currentNumericValue = 3.2,
                targetNumericValue = 5.0,
                deadline = calendar.time
            )
        )

        calendar.add(Calendar.DAY_OF_MONTH, 60)
        activeGoals.add(
            Goal(
                name = "Drink 2L water daily",
                category = GoalCategory.HYDRATION,
                currentValue = "1.5L avg",
                targetValue = "2L daily",
                currentNumericValue = 1.5,
                targetNumericValue = 2.0,
                deadline = calendar.time
            )
        )

        // Completed goals
        completedGoals.add(
            Goal(
                name = "Complete 10 workouts",
                category = GoalCategory.ACTIVITY,
                currentValue = "10",
                targetValue = "10",
                currentNumericValue = 10.0,
                targetNumericValue = 10.0,
                deadline = Date(),
                isCompleted = true,
                completedAt = Date()
            )
        )

        activeGoalsAdapter.notifyDataSetChanged()
        completedGoalsAdapter.notifyDataSetChanged()

        Log.d(TAG, "Sample goals loaded: ${activeGoals.size} active, ${completedGoals.size} completed")
    }

    private fun showAddGoalDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null)

            val goalNameInput = dialogView.findViewById<EditText>(R.id.goal_name_input)
            val currentValueInput = dialogView.findViewById<EditText>(R.id.current_value_input)
            val targetValueInput = dialogView.findViewById<EditText>(R.id.target_value_input)
            val goalDeadlineInput = dialogView.findViewById<EditText>(R.id.goal_deadline_input)

            // Category selection views
            val categoryWeight = dialogView.findViewById<CardView>(R.id.category_weight)
            val categoryCardio = dialogView.findViewById<CardView>(R.id.category_cardio)
            val categoryStrength = dialogView.findViewById<CardView>(R.id.category_strength)
            val categoryNutrition = dialogView.findViewById<CardView>(R.id.category_nutrition)
            val categoryHydration = dialogView.findViewById<CardView>(R.id.category_hydration)
            val categoryActivity = dialogView.findViewById<CardView>(R.id.category_activity)

            val weightIndicator = dialogView.findViewById<View>(R.id.weight_indicator)
            val cardioIndicator = dialogView.findViewById<View>(R.id.cardio_indicator)
            val strengthIndicator = dialogView.findViewById<View>(R.id.strength_indicator)
            val nutritionIndicator = dialogView.findViewById<View>(R.id.nutrition_indicator)
            val hydrationIndicator = dialogView.findViewById<View>(R.id.hydration_indicator)
            val activityIndicator = dialogView.findViewById<View>(R.id.activity_indicator)

            val closeGoalDialog = dialogView.findViewById<ImageView>(R.id.close_goal_dialog)
            val cancelGoalButton = dialogView.findViewById<Button>(R.id.cancel_goal_button)
            val addGoalButton = dialogView.findViewById<Button>(R.id.add_goal_button)

            val categoryViews = mapOf(
                GoalCategory.WEIGHT to Pair(categoryWeight, weightIndicator),
                GoalCategory.CARDIO to Pair(categoryCardio, cardioIndicator),
                GoalCategory.STRENGTH to Pair(categoryStrength, strengthIndicator),
                GoalCategory.NUTRITION to Pair(categoryNutrition, nutritionIndicator),
                GoalCategory.HYDRATION to Pair(categoryHydration, hydrationIndicator),
                GoalCategory.ACTIVITY to Pair(categoryActivity, activityIndicator)
            )

            // Setup category selection
            fun updateCategorySelection(category: GoalCategory) {
                selectedCategory = category
                categoryViews.forEach { (cat, views) ->
                    val (card, indicator) = views
                    if (cat == category) {
                        indicator.setBackgroundResource(R.drawable.indicator_active)
                    } else {
                        indicator.setBackgroundResource(R.drawable.indicator_inactive)
                    }
                }

                // Update placeholder text based on category
                currentValueInput.hint = "e.g. 75${category.unit}"
                targetValueInput.hint = "e.g. 70${category.unit}"
            }

            // Set default category
            updateCategorySelection(GoalCategory.WEIGHT)

            // Category click listeners
            categoryViews.forEach { (category, views) ->
                val (card, _) = views
                card.setOnClickListener {
                    updateCategorySelection(category)
                }
            }

            // Setup date picker
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            calendar.add(Calendar.DAY_OF_MONTH, 30) // Default to 30 days from now
            goalDeadlineInput.setText(dateFormat.format(calendar.time))

            goalDeadlineInput.setOnClickListener {
                DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        goalDeadlineInput.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            addGoalDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            addGoalDialog?.window?.apply {
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
            closeGoalDialog.setOnClickListener {
                addGoalDialog?.dismiss()
            }

            cancelGoalButton.setOnClickListener {
                addGoalDialog?.dismiss()
            }

            // Add goal listener
            addGoalButton.setOnClickListener {
                val name = goalNameInput.text.toString().trim()
                val currentValue = currentValueInput.text.toString().trim()
                val targetValue = targetValueInput.text.toString().trim()

                if (validateGoalInput(name, currentValue, targetValue)) {
                    val currentNumeric = extractNumericValue(currentValue)
                    val targetNumeric = extractNumericValue(targetValue)

                    val newGoal = Goal(
                        name = name,
                        category = selectedCategory,
                        currentValue = currentValue,
                        targetValue = targetValue,
                        currentNumericValue = currentNumeric,
                        targetNumericValue = targetNumeric,
                        deadline = calendar.time
                    )

                    addGoal(newGoal)
                    addGoalDialog?.dismiss()

                    Toast.makeText(this, "Goal added successfully!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "New goal added: $name")
                }
            }

            addGoalDialog?.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing add goal dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening add goal dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractNumericValue(value: String): Double {
        return try {
            value.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun validateGoalInput(name: String, currentValue: String, targetValue: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please enter goal name", Toast.LENGTH_SHORT).show()
                false
            }
            currentValue.isEmpty() -> {
                Toast.makeText(this, "Please enter current value", Toast.LENGTH_SHORT).show()
                false
            }
            targetValue.isEmpty() -> {
                Toast.makeText(this, "Please enter target value", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun addGoal(goal: Goal) {
        activeGoals.add(0, goal) // Add to beginning
        activeGoalsAdapter.notifyItemInserted(0)
    }

    private fun completeGoal(goal: Goal) {
        AlertDialog.Builder(this)
            .setTitle("Complete Goal")
            .setMessage("Are you sure you want to mark '${goal.name}' as completed?")
            .setPositiveButton("Complete") { _, _ ->
                val completedGoal = goal.copy(
                    isCompleted = true,
                    completedAt = Date(),
                    currentNumericValue = goal.targetNumericValue
                )

                activeGoals.remove(goal)
                completedGoals.add(0, completedGoal)

                activeGoalsAdapter.notifyDataSetChanged()
                completedGoalsAdapter.notifyDataSetChanged()

                Toast.makeText(this, "Congratulations! Goal completed!", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Goal completed: ${goal.name}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUpdateProgressDialog(goal: Goal) {
        val input = EditText(this)
        input.hint = "Enter new ${goal.category.displayName.lowercase()} value"
        input.setText(goal.currentValue)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Update Progress")
            .setMessage("Update your progress for '${goal.name}'")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newValue = input.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    updateGoalProgress(goal, newValue)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGoalProgress(goal: Goal, newCurrentValue: String) {
        val newNumericValue = extractNumericValue(newCurrentValue)
        val updatedGoal = goal.copy(
            currentValue = newCurrentValue,
            currentNumericValue = newNumericValue
        )

        val index = activeGoals.indexOf(goal)
        if (index >= 0) {
            activeGoals[index] = updatedGoal
            activeGoalsAdapter.notifyItemChanged(index)

            Toast.makeText(this, "Progress updated!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Goal progress updated: ${goal.name} -> $newCurrentValue")

            // Check if goal is now complete
            if (updatedGoal.progressPercentage >= 100) {
                AlertDialog.Builder(this)
                    .setTitle("Goal Achieved!")
                    .setMessage("Congratulations! You've reached your goal '${goal.name}'. Would you like to mark it as completed?")
                    .setPositiveButton("Complete") { _, _ ->
                        completeGoal(updatedGoal)
                    }
                    .setNegativeButton("Keep Active", null)
                    .show()
            }
        }
    }

    private fun showGoalDetails(goal: Goal) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val message = StringBuilder()

        message.append("Category: ${goal.category.displayName}\n")
        message.append("Current: ${goal.currentValue}\n")
        message.append("Target: ${goal.targetValue}\n")

        if (goal.isCompleted) {
            message.append("Status: Completed\n")
            goal.completedAt?.let {
                message.append("Completed on: ${dateFormat.format(it)}\n")
            }
        } else {
            message.append("Progress: ${goal.progressPercentage}%\n")
            message.append("Deadline: ${dateFormat.format(goal.deadline)}\n")
            message.append("Days remaining: ${goal.daysRemaining}\n")

            if (goal.isOverdue) {
                message.append("Status: ⚠️ Overdue\n")
            }
        }

        message.append("Created: ${dateFormat.format(goal.createdAt)}")

        val builder = AlertDialog.Builder(this)
            .setTitle(goal.name)
            .setMessage(message.toString())
            .setPositiveButton("Close", null)

        if (!goal.isCompleted) {
            builder.setNegativeButton("Edit") { _, _ ->
                editGoal(goal)
            }
            builder.setNeutralButton("Delete") { _, _ ->
                deleteGoal(goal)
            }
        }

        builder.show()
    }

    private fun editGoal(goal: Goal) {
        Toast.makeText(this, "Edit goal feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteGoal(goal: Goal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                activeGoals.remove(goal)
                activeGoalsAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        addGoalDialog?.dismiss()
    }
}

// Adapter Classes
class ActiveGoalsAdapter(
    private var goals: List<Goal>,
    private val onUpdateProgress: (Goal) -> Unit,
    private val onCompleteGoal: (Goal) -> Unit,
    private val onGoalClick: (Goal) -> Unit
) : RecyclerView.Adapter<ActiveGoalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalName: TextView = view.findViewById(R.id.goal_name)
        val goalCategory: TextView = view.findViewById(R.id.goal_category)
        val goalCategoryIndicator: View = view.findViewById(R.id.goal_category_indicator)
        val goalDueDate: TextView = view.findViewById(R.id.goal_due_date)
        val goalPercentage: TextView = view.findViewById(R.id.goal_percentage)
        val goalCurrentValue: TextView = view.findViewById(R.id.goal_current_value)
        val goalTargetValue: TextView = view.findViewById(R.id.goal_target_value)
        val goalProgressBar: ProgressBar = view.findViewById(R.id.goal_progress_bar)
        val updateProgressButton: Button = view.findViewById(R.id.update_progress_button)
        val completeGoalButton: Button = view.findViewById(R.id.complete_goal_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]

        holder.goalName.text = goal.name
        holder.goalCategory.text = goal.category.displayName
        holder.goalCategoryIndicator.setBackgroundColor(Color.parseColor(goal.category.color))

        // Format due date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        holder.goalDueDate.text = "Due ${dateFormat.format(goal.deadline)}"

        holder.goalPercentage.text = "${goal.progressPercentage}%"
        holder.goalCurrentValue.text = goal.currentValue
        holder.goalTargetValue.text = goal.targetValue

        // Update progress bar
        holder.goalProgressBar.progress = goal.progressPercentage
        holder.goalProgressBar.progressTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(goal.category.color))

        // Update colors based on status
        if (goal.isOverdue) {
            holder.goalDueDate.setTextColor(Color.parseColor("#FF6B6B"))
            holder.goalPercentage.setTextColor(Color.parseColor("#FF6B6B"))
        } else {
            holder.goalDueDate.setTextColor(Color.parseColor("#9E9E9E"))
            holder.goalPercentage.setTextColor(Color.parseColor("#FFFFFF"))
        }

        // Button listeners
        holder.updateProgressButton.setOnClickListener {
            onUpdateProgress(goal)
        }

        holder.completeGoalButton.setOnClickListener {
            onCompleteGoal(goal)
        }

        holder.itemView.setOnClickListener {
            onGoalClick(goal)
        }
    }

    override fun getItemCount() = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}

class CompletedGoalsAdapter(
    private var goals: List<Goal>,
    private val onGoalClick: (Goal) -> Unit
) : RecyclerView.Adapter<CompletedGoalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val completedGoalName: TextView = view.findViewById(R.id.completed_goal_name)
        val completedGoalCategory: TextView = view.findViewById(R.id.completed_goal_category)
        val completedGoalCategoryIndicator: View = view.findViewById(R.id.completed_goal_category_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]

        holder.completedGoalName.text = goal.name
        holder.completedGoalCategory.text = goal.category.displayName
        holder.completedGoalCategoryIndicator.setBackgroundColor(Color.parseColor(goal.category.color))

        holder.itemView.setOnClickListener {
            onGoalClick(goal)
        }
    }

    override fun getItemCount() = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}