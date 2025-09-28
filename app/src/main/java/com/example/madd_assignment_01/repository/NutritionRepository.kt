package com.example.madd_assignment_01.repository

import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.database.entities.*
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class NutritionRepository(
    private val mealDao: MealDao,
    private val foodItemDao: FoodItemDao,
    private val userRepository: UserRepository
) {

    // Meal Operations
    suspend fun addMeal(meal: Meal): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val mealEntity = MealEntity(
                id = meal.id,
                userId = userId,
                type = meal.type.name,
                time = meal.time,
                date = meal.date.time,
                totalCalories = meal.totalCalories,
                totalProtein = meal.totalProtein,
                totalCarbs = meal.totalCarbs,
                totalFat = meal.totalFat
            )

            mealDao.insertMeal(mealEntity)

            // Add associated food items
            meal.foodItems.forEach { selectedFood ->
                val foodEntity = FoodItemEntity(
                    id = UUID.randomUUID().toString(),
                    mealId = meal.id,
                    name = selectedFood.foodItem.name,
                    caloriesPerServing = selectedFood.foodItem.caloriesPerServing,
                    servingSize = selectedFood.foodItem.servingSize,
                    protein = selectedFood.foodItem.protein,
                    carbs = selectedFood.foodItem.carbs,
                    fat = selectedFood.foodItem.fat,
                    fiber = selectedFood.foodItem.fiber,
                    sugar = selectedFood.foodItem.sugar,
                    sodium = selectedFood.foodItem.sodium,
                    category = selectedFood.foodItem.category.name,
                    quantity = selectedFood.quantity,
                    customPortion = selectedFood.customPortion
                )
                foodItemDao.insertFoodItem(foodEntity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMealsByDate(date: Date): Flow<List<Meal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        return mealDao.getMealsByUserAndDateRange(userId, startOfDay, startOfNextDay).map { entities ->
            entities.map { mealEntity ->
                val foodItems = foodItemDao.getFoodItemsByMealId(mealEntity.id).map { foodEntity ->
                    SelectedFoodItem(
                        foodItem = FoodItem(
                            id = foodEntity.id,
                            name = foodEntity.name,
                            caloriesPerServing = foodEntity.caloriesPerServing,
                            servingSize = foodEntity.servingSize,
                            protein = foodEntity.protein,
                            carbs = foodEntity.carbs,
                            fat = foodEntity.fat,
                            fiber = foodEntity.fiber,
                            sugar = foodEntity.sugar,
                            sodium = foodEntity.sodium,
                            category = FoodCategory.valueOf(foodEntity.category)
                        ),
                        quantity = foodEntity.quantity,
                        customPortion = foodEntity.customPortion
                    )
                }.toMutableList()

                Meal(
                    id = mealEntity.id,
                    type = MealType.valueOf(mealEntity.type),
                    time = mealEntity.time,
                    date = Date(mealEntity.date),
                    foodItems = foodItems
                )
            }
        }
    }

    fun getMealsByType(mealType: MealType): Flow<List<Meal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return mealDao.getMealsByUserAndType(userId, mealType.name).map { entities ->
            entities.map { mealEntity ->
                val foodItems = foodItemDao.getFoodItemsByMealId(mealEntity.id).map { foodEntity ->
                    SelectedFoodItem(
                        foodItem = FoodItem(
                            id = foodEntity.id,
                            name = foodEntity.name,
                            caloriesPerServing = foodEntity.caloriesPerServing,
                            servingSize = foodEntity.servingSize,
                            protein = foodEntity.protein,
                            carbs = foodEntity.carbs,
                            fat = foodEntity.fat,
                            fiber = foodEntity.fiber,
                            sugar = foodEntity.sugar,
                            sodium = foodEntity.sodium,
                            category = FoodCategory.valueOf(foodEntity.category)
                        ),
                        quantity = foodEntity.quantity,
                        customPortion = foodEntity.customPortion
                    )
                }.toMutableList()

                Meal(
                    id = mealEntity.id,
                    type = MealType.valueOf(mealEntity.type),
                    time = mealEntity.time,
                    date = Date(mealEntity.date),
                    foodItems = foodItems
                )
            }
        }
    }

    suspend fun updateMeal(meal: Meal): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val mealEntity = MealEntity(
                id = meal.id,
                userId = userId,
                type = meal.type.name,
                time = meal.time,
                date = meal.date.time,
                totalCalories = meal.totalCalories,
                totalProtein = meal.totalProtein,
                totalCarbs = meal.totalCarbs,
                totalFat = meal.totalFat
            )

            mealDao.updateMeal(mealEntity)

            // Delete existing food items and add updated ones
            foodItemDao.deleteFoodItemsByMealId(meal.id)
            meal.foodItems.forEach { selectedFood ->
                val foodEntity = FoodItemEntity(
                    id = UUID.randomUUID().toString(),
                    mealId = meal.id,
                    name = selectedFood.foodItem.name,
                    caloriesPerServing = selectedFood.foodItem.caloriesPerServing,
                    servingSize = selectedFood.foodItem.servingSize,
                    protein = selectedFood.foodItem.protein,
                    carbs = selectedFood.foodItem.carbs,
                    fat = selectedFood.foodItem.fat,
                    fiber = selectedFood.foodItem.fiber,
                    sugar = selectedFood.foodItem.sugar,
                    sodium = selectedFood.foodItem.sodium,
                    category = selectedFood.foodItem.category.name,
                    quantity = selectedFood.quantity,
                    customPortion = selectedFood.customPortion
                )
                foodItemDao.insertFoodItem(foodEntity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMeal(mealId: String): Result<Unit> {
        return try {
            foodItemDao.deleteFoodItemsByMealId(mealId)
            mealDao.deleteMealById(mealId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMeal(mealId: String): Meal? {
        return try {
            val mealEntity = mealDao.getMealById(mealId) ?: return null
            val foodItems = foodItemDao.getFoodItemsByMealId(mealId).map { foodEntity ->
                SelectedFoodItem(
                    foodItem = FoodItem(
                        id = foodEntity.id,
                        name = foodEntity.name,
                        caloriesPerServing = foodEntity.caloriesPerServing,
                        servingSize = foodEntity.servingSize,
                        protein = foodEntity.protein,
                        carbs = foodEntity.carbs,
                        fat = foodEntity.fat,
                        fiber = foodEntity.fiber,
                        sugar = foodEntity.sugar,
                        sodium = foodEntity.sodium,
                        category = FoodCategory.valueOf(foodEntity.category)
                    ),
                    quantity = foodEntity.quantity,
                    customPortion = foodEntity.customPortion
                )
            }.toMutableList()

            Meal(
                id = mealEntity.id,
                type = MealType.valueOf(mealEntity.type),
                time = mealEntity.time,
                date = Date(mealEntity.date),
                foodItems = foodItems
            )
        } catch (e: Exception) {
            null
        }
    }

    // Today's data methods for dashboard
    suspend fun getTodaysMeals(): List<Meal> {
        val today = Date()
        val userId = userRepository.getCurrentUserId() ?: return emptyList()

        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        return try {
            // Simplified approach - return empty list for now
            // In a real implementation, this would use proper Flow collection
            emptyList<Meal>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Nutrition Analytics
    suspend fun getDailyCalories(date: Date): Int {
        val userId = userRepository.getCurrentUserId() ?: return 0

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        return mealDao.getTotalCaloriesForPeriod(userId, startOfDay, startOfNextDay) ?: 0
    }

    suspend fun getDailyMacros(date: Date): DailyNutrition {
        val userId = userRepository.getCurrentUserId() ?: return DailyNutrition()

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        val totalProtein = mealDao.getTotalProteinForPeriod(userId, startOfDay, startOfNextDay) ?: 0.0
        val totalCarbs = mealDao.getTotalCarbsForPeriod(userId, startOfDay, startOfNextDay) ?: 0.0
        val totalFat = mealDao.getTotalFatForPeriod(userId, startOfDay, startOfNextDay) ?: 0.0

        return DailyNutrition(
            targetProtein = totalProtein,
            targetCarbs = totalCarbs,
            targetFat = totalFat
        )
    }

    suspend fun getWeeklyCalories(): Int {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        return getCaloriesForPeriod(startDate, endDate)
    }

    suspend fun getMonthlyCalories(): Int {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.time

        return getCaloriesForPeriod(startDate, endDate)
    }

    suspend fun getCaloriesForPeriod(startDate: Date, endDate: Date): Int {
        val userId = userRepository.getCurrentUserId() ?: return 0
        return mealDao.getTotalCaloriesForPeriod(userId, startDate.time, endDate.time) ?: 0
    }

    suspend fun getNutritionGoals(): NutritionGoals? {
        val userId = userRepository.getCurrentUserId() ?: return null
        // This would need a separate DAO for nutrition goals
        // For now, return default values
        return NutritionGoals(
            userId = userId,
            targetCalories = 2000,
            targetProtein = 150.0,
            targetCarbs = 250.0,
            targetFat = 65.0,
            targetWater = 2000.0
        )
    }

    // Water Intake Methods (placeholder for future implementation)
    suspend fun addWaterIntake(waterIntake: WaterIntake): Result<Unit> {
        return try {
            // Would need a separate WaterIntakeDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyWaterIntake(date: Date): Double {
        // Would need implementation with WaterIntakeDao
        return 0.0
    }

    // Meal Planning Methods
    suspend fun getMealPlans(): List<MealPlan> {
        // Would need implementation with MealPlanDao
        return emptyList()
    }

    suspend fun saveMealPlan(mealPlan: MealPlan): Result<Unit> {
        return try {
            // Would need implementation with MealPlanDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}