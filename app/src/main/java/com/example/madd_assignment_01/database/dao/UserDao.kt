package com.example.madd_assignment_01.database.dao

import androidx.room.*
import com.example.madd_assignment_01.database.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND isActive = 1 LIMIT 1")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId AND isActive = 1 LIMIT 1")
    fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash AND isActive = 1 LIMIT 1")
    fun authenticateUser(email: String, passwordHash: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User): Int

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    fun softDeleteUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND isActive = 1")
    fun emailExists(email: String): Int

    @Query("UPDATE users SET updatedAt = :timestamp WHERE id = :userId")
    fun updateLastActivity(userId: String, timestamp: Long): Int

    @Query("UPDATE users SET name = :name, age = :age, height = :height, currentWeight = :currentWeight, targetWeight = :targetWeight, activityLevel = :activityLevel, profileImageUrl = :profileImageUrl, phoneNumber = :phoneNumber, dateOfBirth = :dateOfBirth, gender = :gender, fitnessGoal = :fitnessGoal, updatedAt = :timestamp WHERE id = :userId")
    fun updateUserProfile(
        userId: String,
        name: String,
        age: Int?,
        height: Int?,
        currentWeight: Double?,
        targetWeight: Double?,
        activityLevel: String,
        profileImageUrl: String?,
        phoneNumber: String?,
        dateOfBirth: String?,
        gender: String?,
        fitnessGoal: String?,
        timestamp: Long
    ): Int

    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveUsers(): Flow<List<User>>
}