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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madd_assignment_01.data.DataManager
import com.example.madd_assignment_01.utils.NavigationUtils
import java.text.SimpleDateFormat
import java.util.*

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: ReminderType,
    val time: String,
    val repeatDays: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val description: String = "",
    val createdDate: Date = Date()
)

enum class ReminderType(val displayName: String, val iconRes: Int) {
    WORKOUT("Workout", R.drawable.workout_icon),
    WATER("Water", R.drawable.home),
    MEAL("Meal", R.drawable.diet_icon)
}

class ReminderActivity : AppCompatActivity() {

    // Views
    private lateinit var addReminderFab: CardView
    private lateinit var reminderSettingsButton: ImageView
    private lateinit var addReminderButton: LinearLayout

    // Reminder sections
    private lateinit var activeRemindersTitle: TextView
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var remindersAdapter: RemindersAdapter

    // Bottom navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navWorkouts: LinearLayout
    private lateinit var navDiet: LinearLayout
    private lateinit var navGoals: LinearLayout
    private lateinit var navReminders: LinearLayout
    private lateinit var navAnalytics: LinearLayout

    // Data
    private val activeReminders = mutableListOf<Reminder>()
    private lateinit var dataManager: DataManager

    // Dialog
    private var addReminderDialog: AlertDialog? = null

    companion object {
        private const val TAG = "ReminderActivity"
        private val DAYS_OF_WEEK = arrayOf("M", "T", "W", "T", "F", "S", "S")
        private val DAYS_FULL_NAMES = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_reminder)
            dataManager = DataManager.getInstance(this)
            initializeViews()
            setupClickListeners()
            setupRecyclerView()
            loadRemindersFromStorage()

            Log.d(TAG, "ReminderActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading reminders screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header
            addReminderFab = findViewById(R.id.add_reminder_fab)
            reminderSettingsButton = findViewById(R.id.reminder_settings_button)

            // Sections
            activeRemindersTitle = findViewById(R.id.active_reminders_title)
            remindersRecyclerView = findViewById(R.id.reminders_recyclerview)
            addReminderButton = findViewById(R.id.add_reminder_button)


            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Add reminder buttons
        addReminderFab.setOnClickListener {
            Log.d(TAG, "Add reminder FAB clicked")
            showAddReminderDialog()
        }

        addReminderButton.setOnClickListener {
            Log.d(TAG, "Add reminder button clicked")
            showAddReminderDialog()
        }

        // Settings button
        reminderSettingsButton.setOnClickListener {
            Log.d(TAG, "Reminder settings button clicked")
            showReminderSettings()
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
            // Already on reminders page
        }

        navAnalytics.setOnClickListener {
            NavigationUtils.navigateToAnalytics(this)
        }
    }

    private fun setupRecyclerView() {
        remindersAdapter = RemindersAdapter(
            reminders = activeReminders,
            onToggleReminder = { reminder, isEnabled ->
                Log.d(TAG, "Toggle reminder: ${reminder.title} to $isEnabled")
                toggleReminder(reminder, isEnabled)
            },
            onDeleteReminder = { reminder ->
                Log.d(TAG, "Delete reminder: ${reminder.title}")
                deleteReminder(reminder)
            },
            onEditReminder = { reminder ->
                Log.d(TAG, "Edit reminder: ${reminder.title}")
                editReminder(reminder)
            }
        )

        remindersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReminderActivity)
            adapter = remindersAdapter
        }
    }

    private fun loadRemindersFromStorage() {
        val savedReminders = dataManager.getReminders()

        if (savedReminders.isEmpty()) {
            // Load sample reminders only if no saved data exists
            loadSampleReminders()
        } else {
            activeReminders.clear()
            activeReminders.addAll(savedReminders)
            remindersAdapter.notifyDataSetChanged()
        }

        Log.d(TAG, "Reminders loaded from storage: ${activeReminders.size} reminders")
    }

    private fun loadSampleReminders() {
        val sampleReminders = listOf(
            Reminder(
                title = "Morning Workout",
                type = ReminderType.WORKOUT,
                time = "07:00 AM",
                repeatDays = listOf("Mon", "Wed", "Fri"),
                description = "Start your day with energy!"
            ),
            Reminder(
                title = "Drink Water",
                type = ReminderType.WATER,
                time = "Every 2 hours",
                repeatDays = listOf("Everyday"),
                description = "Stay hydrated throughout the day"
            ),
            Reminder(
                title = "Protein Shake",
                type = ReminderType.MEAL,
                time = "04:30 PM",
                repeatDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
                isEnabled = false,
                description = "Post-workout nutrition"
            ),
            Reminder(
                title = "Evening Run",
                type = ReminderType.WORKOUT,
                time = "06:30 PM",
                repeatDays = listOf("Tue", "Thu"),
                description = "Evening cardio session"
            )
        )

        activeReminders.addAll(sampleReminders)

        // Save sample reminders to storage
        dataManager.saveReminders(activeReminders)

        remindersAdapter.notifyDataSetChanged()
        Log.d(TAG, "Sample reminders loaded: ${activeReminders.size} reminders")
    }

    private fun showAddReminderDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)

            val titleInput = dialogView.findViewById<EditText>(R.id.reminder_title_input)
            val workoutTypeButton = dialogView.findViewById<LinearLayout>(R.id.workout_type_button)
            val waterTypeButton = dialogView.findViewById<LinearLayout>(R.id.water_type_button)
            val mealTypeButton = dialogView.findViewById<LinearLayout>(R.id.meal_type_button)
            val timeInput = dialogView.findViewById<EditText>(R.id.reminder_time_input)
            val closeReminderDialog = dialogView.findViewById<ImageView>(R.id.close_reminder_dialog)
            val cancelReminderButton = dialogView.findViewById<Button>(R.id.cancel_reminder_button)
            val saveReminderButton = dialogView.findViewById<Button>(R.id.save_reminder_button)

            // Day selection buttons
            val dayButtons = arrayOf(
                dialogView.findViewById<Button>(R.id.day_m),
                dialogView.findViewById<Button>(R.id.day_t1),
                dialogView.findViewById<Button>(R.id.day_w),
                dialogView.findViewById<Button>(R.id.day_t2),
                dialogView.findViewById<Button>(R.id.day_f),
                dialogView.findViewById<Button>(R.id.day_s1),
                dialogView.findViewById<Button>(R.id.day_s2)
            )

            var selectedType = ReminderType.WORKOUT
            val selectedDays = mutableListOf<String>()

            // Type selection listeners
            val typeButtons = listOf(workoutTypeButton, waterTypeButton, mealTypeButton)
            val types = listOf(ReminderType.WORKOUT, ReminderType.WATER, ReminderType.MEAL)

            typeButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    selectedType = types[index]
                    updateTypeSelection(typeButtons, index)
                    Log.d(TAG, "Selected reminder type: ${selectedType.displayName}")
                }
            }

            // Day selection listeners
            dayButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    val dayName = DAYS_FULL_NAMES[index]
                    if (selectedDays.contains(dayName)) {
                        selectedDays.remove(dayName)
                        button.setBackgroundResource(R.drawable.indicator_inactive)
                    } else {
                        selectedDays.add(dayName)
                        button.setBackgroundResource(R.drawable.indicator_active)
                    }
                    Log.d(TAG, "Selected days: $selectedDays")
                }
            }

            addReminderDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            addReminderDialog?.window?.apply {
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
            closeReminderDialog.setOnClickListener {
                addReminderDialog?.dismiss()
            }

            cancelReminderButton.setOnClickListener {
                addReminderDialog?.dismiss()
            }

            // Save reminder listener
            saveReminderButton.setOnClickListener {
                val title = titleInput.text.toString().trim()
                val time = timeInput.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (time.isEmpty()) {
                    Toast.makeText(this, "Please enter a time", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (selectedDays.isEmpty()) {
                    Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newReminder = Reminder(
                    title = title,
                    type = selectedType,
                    time = time,
                    repeatDays = selectedDays.toList(),
                    description = "Custom reminder"
                )

                addReminder(newReminder)
                addReminderDialog?.dismiss()

                Toast.makeText(this, "Reminder added successfully!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "New reminder added: $title")
            }

            addReminderDialog?.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing add reminder dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening add reminder dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTypeSelection(typeButtons: List<LinearLayout>, selectedIndex: Int) {
        typeButtons.forEachIndexed { index, button ->
            if (index == selectedIndex) {
                button.setBackgroundResource(R.drawable.button_blue_rounded)
                // Update text and icon colors for selected type
                val textView = button.getChildAt(1) as? TextView
                val imageView = button.getChildAt(0) as? ImageView
                textView?.setTextColor(Color.WHITE)
                imageView?.setColorFilter(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.parseColor("#34495E")) // card_background color
                // Update text and icon colors for unselected type
                val textView = button.getChildAt(1) as? TextView
                val imageView = button.getChildAt(0) as? ImageView
                textView?.setTextColor(Color.parseColor("#95A5A6"))
                imageView?.setColorFilter(Color.parseColor("#1E88E5"))
            }
        }
    }

    private fun addReminder(reminder: Reminder) {
        activeReminders.add(reminder)
        activeReminders.sortBy { it.time }
        dataManager.addReminder(reminder)
        remindersAdapter.notifyDataSetChanged()
    }

    private fun toggleReminder(reminder: Reminder, isEnabled: Boolean) {
        val index = activeReminders.indexOf(reminder)
        if (index >= 0) {
            val updatedReminder = reminder.copy(isEnabled = isEnabled)
            activeReminders[index] = updatedReminder
            dataManager.toggleReminder(reminder.id, isEnabled)
            remindersAdapter.notifyItemChanged(index)
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete '${reminder.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                activeReminders.remove(reminder)
                dataManager.deleteReminder(reminder.id)
                remindersAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editReminder(reminder: Reminder) {
        Toast.makeText(this, "Edit reminder feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showReminderSettings() {
        val options = arrayOf(
            "Notification Settings",
            "Default Reminder Times",
            "Sound & Vibration",
            "Do Not Disturb Hours",
            "Export Reminders"
        )

        AlertDialog.Builder(this)
            .setTitle("Reminder Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showNotificationSettings()
                    1 -> showDefaultReminderTimes()
                    2 -> showSoundVibrationSettings()
                    3 -> showDoNotDisturbSettings()
                    4 -> exportReminders()
                }
            }
            .show()
    }

    private fun showNotificationSettings() {
        Toast.makeText(this, "Notification settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showDefaultReminderTimes() {
        Toast.makeText(this, "Default reminder times coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showSoundVibrationSettings() {
        Toast.makeText(this, "Sound & vibration settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showDoNotDisturbSettings() {
        Toast.makeText(this, "Do not disturb settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun exportReminders() {
        Toast.makeText(this, "Export reminders feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        addReminderDialog?.dismiss()
    }
}

// Adapter Class
class RemindersAdapter(
    private var reminders: List<Reminder>,
    private val onToggleReminder: (Reminder, Boolean) -> Unit,
    private val onDeleteReminder: (Reminder) -> Unit,
    private val onEditReminder: (Reminder) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reminderIcon: ImageView = view.findViewById(R.id.reminder_icon)
        val reminderTitle: TextView = view.findViewById(R.id.reminder_title)
        val reminderTime: TextView = view.findViewById(R.id.reminder_time)
        val reminderDays: TextView = view.findViewById(R.id.reminder_days)
        val reminderToggle: Switch = view.findViewById(R.id.reminder_toggle)
        val deleteReminderButton: ImageView = view.findViewById(R.id.delete_reminder_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]

        holder.reminderIcon.setImageResource(reminder.type.iconRes)
        holder.reminderTitle.text = reminder.title
        holder.reminderTime.text = reminder.time
        holder.reminderDays.text = reminder.repeatDays.joinToString(", ")
        holder.reminderToggle.isChecked = reminder.isEnabled

        // Set alpha based on enabled state
        val alpha = if (reminder.isEnabled) 1.0f else 0.5f
        holder.itemView.alpha = alpha

        holder.reminderToggle.setOnCheckedChangeListener { _, isChecked ->
            onToggleReminder(reminder, isChecked)
        }

        holder.deleteReminderButton.setOnClickListener {
            onDeleteReminder(reminder)
        }

        holder.itemView.setOnClickListener {
            onEditReminder(reminder)
        }
    }

    override fun getItemCount() = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}