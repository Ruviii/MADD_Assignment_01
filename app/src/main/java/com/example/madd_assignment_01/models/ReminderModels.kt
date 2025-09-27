package com.example.madd_assignment_01.models

import java.util.*

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: ReminderType,
    val time: String,
    val repeatDays: List<String>,
    val isEnabled: Boolean = true,
    val description: String? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Date = Date()
)

enum class ReminderType(val displayName: String) {
    WORKOUT("Workout"),
    WATER("Water"),
    MEAL("Meal"),
    MEDICATION("Medication"),
    SLEEP("Sleep"),
    WEIGH_IN("Weigh In"),
    CUSTOM("Custom");

    companion object {
        fun fromDisplayName(displayName: String): ReminderType {
            return values().find { it.displayName == displayName } ?: CUSTOM
        }
    }
}

data class NotificationSettings(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val workoutReminders: Boolean = true,
    val mealReminders: Boolean = true,
    val waterReminders: Boolean = true,
    val goalReminders: Boolean = true,
    val achievementNotifications: Boolean = true,
    val weeklyReports: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHours: QuietHours? = null
)

data class QuietHours(
    val startTime: String, // "22:00"
    val endTime: String,   // "07:00"
    val enabled: Boolean = true
)

data class ReminderSchedule(
    val id: String = UUID.randomUUID().toString(),
    val reminderId: String,
    val scheduledTime: Date,
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val snoozedUntil: Date? = null,
    val skipped: Boolean = false
)

data class HabitTracker(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val targetFrequency: Int, // times per day/week
    val frequencyType: FrequencyType,
    val reminderTimes: List<String>,
    val streakCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Date = Date()
)

enum class FrequencyType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class HabitEntry(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val date: Date,
    val completed: Boolean = false,
    val notes: String = "",
    val completedAt: Date? = null
)