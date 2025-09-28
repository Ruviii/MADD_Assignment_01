package com.example.madd_assignment_01.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo
import java.util.*

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val passwordHash: String,
    val name: String,
    val age: Int?,
    val height: Int?, // cm
    val currentWeight: Double?, // kg
    val targetWeight: Double?, // kg
    val activityLevel: String,
    val profileImageUrl: String?,
    val phoneNumber: String?,
    val dateOfBirth: String?, // DD/MM/YYYY format
    val gender: String?, // Male, Female, Other
    val fitnessGoal: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
) {
    companion object {
        fun create(
            email: String,
            passwordHash: String,
            name: String,
            age: Int? = null,
            height: Int? = null,
            currentWeight: Double? = null,
            targetWeight: Double? = null,
            activityLevel: String = "Moderate",
            profileImageUrl: String? = null,
            phoneNumber: String? = null,
            dateOfBirth: String? = null,
            gender: String? = null,
            fitnessGoal: String? = null
        ): User {
            val currentTime = System.currentTimeMillis()
            return User(
                id = UUID.randomUUID().toString(),
                email = email,
                passwordHash = passwordHash,
                name = name,
                age = age,
                height = height,
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                activityLevel = activityLevel,
                profileImageUrl = profileImageUrl,
                phoneNumber = phoneNumber,
                dateOfBirth = dateOfBirth,
                gender = gender,
                fitnessGoal = fitnessGoal,
                createdAt = currentTime,
                updatedAt = currentTime,
                isActive = true
            )
        }
    }
}