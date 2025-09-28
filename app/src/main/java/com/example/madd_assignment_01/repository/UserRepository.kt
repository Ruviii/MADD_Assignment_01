package com.example.madd_assignment_01.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.madd_assignment_01.database.dao.UserDao
import com.example.madd_assignment_01.database.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class UserRepository(
    private val userDao: UserDao,
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Authentication methods
    suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                // Check if email already exists
                if (emailExists(email)) {
                    Result.failure(Exception("Email already registered"))
                } else {
                    // Hash password
                    val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

                    // Create new user
                    val user = User.create(
                        email = email.lowercase().trim(),
                        passwordHash = passwordHash,
                        name = name.trim()
                    )

                    // Insert user
                    userDao.insertUser(user)

                    // Save user session
                    saveUserSession(user)

                    Result.success(user)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val user = userDao.getUserByEmail(email.lowercase().trim())

                if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
                    // Update last activity
                    userDao.updateLastActivity(user.id, System.currentTimeMillis())

                    // Save user session
                    saveUserSession(user)

                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        clearUserSession()
    }

    // Session management
    private fun saveUserSession(user: User) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_name", user.name)
            putBoolean("is_logged_in", true)
            putLong("last_login", System.currentTimeMillis())
            apply()
        }
    }

    private fun clearUserSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun getCurrentUserId(): String? {
        return if (isLoggedIn()) {
            sharedPreferences.getString("user_id", null)
        } else null
    }

    fun getCurrentUserEmail(): String? {
        return if (isLoggedIn()) {
            sharedPreferences.getString("user_email", null)
        } else null
    }

    fun getCurrentUserName(): String? {
        return if (isLoggedIn()) {
            sharedPreferences.getString("user_name", null)
        } else null
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    // User profile methods
    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId()
        return if (userId != null) {
            userDao.getUserById(userId)
        } else null
    }

    suspend fun updateUserProfile(
        name: String,
        age: Int?,
        height: Int?,
        currentWeight: Double?,
        targetWeight: Double?,
        activityLevel: String,
        profileImageUrl: String? = null,
        phoneNumber: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        fitnessGoal: String? = null
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                userDao.updateUserProfile(
                    userId = userId,
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
                    timestamp = System.currentTimeMillis()
                )

                // Update session with new name
                sharedPreferences.edit().putString("user_name", name).apply()

                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun emailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.emailExists(email.lowercase().trim()) > 0
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = getCurrentUser()
            if (user != null && BCrypt.checkpw(currentPassword, user.passwordHash)) {
                val newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                val updatedUser = user.copy(
                    passwordHash = newPasswordHash,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.updateUser(updatedUser)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Current password is incorrect"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                userDao.softDeleteUser(userId)
                clearUserSession()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Validation methods
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
}