package com.example.madd_assignment_01.database.dao

import androidx.room.*
import com.example.madd_assignment_01.database.entities.MealEntity
import com.example.madd_assignment_01.database.entities.FoodItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY date DESC, time ASC")
    fun getMealsByUser(userId: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC, time ASC")
    fun getMealsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getMealsByType(userId: String, type: String): Flow<List<MealEntity>>

    @Query("SELECT SUM(totalCalories) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalCaloriesConsumed(userId: String, startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(totalProtein) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalProteinConsumed(userId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(totalCarbs) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalCarbsConsumed(userId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(totalFat) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalFatConsumed(userId: String, startDate: Long, endDate: Long): Double?

    @Insert
    fun insertMeal(meal: MealEntity): Long

    @Update
    fun updateMeal(meal: MealEntity): Int

    @Delete
    fun deleteMeal(meal: MealEntity): Int

    @Query("DELETE FROM meals WHERE id = :mealId")
    fun deleteMealById(mealId: String): Int

    @Query("SELECT * FROM meals WHERE id = :mealId LIMIT 1")
    fun getMealById(mealId: String): MealEntity?

    @Query("DELETE FROM meals WHERE userId = :userId")
    fun deleteAllMealsForUser(userId: String): Int

    // Additional analytics methods for MealDao
    @Query("SELECT COUNT(*) FROM meals WHERE userId = :userId")
    fun getTotalMealsCount(userId: String): Int

    @Query("SELECT * FROM meals WHERE userId IS NULL OR userId NOT IN (SELECT id FROM users)")
    fun getOrphanedMeals(): List<MealEntity>

    @Query("DELETE FROM meals WHERE date < :cutoffDate")
    fun deleteMealsOlderThan(cutoffDate: Long): Int

    @Query("SELECT MIN(date) FROM meals WHERE userId = :userId")
    fun getOldestMealDate(userId: String): Long?

    @Query("SELECT MAX(date) FROM meals WHERE userId = :userId")
    fun getNewestMealDate(userId: String): Long?

    // Nutrition analytics aliases for compatibility
    @Query("SELECT SUM(totalCalories) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalCaloriesForPeriod(userId: String, startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(totalProtein) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalProteinForPeriod(userId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(totalCarbs) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalCarbsForPeriod(userId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(totalFat) FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalFatForPeriod(userId: String, startDate: Long, endDate: Long): Double?

    // User and type specific methods
    @Query("SELECT * FROM meals WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC, time ASC")
    fun getMealsByUserAndDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getMealsByUserAndType(userId: String, type: String): Flow<List<MealEntity>>
}

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE mealId = :mealId")
    fun getFoodItemsByMeal(mealId: String): List<FoodItemEntity>

    @Insert
    fun insertFoodItem(foodItem: FoodItemEntity): Long

    @Insert
    fun insertFoodItems(foodItems: List<FoodItemEntity>)

    @Update
    fun updateFoodItem(foodItem: FoodItemEntity): Int

    @Delete
    fun deleteFoodItem(foodItem: FoodItemEntity): Int

    @Query("DELETE FROM food_items WHERE id = :foodItemId")
    fun deleteFoodItemById(foodItemId: String): Int

    @Query("DELETE FROM food_items WHERE mealId = :mealId")
    fun deleteFoodItemsByMeal(mealId: String): Int

    // Additional methods for FoodItemDao
    @Query("SELECT * FROM food_items WHERE mealId = :mealId")
    fun getFoodItemsByMealId(mealId: String): List<FoodItemEntity>

    @Query("DELETE FROM food_items WHERE mealId = :mealId")
    fun deleteFoodItemsByMealId(mealId: String): Int
}