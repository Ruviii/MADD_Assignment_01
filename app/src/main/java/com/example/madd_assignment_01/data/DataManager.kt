package com.example.madd_assignment_01.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.madd_assignment_01.*
import java.util.*

class DataManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "health_fitness_app_data"
        private const val KEY_WORKOUTS = "workouts"
        private const val KEY_MEALS = "meals"
        private const val KEY_GOALS = "goals"
        private const val KEY_COMPLETED_GOALS = "completed_goals"
        private const val KEY_REMINDERS = "reminders"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_DAILY_NUTRITION = "daily_nutrition"
        private const val KEY_ACHIEVEMENTS = "achievements"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        @Volatile
        private var INSTANCE: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // User Profile Management
    data class UserProfile(
        val name: String = "User",
        val email: String = "",
        val age: Int = 25,
        val height: Int = 170, // cm
        val currentWeight: Double = 70.0, // kg
        val targetWeight: Double = 65.0, // kg
        val activityLevel: String = "Moderate",
        val joinDate: Date = Date()
    )

    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        sharedPreferences.edit().putString(KEY_USER_PROFILE, json).apply()
    }

    fun getUserProfile(): UserProfile {
        val json = sharedPreferences.getString(KEY_USER_PROFILE, null)
        return if (json != null) {
            gson.fromJson(json, UserProfile::class.java)
        } else {
            UserProfile() // Return default profile
        }
    }

    // Workout Management
    fun saveWorkouts(workouts: List<WorkoutRecord>) {
        val json = gson.toJson(workouts)
        sharedPreferences.edit().putString(KEY_WORKOUTS, json).apply()
    }

    fun getWorkouts(): MutableList<WorkoutRecord> {
        val json = sharedPreferences.getString(KEY_WORKOUTS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<WorkoutRecord>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun addWorkout(workout: WorkoutRecord) {
        val workouts = getWorkouts()
        workouts.add(0, workout) // Add to beginning
        saveWorkouts(workouts)
    }

    fun deleteWorkout(workoutId: String) {
        val workouts = getWorkouts()
        workouts.removeAll { it.id == workoutId }
        saveWorkouts(workouts)
    }

    // Meal Management
    fun saveMeals(meals: List<Meal>) {
        val json = gson.toJson(meals)
        sharedPreferences.edit().putString(KEY_MEALS, json).apply()
    }

    fun getMeals(): MutableList<Meal> {
        val json = sharedPreferences.getString(KEY_MEALS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Meal>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun addMeal(meal: Meal) {
        val meals = getMeals()
        // Find existing meal of same type and merge, or add new meal
        val existingMealIndex = meals.indexOfFirst { it.type == meal.type }

        if (existingMealIndex >= 0) {
            meals[existingMealIndex].foodItems.addAll(meal.foodItems)
        } else {
            meals.add(meal)
            meals.sortBy { it.type.ordinal }
        }
        saveMeals(meals)
    }

    fun deleteMeal(mealId: String) {
        val meals = getMeals()
        meals.removeAll { it.id == mealId }
        saveMeals(meals)
    }

    // Goals Management
    fun saveActiveGoals(goals: List<Goal>) {
        val json = gson.toJson(goals)
        sharedPreferences.edit().putString(KEY_GOALS, json).apply()
    }

    fun getActiveGoals(): MutableList<Goal> {
        val json = sharedPreferences.getString(KEY_GOALS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Goal>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun saveCompletedGoals(goals: List<Goal>) {
        val json = gson.toJson(goals)
        sharedPreferences.edit().putString(KEY_COMPLETED_GOALS, json).apply()
    }

    fun getCompletedGoals(): MutableList<Goal> {
        val json = sharedPreferences.getString(KEY_COMPLETED_GOALS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Goal>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun addGoal(goal: Goal) {
        val goals = getActiveGoals()
        goals.add(0, goal) // Add to beginning
        saveActiveGoals(goals)
    }

    fun completeGoal(goalId: String) {
        val activeGoals = getActiveGoals()
        val completedGoals = getCompletedGoals()

        val goal = activeGoals.find { it.id == goalId }
        if (goal != null) {
            val completedGoal = goal.copy(
                isCompleted = true,
                completedAt = Date(),
                currentNumericValue = goal.targetNumericValue
            )

            activeGoals.removeAll { it.id == goalId }
            completedGoals.add(0, completedGoal)

            saveActiveGoals(activeGoals)
            saveCompletedGoals(completedGoals)
        }
    }

    fun updateGoalProgress(goalId: String, newCurrentValue: String, newNumericValue: Double) {
        val goals = getActiveGoals()
        val goalIndex = goals.indexOfFirst { it.id == goalId }

        if (goalIndex >= 0) {
            val updatedGoal = goals[goalIndex].copy(
                currentValue = newCurrentValue,
                currentNumericValue = newNumericValue
            )
            goals[goalIndex] = updatedGoal
            saveActiveGoals(goals)
        }
    }

    fun deleteGoal(goalId: String) {
        val goals = getActiveGoals()
        goals.removeAll { it.id == goalId }
        saveActiveGoals(goals)
    }

    // Reminders Management
    fun saveReminders(reminders: List<Reminder>) {
        val json = gson.toJson(reminders)
        sharedPreferences.edit().putString(KEY_REMINDERS, json).apply()
    }

    fun getReminders(): MutableList<Reminder> {
        val json = sharedPreferences.getString(KEY_REMINDERS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Reminder>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun addReminder(reminder: Reminder) {
        val reminders = getReminders()
        reminders.add(reminder)
        reminders.sortBy { it.time }
        saveReminders(reminders)
    }

    fun toggleReminder(reminderId: String, isEnabled: Boolean) {
        val reminders = getReminders()
        val reminderIndex = reminders.indexOfFirst { it.id == reminderId }

        if (reminderIndex >= 0) {
            val updatedReminder = reminders[reminderIndex].copy(isEnabled = isEnabled)
            reminders[reminderIndex] = updatedReminder
            saveReminders(reminders)
        }
    }

    fun deleteReminder(reminderId: String) {
        val reminders = getReminders()
        reminders.removeAll { it.id == reminderId }
        saveReminders(reminders)
    }

    // Daily Nutrition Settings
    fun saveDailyNutrition(nutrition: DailyNutrition) {
        val json = gson.toJson(nutrition)
        sharedPreferences.edit().putString(KEY_DAILY_NUTRITION, json).apply()
    }

    fun getDailyNutrition(): DailyNutrition {
        val json = sharedPreferences.getString(KEY_DAILY_NUTRITION, null)
        return if (json != null) {
            gson.fromJson(json, DailyNutrition::class.java)
        } else {
            DailyNutrition() // Return default
        }
    }

    // Achievements Management
    fun saveAchievements(achievements: List<Achievement>) {
        val json = gson.toJson(achievements)
        sharedPreferences.edit().putString(KEY_ACHIEVEMENTS, json).apply()
    }

    fun getAchievements(): MutableList<Achievement> {
        val json = sharedPreferences.getString(KEY_ACHIEVEMENTS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Achievement>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun unlockAchievement(achievementId: String) {
        val achievements = getAchievements()
        val achievementIndex = achievements.indexOfFirst { it.id == achievementId }

        if (achievementIndex >= 0 && !achievements[achievementIndex].isUnlocked) {
            val unlockedAchievement = achievements[achievementIndex].copy(
                isUnlocked = true,
                unlockedDate = Date()
            )
            achievements[achievementIndex] = unlockedAchievement
            saveAchievements(achievements)
        }
    }

    // App State Management
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    // Analytics Helper Functions
    fun getWeeklyWorkoutMinutes(): Int {
        val workouts = getWorkouts()
        val calendar = Calendar.getInstance()
        val currentWeekStart = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return workouts.filter { workout ->
            workout.date.after(currentWeekStart)
        }.sumOf { it.duration }
    }

    fun getWeeklyCaloriesBurned(): Int {
        val workouts = getWorkouts()
        val calendar = Calendar.getInstance()
        val currentWeekStart = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return workouts.filter { workout ->
            workout.date.after(currentWeekStart)
        }.sumOf { it.calories }
    }

    fun getTodaysCaloriesConsumed(): Int {
        val meals = getMeals()
        val today = Calendar.getInstance()
        return meals.filter { meal ->
            val mealDate = Calendar.getInstance().apply { time = meal.date }
            today.get(Calendar.YEAR) == mealDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == mealDate.get(Calendar.DAY_OF_YEAR)
        }.sumOf { it.totalCalories }
    }

    // Data Export/Import
    fun exportAllData(): String {
        val allData = mapOf(
            "userProfile" to getUserProfile(),
            "workouts" to getWorkouts(),
            "meals" to getMeals(),
            "activeGoals" to getActiveGoals(),
            "completedGoals" to getCompletedGoals(),
            "reminders" to getReminders(),
            "achievements" to getAchievements(),
            "dailyNutrition" to getDailyNutrition()
        )
        return gson.toJson(allData)
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}