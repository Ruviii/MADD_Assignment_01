package com.example.madd_assignment_01.repository

import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.database.entities.*
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val userRepository: UserRepository
) {

    // Reminder CRUD Operations
    suspend fun addReminder(reminder: Reminder): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val reminderEntity = ReminderEntity(
                id = reminder.id,
                userId = userId,
                title = reminder.title,
                type = reminder.type.name,
                time = reminder.time,
                repeatDays = reminder.repeatDays,
                isEnabled = reminder.isEnabled,
                description = reminder.description,
                soundEnabled = reminder.soundEnabled,
                vibrationEnabled = reminder.vibrationEnabled,
                snoozeMinutes = reminder.snoozeMinutes,
                priority = reminder.priority.name,
                createdAt = reminder.createdAt.time
            )

            reminderDao.insertReminder(reminderEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReminders(): Flow<List<Reminder>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return reminderDao.getRemindersByUser(userId).map { entities ->
            entities.map { entity ->
                Reminder(
                    id = entity.id,
                    title = entity.title,
                    type = ReminderType.valueOf(entity.type),
                    time = entity.time,
                    repeatDays = entity.repeatDays,
                    isEnabled = entity.isEnabled,
                    description = entity.description,
                    soundEnabled = entity.soundEnabled,
                    vibrationEnabled = entity.vibrationEnabled,
                    snoozeMinutes = entity.snoozeMinutes,
                    priority = Priority.valueOf(entity.priority),
                    createdAt = Date(entity.createdAt)
                )
            }
        }
    }

    fun getActiveReminders(): Flow<List<Reminder>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return reminderDao.getActiveRemindersByUser(userId).map { entities ->
            entities.map { entity ->
                Reminder(
                    id = entity.id,
                    title = entity.title,
                    type = ReminderType.valueOf(entity.type),
                    time = entity.time,
                    repeatDays = entity.repeatDays,
                    isEnabled = entity.isEnabled,
                    description = entity.description,
                    soundEnabled = entity.soundEnabled,
                    vibrationEnabled = entity.vibrationEnabled,
                    snoozeMinutes = entity.snoozeMinutes,
                    priority = Priority.valueOf(entity.priority),
                    createdAt = Date(entity.createdAt)
                )
            }
        }
    }

    fun getRemindersByType(type: ReminderType): Flow<List<Reminder>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return reminderDao.getRemindersByUserAndType(userId, type.name).map { entities ->
            entities.map { entity ->
                Reminder(
                    id = entity.id,
                    title = entity.title,
                    type = ReminderType.valueOf(entity.type),
                    time = entity.time,
                    repeatDays = entity.repeatDays,
                    isEnabled = entity.isEnabled,
                    description = entity.description,
                    soundEnabled = entity.soundEnabled,
                    vibrationEnabled = entity.vibrationEnabled,
                    snoozeMinutes = entity.snoozeMinutes,
                    priority = Priority.valueOf(entity.priority),
                    createdAt = Date(entity.createdAt)
                )
            }
        }
    }

    suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val reminderEntity = ReminderEntity(
                id = reminder.id,
                userId = userId,
                title = reminder.title,
                type = reminder.type.name,
                time = reminder.time,
                repeatDays = reminder.repeatDays,
                isEnabled = reminder.isEnabled,
                description = reminder.description,
                soundEnabled = reminder.soundEnabled,
                vibrationEnabled = reminder.vibrationEnabled,
                snoozeMinutes = reminder.snoozeMinutes,
                priority = reminder.priority.name,
                createdAt = reminder.createdAt.time
            )

            reminderDao.updateReminder(reminderEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleReminder(reminderId: String, isEnabled: Boolean): Result<Unit> {
        return try {
            val reminder = getReminder(reminderId) ?: return Result.failure(Exception("Reminder not found"))
            val updatedReminder = reminder.copy(isEnabled = isEnabled)
            updateReminder(updatedReminder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            reminderDao.deleteReminderById(reminderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReminder(reminderId: String): Reminder? {
        return try {
            val entity = reminderDao.getReminderById(reminderId)
            entity?.let {
                Reminder(
                    id = it.id,
                    title = it.title,
                    type = ReminderType.valueOf(it.type),
                    time = it.time,
                    repeatDays = it.repeatDays,
                    isEnabled = it.isEnabled,
                    description = it.description,
                    soundEnabled = it.soundEnabled,
                    vibrationEnabled = it.vibrationEnabled,
                    snoozeMinutes = it.snoozeMinutes,
                    priority = Priority.valueOf(it.priority),
                    createdAt = Date(it.createdAt)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Notification Settings
    suspend fun getNotificationSettings(): NotificationSettings? {
        val userId = userRepository.getCurrentUserId() ?: return null

        // Default notification settings - would need separate DAO for persistence
        return NotificationSettings(
            userId = userId,
            workoutReminders = true,
            mealReminders = true,
            waterReminders = true,
            goalReminders = true,
            achievementNotifications = true,
            weeklyReports = true,
            soundEnabled = true,
            vibrationEnabled = true,
            quietHours = QuietHours("22:00", "07:00", true)
        )
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings): Result<Unit> {
        return try {
            // Would need NotificationSettingsDao for persistence
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reminder Scheduling
    suspend fun getUpcomingReminders(hoursAhead: Int = 24): List<Reminder> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()

        return try {
            // Get active reminders and filter based on time
            val activeReminders = reminderDao.getActiveRemindersList(userId)

            activeReminders.mapNotNull { entity ->
                try {
                    val reminder = Reminder(
                        id = entity.id,
                        title = entity.title,
                        type = ReminderType.valueOf(entity.type),
                        time = entity.time,
                        repeatDays = entity.repeatDays,
                        isEnabled = entity.isEnabled,
                        description = entity.description,
                        soundEnabled = entity.soundEnabled,
                        vibrationEnabled = entity.vibrationEnabled,
                        snoozeMinutes = entity.snoozeMinutes,
                        priority = Priority.valueOf(entity.priority),
                        createdAt = Date(entity.createdAt)
                    )

                    // Check if reminder should trigger in the next specified hours
                    if (shouldTriggerSoon(reminder, hoursAhead)) reminder else null
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun shouldTriggerSoon(reminder: Reminder, hoursAhead: Int): Boolean {
        if (!reminder.isEnabled) return false

        val currentTime = Calendar.getInstance()
        val reminderTime = parseTime(reminder.time)
        val currentDay = getDayOfWeek(currentTime.get(Calendar.DAY_OF_WEEK))

        // Check if today is in repeat days
        if (reminder.repeatDays.contains(currentDay)) {
            val todayReminderTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderTime.first)
                set(Calendar.MINUTE, reminderTime.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val timeDiff = todayReminderTime.timeInMillis - currentTime.timeInMillis
            return timeDiff > 0 && timeDiff <= (hoursAhead * 60 * 60 * 1000)
        }

        return false
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    private fun getDayOfWeek(calendarDay: Int): String {
        return when (calendarDay) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }
    }

    // Reminder Templates
    suspend fun getReminderTemplates(): List<Reminder> {
        return listOf(
            Reminder(
                title = "Morning Workout",
                type = ReminderType.WORKOUT,
                time = "07:00",
                repeatDays = listOf("Monday", "Wednesday", "Friday"),
                description = "Time for your morning workout!"
            ),
            Reminder(
                title = "Drink Water",
                type = ReminderType.WATER,
                time = "10:00",
                repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                description = "Stay hydrated! Drink a glass of water."
            ),
            Reminder(
                title = "Healthy Lunch",
                type = ReminderType.MEAL,
                time = "12:30",
                repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
                description = "Time for a nutritious lunch!"
            ),
            Reminder(
                title = "Evening Walk",
                type = ReminderType.WORKOUT,
                time = "18:00",
                repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                description = "Take a relaxing evening walk."
            ),
            Reminder(
                title = "Sleep Time",
                type = ReminderType.SLEEP,
                time = "22:00",
                repeatDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                description = "Time to prepare for bed for better recovery."
            )
        )
    }

    // Habit Tracking (placeholder for future implementation)
    suspend fun addHabitTracker(habit: HabitTracker): Result<Unit> {
        return try {
            // Would need HabitTrackerDao for persistence
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabitTrackers(): List<HabitTracker> {
        // Would need implementation with HabitTrackerDao
        return emptyList()
    }

    suspend fun updateHabitStreak(habitId: String, completed: Boolean): Result<Unit> {
        return try {
            // Would need implementation with HabitTrackerDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Snooze functionality
    suspend fun snoozeReminder(reminderId: String, minutes: Int = 5): Result<Unit> {
        return try {
            // Would need ReminderScheduleDao for persistence
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markReminderCompleted(reminderId: String): Result<Unit> {
        return try {
            // Would need ReminderScheduleDao for persistence
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Analytics
    suspend fun getReminderCompletionRate(type: ReminderType? = null): Double {
        // Would need implementation with ReminderScheduleDao
        return 0.0
    }

    suspend fun getMostActiveReminderType(): ReminderType? {
        val userId = userRepository.getCurrentUserId() ?: return null

        return try {
            // Get all reminders and find most common type
            val reminders = reminderDao.getRemindersList(userId)
            val typeFrequency = reminders.groupBy { it.type }.mapValues { it.value.size }
            val mostFrequentType = typeFrequency.maxByOrNull { it.value }?.key

            mostFrequentType?.let { ReminderType.valueOf(it) }
        } catch (e: Exception) {
            null
        }
    }
}