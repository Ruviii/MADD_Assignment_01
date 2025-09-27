package com.example.madd_assignment_01.data

import android.content.Context
import com.example.madd_assignment_01.HealthFitnessApplication
import com.example.madd_assignment_01.database.entities.*
import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.models.WorkoutRecord
import com.example.madd_assignment_01.models.WorkoutType
import com.example.madd_assignment_01.models.Meal
import com.example.madd_assignment_01.models.MealType
import com.example.madd_assignment_01.models.SelectedFoodItem
import com.example.madd_assignment_01.models.FoodItem
import com.example.madd_assignment_01.models.Goal
import com.example.madd_assignment_01.models.GoalCategory
import com.example.madd_assignment_01.models.Reminder
import com.example.madd_assignment_01.models.ReminderType
import com.example.madd_assignment_01.repository.*
// Keep old classes with their full names to avoid conflicts
import com.example.madd_assignment_01.DailyNutrition as LegacyDailyNutrition
import com.example.madd_assignment_01.Achievement as LegacyAchievement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.util.*

class DatabaseDataManager private constructor(private val context: Context) {
    private val app = context.applicationContext as HealthFitnessApplication
    private val database = app.database
    private val userRepository = app.userRepository
    private val workoutRepository = app.workoutRepository
    private val nutritionRepository = app.nutritionRepository
    private val goalRepository = app.goalRepository
    private val reminderRepository = app.reminderRepository
    private val analyticsRepository = app.analyticsRepository

    private val workoutDao = database.workoutDao()
    private val mealDao = database.mealDao()
    private val foodItemDao = database.foodItemDao()
    private val goalDao = database.goalDao()
    private val reminderDao = database.reminderDao()

    // For legacy DataManager - pass context
    private val legacyDataManager by lazy { DataManager.getInstance(context) }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseDataManager? = null

        fun getInstance(context: Context): DatabaseDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseDataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Helper function to get current user ID
    private fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    // Workout Management - delegate to WorkoutRepository
    suspend fun addWorkout(workout: WorkoutRecord) {
        workoutRepository.addWorkout(workout)
    }

    suspend fun getWorkouts(): MutableList<WorkoutRecord> {
        return try {
            val workouts = mutableListOf<WorkoutRecord>()
            workoutRepository.getWorkouts().collect { workouts.addAll(it) }
            workouts
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun deleteWorkout(workoutId: String) {
        workoutRepository.deleteWorkout(workoutId)
    }

    // Meal Management - delegate to NutritionRepository
    suspend fun addMeal(meal: Meal) {
        nutritionRepository.addMeal(meal)
    }

    suspend fun getMeals(): MutableList<Meal> {
        return try {
            val meals = mutableListOf<Meal>()
            val today = Date()
            nutritionRepository.getMealsByDate(today).collect { meals.addAll(it) }
            meals
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun deleteMeal(mealId: String) {
        nutritionRepository.deleteMeal(mealId)
    }

    // Goal Management - delegate to GoalRepository
    suspend fun addGoal(goal: Goal) {
        goalRepository.addGoal(goal)
    }

    suspend fun getActiveGoals(): MutableList<Goal> {
        return try {
            val goals = mutableListOf<Goal>()
            goalRepository.getActiveGoals().collect { goals.addAll(it) }
            goals
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun getCompletedGoals(): MutableList<Goal> {
        return try {
            val goals = mutableListOf<Goal>()
            goalRepository.getGoals().collect { allGoals ->
                goals.addAll(allGoals.filter { it.isCompleted })
            }
            goals
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun completeGoal(goalId: String) {
        goalRepository.completeGoal(goalId)
    }

    suspend fun updateGoalProgress(goalId: String, newCurrentValue: String, newNumericValue: Double) {
        goalRepository.updateGoalProgress(goalId, newNumericValue)
    }

    suspend fun deleteGoal(goalId: String) {
        goalRepository.deleteGoal(goalId)
    }

    // Reminder Management - delegate to ReminderRepository
    suspend fun addReminder(reminder: Reminder) {
        reminderRepository.addReminder(reminder)
    }

    suspend fun getReminders(): MutableList<Reminder> {
        return try {
            val reminders = mutableListOf<Reminder>()
            reminderRepository.getReminders().collect { reminders.addAll(it) }
            reminders
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun toggleReminder(reminderId: String, isEnabled: Boolean) {
        reminderRepository.toggleReminder(reminderId, isEnabled)
    }

    suspend fun deleteReminder(reminderId: String) {
        reminderRepository.deleteReminder(reminderId)
    }

    // Analytics Helper Functions - delegate to repositories
    suspend fun getWeeklyWorkoutMinutes(): Int {
        return workoutRepository.getWeeklyWorkoutMinutes()
    }

    suspend fun getWeeklyCaloriesBurned(): Int {
        return workoutRepository.getWeeklyCaloriesBurned()
    }

    suspend fun getTodaysCaloriesConsumed(): Int {
        return nutritionRepository.getDailyCalories(Date())
    }

    // Legacy methods for compatibility with existing DataManager
    fun saveUserProfile(profile: DataManager.UserProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                userRepository.updateUserProfile(
                    name = profile.name,
                    age = profile.age,
                    height = profile.height,
                    currentWeight = profile.currentWeight,
                    targetWeight = profile.targetWeight,
                    activityLevel = profile.activityLevel ?: "MODERATE"
                )
            }
        }
    }

    fun getUserProfile(): DataManager.UserProfile {
        return DataManager.UserProfile(
            name = userRepository.getCurrentUserName() ?: "User",
            email = userRepository.getCurrentUserEmail() ?: ""
        )
    }

    // Methods for compatibility with legacy DataManager interface
    // Use aliased types to avoid conflicts between old and new model classes
    fun getDailyNutrition(): LegacyDailyNutrition = legacyDataManager.getDailyNutrition()
    fun saveDailyNutrition(nutrition: LegacyDailyNutrition) = legacyDataManager.saveDailyNutrition(nutrition)
    fun getAchievements(): MutableList<LegacyAchievement> = legacyDataManager.getAchievements()
    fun saveAchievements(achievements: List<LegacyAchievement>) = legacyDataManager.saveAchievements(achievements)
    fun unlockAchievement(achievementId: String) = legacyDataManager.unlockAchievement(achievementId)

    // Synchronous wrapper methods for compatibility
    fun saveWorkouts(workouts: List<WorkoutRecord>) {
        CoroutineScope(Dispatchers.IO).launch {
            // Clear existing workouts and add new ones
            val userId = getCurrentUserId() ?: return@launch
            workouts.forEach { addWorkout(it) }
        }
    }

    fun saveMeals(meals: List<Meal>) {
        CoroutineScope(Dispatchers.IO).launch {
            meals.forEach { addMeal(it) }
        }
    }

    fun saveActiveGoals(goals: List<Goal>) {
        CoroutineScope(Dispatchers.IO).launch {
            goals.forEach { addGoal(it) }
        }
    }

    fun saveCompletedGoals(goals: List<Goal>) {
        // For now, handled automatically through completeGoal method
    }

    fun saveReminders(reminders: List<Reminder>) {
        CoroutineScope(Dispatchers.IO).launch {
            reminders.forEach { addReminder(it) }
        }
    }
}