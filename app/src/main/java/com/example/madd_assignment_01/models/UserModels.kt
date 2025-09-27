package com.example.madd_assignment_01.models

import java.util.*

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val dateOfBirth: Date? = null,
    val gender: Gender? = null,
    val height: Int? = null, // cm
    val currentWeight: Double? = null, // kg
    val targetWeight: Double? = null, // kg
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val fitnessGoal: FitnessGoal = FitnessGoal.MAINTAIN_WEIGHT,
    val profileImageUrl: String? = null,
    val joinDate: Date = Date(),
    val lastActiveDate: Date = Date(),
    val preferences: UserPreferences = UserPreferences(),
    val isOnboardingComplete: Boolean = false
) {
    val age: Int?
        get() = dateOfBirth?.let {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            calendar.time = it
            currentYear - calendar.get(Calendar.YEAR)
        }

    val bmi: Double?
        get() = if (height != null && currentWeight != null) {
            val heightInMeters = height / 100.0
            currentWeight / (heightInMeters * heightInMeters)
        } else null

    val bmiCategory: BMICategory?
        get() = bmi?.let { BMICategory.fromBMI(it) }
}

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    SEDENTARY("Sedentary (little/no exercise)", 1.2),
    LIGHT("Light (light exercise 1-3 days/week)", 1.375),
    MODERATE("Moderate (moderate exercise 3-5 days/week)", 1.55),
    ACTIVE("Active (hard exercise 6-7 days/week)", 1.725),
    VERY_ACTIVE("Very Active (very hard exercise & physical job)", 1.9)
}

enum class FitnessGoal(val displayName: String) {
    LOSE_WEIGHT("Lose Weight"),
    MAINTAIN_WEIGHT("Maintain Weight"),
    GAIN_WEIGHT("Gain Weight"),
    BUILD_MUSCLE("Build Muscle"),
    IMPROVE_ENDURANCE("Improve Endurance"),
    GENERAL_FITNESS("General Fitness")
}

enum class BMICategory(val displayName: String, val range: ClosedFloatingPointRange<Double>) {
    UNDERWEIGHT("Underweight", 0.0..18.4),
    NORMAL("Normal", 18.5..24.9),
    OVERWEIGHT("Overweight", 25.0..29.9),
    OBESE("Obese", 30.0..Double.MAX_VALUE);

    companion object {
        fun fromBMI(bmi: Double): BMICategory {
            return values().find { bmi in it.range } ?: NORMAL
        }
    }
}

data class UserPreferences(
    val units: UnitSystem = UnitSystem.METRIC,
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: String = "en",
    val timezone: String = TimeZone.getDefault().id,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val notificationSettings: NotificationSettings = NotificationSettings(userId = "")
)

enum class UnitSystem(val displayName: String) {
    METRIC("Metric (kg, cm)"),
    IMPERIAL("Imperial (lbs, ft)")
}

enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}

data class PrivacySettings(
    val shareWorkouts: Boolean = false,
    val shareProgress: Boolean = false,
    val allowFriendRequests: Boolean = true,
    val profileVisibility: ProfileVisibility = ProfileVisibility.FRIENDS_ONLY
)

enum class ProfileVisibility(val displayName: String) {
    PUBLIC("Public"),
    FRIENDS_ONLY("Friends Only"),
    PRIVATE("Private")
}

data class UserStats(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val totalWorkouts: Int = 0,
    val totalWorkoutMinutes: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val goalsCompleted: Int = 0,
    val achievementsUnlocked: Int = 0,
    val lastUpdated: Date = Date()
)

data class WeightEntry(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val weight: Double, // kg
    val date: Date = Date(),
    val notes: String = ""
)

data class BodyMeasurement(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: Date = Date(),
    val weight: Double? = null, // kg
    val bodyFatPercentage: Double? = null,
    val muscleMass: Double? = null, // kg
    val waist: Double? = null, // cm
    val chest: Double? = null, // cm
    val arms: Double? = null, // cm
    val thighs: Double? = null, // cm
    val notes: String = ""
)