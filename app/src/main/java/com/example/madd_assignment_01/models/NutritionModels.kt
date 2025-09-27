package com.example.madd_assignment_01.models

import java.util.*

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val caloriesPerServing: Int,
    val servingSize: String,
    val protein: Double, // grams
    val carbs: Double, // grams
    val fat: Double, // grams
    val fiber: Double = 0.0, // grams
    val sugar: Double = 0.0, // grams
    val sodium: Double = 0.0, // mg
    val category: FoodCategory = FoodCategory.OTHER,
    val barcode: String? = null
)

enum class FoodCategory(val displayName: String) {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    GRAINS("Grains"),
    PROTEIN("Protein"),
    DAIRY("Dairy"),
    FATS("Fats & Oils"),
    BEVERAGES("Beverages"),
    SNACKS("Snacks"),
    OTHER("Other")
}

data class SelectedFoodItem(
    val foodItem: FoodItem,
    val quantity: Double = 1.0,
    val customPortion: String = ""
) {
    val totalCalories: Int
        get() = (foodItem.caloriesPerServing * quantity).toInt()

    val totalProtein: Double
        get() = foodItem.protein * quantity

    val totalCarbs: Double
        get() = foodItem.carbs * quantity

    val totalFat: Double
        get() = foodItem.fat * quantity
}

data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val type: MealType,
    val time: String,
    val date: Date = Date(),
    val foodItems: MutableList<SelectedFoodItem> = mutableListOf()
) {
    val totalCalories: Int
        get() = foodItems.sumOf { it.totalCalories }

    val totalProtein: Double
        get() = foodItems.sumOf { it.totalProtein }

    val totalCarbs: Double
        get() = foodItems.sumOf { it.totalCarbs }

    val totalFat: Double
        get() = foodItems.sumOf { it.totalFat }
}

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromDisplayName(displayName: String): MealType {
            return values().find { it.displayName == displayName } ?: BREAKFAST
        }
    }
}

data class DailyNutrition(
    val targetCalories: Int = 2000,
    val targetProtein: Double = 150.0, // grams
    val targetCarbs: Double = 250.0, // grams
    val targetFat: Double = 65.0, // grams
    val targetFiber: Double = 25.0, // grams
    val targetSodium: Double = 2300.0 // mg
) {
    fun getConsumedCalories(meals: List<Meal>): Int {
        return meals.sumOf { it.totalCalories }
    }

    fun getRemainingCalories(meals: List<Meal>): Int {
        return (targetCalories - getConsumedCalories(meals)).coerceAtLeast(0)
    }

    fun getConsumedProtein(meals: List<Meal>): Double {
        return meals.sumOf { it.totalProtein }
    }

    fun getConsumedCarbs(meals: List<Meal>): Double {
        return meals.sumOf { it.totalCarbs }
    }

    fun getConsumedFat(meals: List<Meal>): Double {
        return meals.sumOf { it.totalFat }
    }

    fun getCalorieProgress(meals: List<Meal>): Int {
        return ((getConsumedCalories(meals).toDouble() / targetCalories) * 100).toInt().coerceAtMost(100)
    }
}

data class NutritionGoals(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val targetCalories: Int,
    val targetProtein: Double,
    val targetCarbs: Double,
    val targetFat: Double,
    val targetWater: Double = 2000.0, // ml
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)

data class WaterIntake(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double, // ml
    val timestamp: Date = Date(),
    val type: WaterType = WaterType.WATER
)

enum class WaterType(val displayName: String) {
    WATER("Water"),
    COFFEE("Coffee"),
    TEA("Tea"),
    JUICE("Juice"),
    OTHER("Other")
}

data class MealPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val meals: Map<MealType, List<FoodItem>>,
    val totalCalories: Int,
    val duration: Int, // days
    val tags: List<String> = emptyList()
)