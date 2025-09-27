package com.example.madd_assignment_01.database.dao

import androidx.room.*
import com.example.madd_assignment_01.database.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 ORDER BY deadline ASC")
    fun getActiveGoalsByUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoalsByUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND category = :category ORDER BY deadline ASC")
    fun getGoalsByCategory(userId: String, category: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND deadline < :currentTime AND isCompleted = 0")
    fun getOverdueGoals(userId: String, currentTime: Long): Flow<List<GoalEntity>>

    @Insert
    fun insertGoal(goal: GoalEntity): Long

    @Update
    fun updateGoal(goal: GoalEntity): Int

    @Query("UPDATE goals SET currentValue = :currentValue, currentNumericValue = :currentNumericValue WHERE id = :goalId")
    fun updateGoalProgress(goalId: String, currentValue: String, currentNumericValue: Double): Int

    @Query("UPDATE goals SET isCompleted = 1, completedAt = :completedAt WHERE id = :goalId")
    fun completeGoal(goalId: String, completedAt: Long): Int

    @Delete
    fun deleteGoal(goal: GoalEntity): Int

    @Query("DELETE FROM goals WHERE id = :goalId")
    fun deleteGoalById(goalId: String): Int

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    fun getGoalById(goalId: String): GoalEntity?

    @Query("DELETE FROM goals WHERE userId = :userId")
    fun deleteAllGoalsForUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedGoalsCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 0")
    fun getActiveGoalsCount(userId: String): Int

    // Additional analytics methods for GoalDao
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    fun getTotalGoalsCount(userId: String): Int

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND category = :category ORDER BY deadline ASC")
    fun getGoalsByUserAndCategory(userId: String, category: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND priority = :priority ORDER BY deadline ASC")
    fun getGoalsByUserAndPriority(userId: String, priority: String): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE userId = :userId AND deadline < :currentTime AND isCompleted = 0")
    fun getOverdueGoalsByUser(userId: String, currentTime: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND deadline <= :deadlineThreshold AND isCompleted = 0 ORDER BY deadline ASC")
    fun getGoalsNearDeadline(userId: String, deadlineThreshold: Long): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentGoalsByUser(userId: String, limit: Int): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE userId IS NULL OR userId NOT IN (SELECT id FROM users)")
    fun getOrphanedGoals(): List<GoalEntity>

    @Query("DELETE FROM goals WHERE isCompleted = 1 AND completedAt < :cutoffDate")
    fun deleteCompletedGoalsOlderThan(cutoffDate: Long): Int
}