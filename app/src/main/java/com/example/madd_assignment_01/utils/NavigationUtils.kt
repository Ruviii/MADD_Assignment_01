package com.example.madd_assignment_01.utils

import android.app.Activity
import android.content.Intent
import com.example.madd_assignment_01.*

object NavigationUtils {

    fun navigateToHome(activity: Activity) {
        val intent = Intent(activity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun navigateToWorkouts(activity: Activity) {
        if (activity !is WorkoutActivity) {
            val intent = Intent(activity, WorkoutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun navigateToDiet(activity: Activity) {
        if (activity !is DietActivity) {
            val intent = Intent(activity, DietActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun navigateToGoals(activity: Activity) {
        if (activity !is GoalsActivity) {
            val intent = Intent(activity, GoalsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun navigateToReminders(activity: Activity) {
        if (activity !is ReminderActivity) {
            val intent = Intent(activity, ReminderActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun navigateToAnalytics(activity: Activity) {
        if (activity !is AnalyticsActivity) {
            val intent = Intent(activity, AnalyticsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }
}