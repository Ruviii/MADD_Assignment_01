package com.example.madd_assignment_01.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.madd_assignment_01.database.converters.StringListConverter
import com.example.madd_assignment_01.database.dao.*
import com.example.madd_assignment_01.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        WorkoutEntity::class,
        MealEntity::class,
        FoodItemEntity::class,
        GoalEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class HealthFitnessDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderDao(): ReminderDao

    private class HealthFitnessDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // Pre-populate the database with sample data if needed
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: HealthFitnessDatabase) {
            // Pre-populate any initial data here if needed
            // For example, default food items, workout types, etc.
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HealthFitnessDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): HealthFitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthFitnessDatabase::class.java,
                    "health_fitness_database"
                )
                .addCallback(HealthFitnessDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}