package com.example.madd_assignment_01.components

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.madd_assignment_01.*
import com.example.madd_assignment_01.utils.NavigationUtils

class BottomNavigationManager(
    private val activity: Activity,
    private val bottomNavigationView: View
) {

    private val navHome: LinearLayout = bottomNavigationView.findViewById(R.id.nav_home)
    private val navWorkouts: LinearLayout = bottomNavigationView.findViewById(R.id.nav_workouts)
    private val navDiet: LinearLayout = bottomNavigationView.findViewById(R.id.nav_diet)
    private val navGoals: LinearLayout = bottomNavigationView.findViewById(R.id.nav_goals)
    private val navReminders: LinearLayout = bottomNavigationView.findViewById(R.id.nav_reminders)
    private val navAnalytics: LinearLayout = bottomNavigationView.findViewById(R.id.nav_analytics)

    // Icons
    private val navHomeIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_home_icon)
    private val navWorkoutsIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_workouts_icon)
    private val navDietIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_diet_icon)
    private val navGoalsIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_goals_icon)
    private val navRemindersIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_reminders_icon)
    private val navAnalyticsIcon: ImageView = bottomNavigationView.findViewById(R.id.nav_analytics_icon)

    // Text labels
    private val navHomeText: TextView = bottomNavigationView.findViewById(R.id.nav_home_text)
    private val navWorkoutsText: TextView = bottomNavigationView.findViewById(R.id.nav_workouts_text)
    private val navDietText: TextView = bottomNavigationView.findViewById(R.id.nav_diet_text)
    private val navGoalsText: TextView = bottomNavigationView.findViewById(R.id.nav_goals_text)
    private val navRemindersText: TextView = bottomNavigationView.findViewById(R.id.nav_reminders_text)
    private val navAnalyticsText: TextView = bottomNavigationView.findViewById(R.id.nav_analytics_text)

    init {
        setupClickListeners()
        highlightCurrentPage()
    }

    private fun setupClickListeners() {
        navHome.setOnClickListener {
            NavigationUtils.navigateToHome(activity)
        }

        navWorkouts.setOnClickListener {
            NavigationUtils.navigateToWorkouts(activity)
        }

        navDiet.setOnClickListener {
            NavigationUtils.navigateToDiet(activity)
        }

        navGoals.setOnClickListener {
            NavigationUtils.navigateToGoals(activity)
        }

        navReminders.setOnClickListener {
            NavigationUtils.navigateToReminders(activity)
        }

        navAnalytics.setOnClickListener {
            NavigationUtils.navigateToAnalytics(activity)
        }
    }

    private fun highlightCurrentPage() {
        // Reset all to inactive state
        resetAllToInactive()

        // Highlight current page
        when (activity::class.java) {
            MainActivity::class.java -> setActiveState(navHomeIcon, navHomeText)
            WorkoutActivity::class.java -> setActiveState(navWorkoutsIcon, navWorkoutsText)
            DietActivity::class.java -> setActiveState(navDietIcon, navDietText)
            GoalsActivity::class.java -> setActiveState(navGoalsIcon, navGoalsText)
            ReminderActivity::class.java -> setActiveState(navRemindersIcon, navRemindersText)
            AnalyticsActivity::class.java -> setActiveState(navAnalyticsIcon, navAnalyticsText)
        }
    }

    private fun resetAllToInactive() {
        val inactiveColor = ContextCompat.getColor(activity, R.color.onboarding_text_secondary)

        // Reset all icons and text to inactive color
        listOf(navHomeIcon, navWorkoutsIcon, navDietIcon, navGoalsIcon, navRemindersIcon, navAnalyticsIcon)
            .forEach { it.setColorFilter(inactiveColor) }

        listOf(navHomeText, navWorkoutsText, navDietText, navGoalsText, navRemindersText, navAnalyticsText)
            .forEach { it.setTextColor(inactiveColor) }
    }

    private fun setActiveState(icon: ImageView, text: TextView) {
        val activeColor = ContextCompat.getColor(activity, R.color.primary_blue)
        icon.setColorFilter(activeColor)
        text.setTextColor(activeColor)
    }
}