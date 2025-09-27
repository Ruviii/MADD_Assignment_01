package com.example.madd_assignment_01.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.*

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class WorkoutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val type: String, // CARDIO, STRENGTH, FLEXIBILITY, HIIT
    val date: Long,
    val duration: Int, // minutes
    val calories: Int,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)