package com.example.madd_assignment_01.models

import java.util.*

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: GoalCategory,
    val currentValue: String,
    val targetValue: String,
    val currentNumericValue: Double,
    val targetNumericValue: Double,
    val deadline: Date,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val priority: Priority = Priority.MEDIUM,
    val description: String = ""
) {
    val progressPercentage: Int
        get() = if (targetNumericValue > 0) {
            ((currentNumericValue / targetNumericValue) * 100).toInt().coerceAtMost(100)
        } else 0

    val daysRemaining: Int
        get() = ((deadline.time - Date().time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

    val isOverdue: Boolean
        get() = !isCompleted && Date().after(deadline)
}

enum class GoalCategory(val displayName: String, val unit: String) {
    WEIGHT("Weight Loss", "kg"),
    CARDIO("Cardio Endurance", "km"),
    STRENGTH("Strength Training", "kg"),
    NUTRITION("Nutrition", "kcal"),
    HYDRATION("Hydration", "L"),
    ACTIVITY("Activity", "workouts"),
    SLEEP("Sleep", "hours"),
    STEPS("Steps", "steps");

    companion object {
        fun fromDisplayName(displayName: String): GoalCategory {
            return values().find { it.displayName == displayName } ?: WEIGHT
        }
    }
}

enum class Priority(val displayName: String, val value: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

data class GoalTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: GoalCategory,
    val description: String,
    val targetValue: String,
    val targetNumericValue: Double,
    val recommendedDuration: Int, // days
    val difficulty: DifficultyLevel,
    val tips: List<String> = emptyList()
)

data class GoalProgress(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val date: Date,
    val value: String,
    val numericValue: Double,
    val notes: String = ""
)

data class Achievement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Date? = null,
    val requirement: String,
    val points: Int = 10
)

enum class AchievementCategory(val displayName: String) {
    WORKOUT("Workout"),
    NUTRITION("Nutrition"),
    GOALS("Goals"),
    CONSISTENCY("Consistency"),
    MILESTONES("Milestones")
}

data class Challenge(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: GoalCategory,
    val startDate: Date,
    val endDate: Date,
    val targetValue: Double,
    val unit: String,
    val participants: Int = 0,
    val isJoined: Boolean = false,
    val progress: Double = 0.0
)

data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val title: String,
    val targetValue: Double,
    val isReached: Boolean = false,
    val reachedDate: Date? = null,
    val reward: String? = null
)

enum class DifficultyLevel(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert")
}