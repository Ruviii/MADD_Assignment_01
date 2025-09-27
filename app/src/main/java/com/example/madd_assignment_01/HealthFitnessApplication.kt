package com.example.madd_assignment_01

import android.app.Application
import com.example.madd_assignment_01.database.HealthFitnessDatabase
import com.example.madd_assignment_01.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class HealthFitnessApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Core database and repositories
    val database by lazy { HealthFitnessDatabase.getDatabase(this, applicationScope) }
    val userRepository by lazy { UserRepository(database.userDao(), this) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao(), userRepository) }
    val nutritionRepository by lazy { NutritionRepository(database.mealDao(), database.foodItemDao(), userRepository) }
    val goalRepository by lazy { GoalRepository(database.goalDao(), userRepository) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao(), userRepository) }

    // Advanced repositories
    val analyticsRepository by lazy {
        AnalyticsRepository(userRepository, workoutRepository, nutritionRepository, goalRepository)
    }

    override fun onCreate() {
        super.onCreate()
    }
}