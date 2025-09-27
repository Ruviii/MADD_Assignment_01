package com.example.madd_assignment_01.database.dao

import androidx.room.*
import com.example.madd_assignment_01.database.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY date DESC")
    fun getWorkoutsByUser(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getWorkoutsByUserAndType(userId: String, type: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT SUM(duration) FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalWorkoutMinutes(userId: String, startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(calories) FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalCaloriesBurned(userId: String, startDate: Long, endDate: Long): Int?

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getWorkoutCount(userId: String, startDate: Long, endDate: Long): Int

    @Insert
    fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    fun updateWorkout(workout: WorkoutEntity): Int

    @Delete
    fun deleteWorkout(workout: WorkoutEntity): Int

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    fun deleteWorkoutById(workoutId: String): Int

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    fun getWorkoutById(workoutId: String): WorkoutEntity?

    @Query("DELETE FROM workouts WHERE userId = :userId")
    fun deleteAllWorkoutsForUser(userId: String): Int

    // Additional analytics methods
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    fun getTotalWorkoutsCount(userId: String): Int

    @Query("SELECT * FROM workouts WHERE userId IS NULL OR userId NOT IN (SELECT id FROM users)")
    fun getOrphanedWorkouts(): List<WorkoutEntity>

    @Query("DELETE FROM workouts WHERE date < :cutoffDate")
    fun deleteWorkoutsOlderThan(cutoffDate: Long): Int

    @Query("SELECT MIN(date) FROM workouts WHERE userId = :userId")
    fun getOldestWorkoutDate(userId: String): Long?

    @Query("SELECT MAX(date) FROM workouts WHERE userId = :userId")
    fun getNewestWorkoutDate(userId: String): Long?
}