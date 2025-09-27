package com.example.madd_assignment_01.database.dao

import androidx.room.*
import com.example.madd_assignment_01.database.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY time ASC")
    fun getRemindersByUser(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isEnabled = 1 ORDER BY time ASC")
    fun getActiveRemindersByUser(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND type = :type ORDER BY time ASC")
    fun getRemindersByType(userId: String, type: String): Flow<List<ReminderEntity>>

    @Insert
    fun insertReminder(reminder: ReminderEntity): Long

    @Update
    fun updateReminder(reminder: ReminderEntity): Int

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :reminderId")
    fun toggleReminder(reminderId: String, isEnabled: Boolean): Int

    @Delete
    fun deleteReminder(reminder: ReminderEntity): Int

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    fun deleteReminderById(reminderId: String): Int

    @Query("SELECT * FROM reminders WHERE id = :reminderId LIMIT 1")
    fun getReminderById(reminderId: String): ReminderEntity?

    @Query("DELETE FROM reminders WHERE userId = :userId")
    fun deleteAllRemindersForUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isEnabled = 1")
    fun getActiveRemindersCount(userId: String): Int

    // Additional analytics methods for ReminderDao
    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId")
    fun getTotalRemindersCount(userId: String): Int

    @Query("SELECT * FROM reminders WHERE userId = :userId AND type = :type ORDER BY time ASC")
    fun getRemindersByUserAndType(userId: String, type: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId IS NULL OR userId NOT IN (SELECT id FROM users)")
    fun getOrphanedReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isEnabled = 1 ORDER BY time ASC")
    fun getActiveRemindersList(userId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY time ASC")
    fun getRemindersList(userId: String): List<ReminderEntity>
}