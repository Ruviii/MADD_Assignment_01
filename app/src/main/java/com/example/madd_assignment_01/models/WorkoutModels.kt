package com.example.madd_assignment_01.models

import java.util.*

data class WorkoutRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: WorkoutType,
    val date: Date,
    val duration: Int, // minutes
    val calories: Int,
    val notes: String = ""
)

enum class WorkoutType(val displayName: String) {
    CARDIO("Cardio"),
    STRENGTH("Strength"),
    FLEXIBILITY("Flexibility"),
    HIIT("HIIT");

    companion object {
        fun fromDisplayName(displayName: String): WorkoutType {
            return values().find { it.displayName == displayName } ?: CARDIO
        }
    }
}

enum class WorkoutFilter {
    ALL, RECENT, CARDIO, STRENGTH, FLEXIBILITY, HIIT
}

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: WorkoutType,
    val description: String,
    val duration: Int, // minutes
    val caloriesPerMinute: Double,
    val difficultyLevel: WorkoutDifficultyLevel = WorkoutDifficultyLevel.BEGINNER
)

enum class WorkoutDifficultyLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val totalDuration: Int,
    val estimatedCalories: Int,
    val difficultyLevel: DifficultyLevel
)

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val workoutPlanId: String?,
    val startTime: Date,
    val endTime: Date?,
    val exercises: MutableList<CompletedExercise> = mutableListOf(),
    val totalCaloriesBurned: Int = 0,
    val notes: String = ""
) {
    val duration: Int
        get() = if (endTime != null) {
            ((endTime.time - startTime.time) / (1000 * 60)).toInt()
        } else 0
}

data class CompletedExercise(
    val exerciseId: String,
    val name: String,
    val duration: Int, // minutes
    val caloriesBurned: Int,
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0, // kg
    val distance: Double = 0.0, // km
    val notes: String = ""
)