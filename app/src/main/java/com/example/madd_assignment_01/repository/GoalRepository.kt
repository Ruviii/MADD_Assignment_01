package com.example.madd_assignment_01.repository

import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.database.entities.*
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class GoalRepository(
    private val goalDao: GoalDao,
    private val userRepository: UserRepository
) {

    // Goal CRUD Operations
    suspend fun addGoal(goal: Goal): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val goalEntity = GoalEntity(
                id = goal.id,
                userId = userId,
                name = goal.name,
                category = goal.category.name,
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                currentNumericValue = goal.currentNumericValue,
                targetNumericValue = goal.targetNumericValue,
                deadline = goal.deadline.time,
                createdAt = goal.createdAt.time,
                isCompleted = goal.isCompleted,
                completedAt = goal.completedAt?.time,
                priority = goal.priority.name,
                description = goal.description
            )

            goalDao.insertGoal(goalEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoals(): Flow<List<Goal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return goalDao.getGoalsByUser(userId).map { entities ->
            entities.map { entity ->
                Goal(
                    id = entity.id,
                    name = entity.name,
                    category = GoalCategory.valueOf(entity.category),
                    currentValue = entity.currentValue,
                    targetValue = entity.targetValue,
                    currentNumericValue = entity.currentNumericValue,
                    targetNumericValue = entity.targetNumericValue,
                    deadline = Date(entity.deadline),
                    createdAt = Date(entity.createdAt),
                    isCompleted = entity.isCompleted,
                    completedAt = entity.completedAt?.let { Date(it) },
                    priority = Priority.valueOf(entity.priority),
                    description = entity.description
                )
            }
        }
    }

    fun getActiveGoals(): Flow<List<Goal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return goalDao.getActiveGoalsByUser(userId).map { entities ->
            entities.map { entity ->
                Goal(
                    id = entity.id,
                    name = entity.name,
                    category = GoalCategory.valueOf(entity.category),
                    currentValue = entity.currentValue,
                    targetValue = entity.targetValue,
                    currentNumericValue = entity.currentNumericValue,
                    targetNumericValue = entity.targetNumericValue,
                    deadline = Date(entity.deadline),
                    createdAt = Date(entity.createdAt),
                    isCompleted = entity.isCompleted,
                    completedAt = entity.completedAt?.let { Date(it) },
                    priority = Priority.valueOf(entity.priority),
                    description = entity.description
                )
            }
        }
    }

    fun getGoalsByCategory(category: GoalCategory): Flow<List<Goal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return goalDao.getGoalsByUserAndCategory(userId, category.name).map { entities ->
            entities.map { entity ->
                Goal(
                    id = entity.id,
                    name = entity.name,
                    category = GoalCategory.valueOf(entity.category),
                    currentValue = entity.currentValue,
                    targetValue = entity.targetValue,
                    currentNumericValue = entity.currentNumericValue,
                    targetNumericValue = entity.targetNumericValue,
                    deadline = Date(entity.deadline),
                    createdAt = Date(entity.createdAt),
                    isCompleted = entity.isCompleted,
                    completedAt = entity.completedAt?.let { Date(it) },
                    priority = Priority.valueOf(entity.priority),
                    description = entity.description
                )
            }
        }
    }

    fun getOverdueGoals(): Flow<List<Goal>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        val currentTime = Date().time

        return goalDao.getOverdueGoalsByUser(userId, currentTime).map { entities ->
            entities.map { entity ->
                Goal(
                    id = entity.id,
                    name = entity.name,
                    category = GoalCategory.valueOf(entity.category),
                    currentValue = entity.currentValue,
                    targetValue = entity.targetValue,
                    currentNumericValue = entity.currentNumericValue,
                    targetNumericValue = entity.targetNumericValue,
                    deadline = Date(entity.deadline),
                    createdAt = Date(entity.createdAt),
                    isCompleted = entity.isCompleted,
                    completedAt = entity.completedAt?.let { Date(it) },
                    priority = Priority.valueOf(entity.priority),
                    description = entity.description
                )
            }
        }
    }

    suspend fun updateGoal(goal: Goal): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val goalEntity = GoalEntity(
                id = goal.id,
                userId = userId,
                name = goal.name,
                category = goal.category.name,
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                currentNumericValue = goal.currentNumericValue,
                targetNumericValue = goal.targetNumericValue,
                deadline = goal.deadline.time,
                createdAt = goal.createdAt.time,
                isCompleted = goal.isCompleted,
                completedAt = goal.completedAt?.time,
                priority = goal.priority.name,
                description = goal.description
            )

            goalDao.updateGoal(goalEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit> {
        return try {
            val goal = getGoal(goalId) ?: return Result.failure(Exception("Goal not found"))

            val updatedGoal = goal.copy(
                currentNumericValue = newValue,
                currentValue = newValue.toString(),
                isCompleted = newValue >= goal.targetNumericValue,
                completedAt = if (newValue >= goal.targetNumericValue) Date() else null
            )

            updateGoal(updatedGoal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeGoal(goalId: String): Result<Unit> {
        return try {
            val goal = getGoal(goalId) ?: return Result.failure(Exception("Goal not found"))

            val completedGoal = goal.copy(
                isCompleted = true,
                completedAt = Date(),
                currentNumericValue = goal.targetNumericValue,
                currentValue = goal.targetValue
            )

            updateGoal(completedGoal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            goalDao.deleteGoalById(goalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGoal(goalId: String): Goal? {
        return try {
            val entity = goalDao.getGoalById(goalId)
            entity?.let {
                Goal(
                    id = it.id,
                    name = it.name,
                    category = GoalCategory.valueOf(it.category),
                    currentValue = it.currentValue,
                    targetValue = it.targetValue,
                    currentNumericValue = it.currentNumericValue,
                    targetNumericValue = it.targetNumericValue,
                    deadline = Date(it.deadline),
                    createdAt = Date(it.createdAt),
                    isCompleted = it.isCompleted,
                    completedAt = it.completedAt?.let { date -> Date(date) },
                    priority = Priority.valueOf(it.priority),
                    description = it.description
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Goal Analytics and Statistics
    suspend fun getGoalCompletionRate(): Double {
        val userId = userRepository.getCurrentUserId() ?: return 0.0
        val totalGoals = goalDao.getTotalGoalsCount(userId)
        val completedGoals = goalDao.getCompletedGoalsCount(userId)

        return if (totalGoals > 0) {
            (completedGoals.toDouble() / totalGoals.toDouble()) * 100
        } else 0.0
    }

    suspend fun getGoalsByPriority(priority: Priority): List<Goal> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()

        return goalDao.getGoalsByUserAndPriority(userId, priority.name).map { entity ->
            Goal(
                id = entity.id,
                name = entity.name,
                category = GoalCategory.valueOf(entity.category),
                currentValue = entity.currentValue,
                targetValue = entity.targetValue,
                currentNumericValue = entity.currentNumericValue,
                targetNumericValue = entity.targetNumericValue,
                deadline = Date(entity.deadline),
                createdAt = Date(entity.createdAt),
                isCompleted = entity.isCompleted,
                completedAt = entity.completedAt?.let { Date(it) },
                priority = Priority.valueOf(entity.priority),
                description = entity.description
            )
        }
    }

    suspend fun getRecentGoals(limit: Int = 5): List<Goal> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()

        return goalDao.getRecentGoalsByUser(userId, limit).map { entity ->
            Goal(
                id = entity.id,
                name = entity.name,
                category = GoalCategory.valueOf(entity.category),
                currentValue = entity.currentValue,
                targetValue = entity.targetValue,
                currentNumericValue = entity.currentNumericValue,
                targetNumericValue = entity.targetNumericValue,
                deadline = Date(entity.deadline),
                createdAt = Date(entity.createdAt),
                isCompleted = entity.isCompleted,
                completedAt = entity.completedAt?.let { Date(it) },
                priority = Priority.valueOf(entity.priority),
                description = entity.description
            )
        }
    }

    suspend fun getGoalsNearDeadline(daysAhead: Int = 7): List<Goal> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
        val deadlineThreshold = calendar.time.time

        return goalDao.getGoalsNearDeadline(userId, deadlineThreshold).map { entity ->
            Goal(
                id = entity.id,
                name = entity.name,
                category = GoalCategory.valueOf(entity.category),
                currentValue = entity.currentValue,
                targetValue = entity.targetValue,
                currentNumericValue = entity.currentNumericValue,
                targetNumericValue = entity.targetNumericValue,
                deadline = Date(entity.deadline),
                createdAt = Date(entity.createdAt),
                isCompleted = entity.isCompleted,
                completedAt = entity.completedAt?.let { Date(it) },
                priority = Priority.valueOf(entity.priority),
                description = entity.description
            )
        }
    }

    // Goal Templates (for future implementation)
    suspend fun getGoalTemplates(): List<GoalTemplate> {
        return listOf(
            GoalTemplate(
                name = "Lose 5kg",
                category = GoalCategory.WEIGHT,
                description = "Healthy weight loss goal",
                targetValue = "5 kg",
                targetNumericValue = 5.0,
                recommendedDuration = 60,
                difficulty = DifficultyLevel.MEDIUM,
                tips = listOf("Eat in calorie deficit", "Exercise regularly", "Stay hydrated")
            ),
            GoalTemplate(
                name = "Run 10km",
                category = GoalCategory.CARDIO,
                description = "Build running endurance",
                targetValue = "10 km",
                targetNumericValue = 10.0,
                recommendedDuration = 90,
                difficulty = DifficultyLevel.HARD,
                tips = listOf("Start with shorter distances", "Build gradually", "Focus on consistency")
            ),
            GoalTemplate(
                name = "Drink 2L water daily",
                category = GoalCategory.HYDRATION,
                description = "Stay properly hydrated",
                targetValue = "2 L",
                targetNumericValue = 2.0,
                recommendedDuration = 30,
                difficulty = DifficultyLevel.EASY,
                tips = listOf("Carry water bottle", "Set reminders", "Track intake")
            )
        )
    }

    // Progress tracking
    suspend fun addGoalProgress(goalProgress: GoalProgress): Result<Unit> {
        return try {
            // This would need a separate GoalProgressDao
            // For now, just update the goal's current value
            updateGoalProgress(goalProgress.goalId, goalProgress.numericValue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGoalProgressHistory(goalId: String): List<GoalProgress> {
        // Would need implementation with GoalProgressDao
        return emptyList()
    }

    // Achievements (placeholder for future implementation)
    suspend fun getUserAchievements(): List<Achievement> {
        // Would need implementation with AchievementDao
        return emptyList()
    }

    suspend fun unlockAchievement(achievementId: String): Result<Unit> {
        return try {
            // Would need implementation with AchievementDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Challenge system (placeholder for future implementation)
    suspend fun getAvailableChallenges(): List<Challenge> {
        // Would need implementation with ChallengeDao
        return emptyList()
    }

    suspend fun joinChallenge(challengeId: String): Result<Unit> {
        return try {
            // Would need implementation with ChallengeDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

