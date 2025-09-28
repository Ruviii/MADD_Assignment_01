package com.example.madd_assignment_01.repository

import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.database.entities.*
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val userRepository: UserRepository
) {

    // CRUD Operations
    suspend fun addWorkout(workout: WorkoutRecord): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val workoutEntity = WorkoutEntity(
                id = workout.id,
                userId = userId,
                name = workout.name,
                type = workout.type.name,
                date = workout.date.time,
                duration = workout.duration,
                calories = workout.calories,
                notes = workout.notes
            )

            workoutDao.insertWorkout(workoutEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getWorkouts(): Flow<List<WorkoutRecord>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return workoutDao.getWorkoutsByUser(userId).map { entities ->
            entities.map { entity ->
                WorkoutRecord(
                    id = entity.id,
                    name = entity.name,
                    type = WorkoutType.valueOf(entity.type),
                    date = Date(entity.date),
                    duration = entity.duration,
                    calories = entity.calories,
                    notes = entity.notes ?: ""
                )
            }
        }
    }

    fun getWorkoutsByType(type: WorkoutType): Flow<List<WorkoutRecord>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return workoutDao.getWorkoutsByUserAndType(userId, type.name).map { entities ->
            entities.map { entity ->
                WorkoutRecord(
                    id = entity.id,
                    name = entity.name,
                    type = WorkoutType.valueOf(entity.type),
                    date = Date(entity.date),
                    duration = entity.duration,
                    calories = entity.calories,
                    notes = entity.notes ?: ""
                )
            }
        }
    }

    fun getWorkoutsByDateRange(startDate: Date, endDate: Date): Flow<List<WorkoutRecord>> {
        val userId = userRepository.getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return workoutDao.getWorkoutsByDateRange(userId, startDate.time, endDate.time).map { entities ->
            entities.map { entity ->
                WorkoutRecord(
                    id = entity.id,
                    name = entity.name,
                    type = WorkoutType.valueOf(entity.type),
                    date = Date(entity.date),
                    duration = entity.duration,
                    calories = entity.calories,
                    notes = entity.notes ?: ""
                )
            }
        }
    }

    suspend fun updateWorkout(workout: WorkoutRecord): Result<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val workoutEntity = WorkoutEntity(
                id = workout.id,
                userId = userId,
                name = workout.name,
                type = workout.type.name,
                date = workout.date.time,
                duration = workout.duration,
                calories = workout.calories,
                notes = workout.notes
            )

            workoutDao.updateWorkout(workoutEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWorkout(workoutId: String): Result<Unit> {
        return try {
            workoutDao.deleteWorkoutById(workoutId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkout(workoutId: String): WorkoutRecord? {
        return try {
            val entity = workoutDao.getWorkoutById(workoutId)
            entity?.let {
                WorkoutRecord(
                    id = it.id,
                    name = it.name,
                    type = WorkoutType.valueOf(it.type),
                    date = Date(it.date),
                    duration = it.duration,
                    calories = it.calories,
                    notes = it.notes ?: ""
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Analytics Methods
    suspend fun getTotalWorkoutMinutes(startDate: Date, endDate: Date): Int {
        val userId = userRepository.getCurrentUserId() ?: return 0
        return workoutDao.getTotalWorkoutMinutes(userId, startDate.time, endDate.time) ?: 0
    }

    suspend fun getTotalCaloriesBurned(startDate: Date, endDate: Date): Int {
        val userId = userRepository.getCurrentUserId() ?: return 0
        return workoutDao.getTotalCaloriesBurned(userId, startDate.time, endDate.time) ?: 0
    }

    suspend fun getWorkoutCount(startDate: Date, endDate: Date): Int {
        val userId = userRepository.getCurrentUserId() ?: return 0
        return workoutDao.getWorkoutCount(userId, startDate.time, endDate.time)
    }

    suspend fun getWeeklyWorkoutMinutes(): Int {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        return getTotalWorkoutMinutes(startDate, endDate)
    }

    suspend fun getWeeklyCaloriesBurned(): Int {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        return getTotalCaloriesBurned(startDate, endDate)
    }

    suspend fun getMonthlyWorkoutStats(): WorkoutStats {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.time

        val totalMinutes = getTotalWorkoutMinutes(startDate, endDate)
        val totalCalories = getTotalCaloriesBurned(startDate, endDate)
        val workoutCount = getWorkoutCount(startDate, endDate)

        return WorkoutStats(
            totalWorkouts = workoutCount,
            totalMinutes = totalMinutes,
            totalCalories = totalCalories,
            averageWorkoutLength = if (workoutCount > 0) totalMinutes / workoutCount else 0,
            period = "Last 30 days"
        )
    }

    suspend fun getWorkoutFrequencyByType(): Map<WorkoutType, Int> {
        val userId = userRepository.getCurrentUserId() ?: return emptyMap()

        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.MONTH, -3) // Last 3 months
        val startDate = calendar.time

        val frequencyMap = mutableMapOf<WorkoutType, Int>()

        for (type in WorkoutType.values()) {
            val count = workoutDao.getWorkoutsByUserAndType(userId, type.name)
            // This would need to be implemented differently with actual Flow collection
            // For now, returning empty map as placeholder
        }

        return frequencyMap
    }

    // Streak calculations
    suspend fun getCurrentWorkoutStreak(): Int {
        // Implementation for calculating current workout streak
        // This would involve getting workouts and checking consecutive days
        return 0 // Placeholder
    }

    suspend fun getLongestWorkoutStreak(): Int {
        // Implementation for calculating longest workout streak
        // This would involve analyzing historical workout data
        return 0 // Placeholder
    }

    // Dashboard specific methods
    suspend fun getTodaysWorkouts(): List<WorkoutRecord> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()
        val today = Date()

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
            emptyList<WorkoutRecord>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeeklyWorkouts(): List<WorkoutRecord> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()

        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        return try {
            // Simplified approach - return empty list for now
            emptyList<WorkoutRecord>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentWorkouts(limit: Int = 5): List<WorkoutRecord> {
        return try {
            emptyList<WorkoutRecord>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class WorkoutStats(
        val totalWorkouts: Int,
        val totalMinutes: Int,
        val totalCalories: Int,
        val averageWorkoutLength: Int,
        val period: String
    )
}