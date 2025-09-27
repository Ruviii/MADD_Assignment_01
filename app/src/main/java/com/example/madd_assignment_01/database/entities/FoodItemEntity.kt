package com.example.madd_assignment_01.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.*

@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealId")]
)
data class FoodItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val name: String,
    val caloriesPerServing: Int,
    val servingSize: String,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val category: String = "OTHER",
    val quantity: Double = 1.0,
    val customPortion: String = ""
)