package com.example.madd_assignment_01.repository

import com.example.madd_assignment_01.database.entities.User
import com.example.madd_assignment_01.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class AnalyticsRepository(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val nutritionRepository: NutritionRepository,
    private val goalRepository: GoalRepository
) {

    // Comprehensive analytics dashboard
    suspend fun getDashboardAnalytics(): DashboardAnalytics = withContext(Dispatchers.IO) {
        val userId = userRepository.getCurrentUserId() ?: return@withContext DashboardAnalytics()

        try {
            val calendar = Calendar.getInstance()
            val today = calendar.time

            // Weekly stats
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.time

            // Monthly stats
            calendar.time = today
            calendar.add(Calendar.MONTH, -1)
            val monthAgo = calendar.time

            val weeklyWorkoutMinutes = workoutRepository.getWeeklyWorkoutMinutes()
            val weeklyCaloriesBurned = workoutRepository.getWeeklyCaloriesBurned()
            val weeklyCaloriesConsumed = nutritionRepository.getWeeklyCalories()
            val monthlyStats = workoutRepository.getMonthlyWorkoutStats()

            val activeGoalsCount = goalRepository.getActiveGoals().let { flow ->
                var count = 0
                flow.collect { count = it.size }
                count
            }

            val completionRate = goalRepository.getGoalCompletionRate()

            DashboardAnalytics(
                weeklyWorkoutMinutes = weeklyWorkoutMinutes,
                weeklyCaloriesBurned = weeklyCaloriesBurned,
                weeklyCaloriesConsumed = weeklyCaloriesConsumed,
                monthlyWorkoutStats = monthlyStats,
                activeGoalsCount = activeGoalsCount,
                goalCompletionRate = completionRate,
                lastUpdated = today
            )
        } catch (e: Exception) {
            DashboardAnalytics()
        }
    }

    // Workout analytics
    suspend fun getWorkoutAnalytics(period: AnalyticsPeriod): WorkoutAnalytics = withContext(Dispatchers.IO) {
        try {
            val (startDate, endDate) = getPeriodDates(period)
            val totalMinutes = workoutRepository.getTotalWorkoutMinutes(startDate, endDate)
            val totalCalories = workoutRepository.getTotalCaloriesBurned(startDate, endDate)
            val workoutCount = workoutRepository.getWorkoutCount(startDate, endDate)
            val frequencyByType = workoutRepository.getWorkoutFrequencyByType()

            val averageWorkoutLength = if (workoutCount > 0) totalMinutes / workoutCount else 0
            val averageCaloriesPerWorkout = if (workoutCount > 0) totalCalories / workoutCount else 0

            // Get workout trends (daily breakdown)
            val trends = getWorkoutTrends(startDate, endDate)

            WorkoutAnalytics(
                period = period,
                totalWorkouts = workoutCount,
                totalMinutes = totalMinutes,
                totalCaloriesBurned = totalCalories,
                averageWorkoutLength = averageWorkoutLength,
                averageCaloriesPerWorkout = averageCaloriesPerWorkout,
                workoutFrequencyByType = frequencyByType,
                dailyTrends = trends,
                mostActiveDay = getMostActiveDay(startDate, endDate),
                improvementRate = calculateWorkoutImprovementRate(startDate, endDate)
            )
        } catch (e: Exception) {
            WorkoutAnalytics(period = period)
        }
    }

    // Nutrition analytics
    suspend fun getNutritionAnalytics(period: AnalyticsPeriod): NutritionAnalytics = withContext(Dispatchers.IO) {
        try {
            val (startDate, endDate) = getPeriodDates(period)
            var totalCalories = 0
            var totalProtein = 0.0
            var totalCarbs = 0.0
            var totalFat = 0.0
            var dayCount = 0

            // Calculate daily averages
            val calendar = Calendar.getInstance()
            calendar.time = startDate

            while (calendar.time.before(endDate) || calendar.time == endDate) {
                val dayCalories = nutritionRepository.getDailyCalories(calendar.time)
                val dayMacros = nutritionRepository.getDailyMacros(calendar.time)

                totalCalories += dayCalories
                totalProtein += dayMacros.getConsumedProtein(emptyList())
                totalCarbs += dayMacros.getConsumedCarbs(emptyList())
                totalFat += dayMacros.getConsumedFat(emptyList())
                dayCount++

                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val avgCalories = if (dayCount > 0) totalCalories / dayCount else 0
            val avgProtein = if (dayCount > 0) totalProtein / dayCount else 0.0
            val avgCarbs = if (dayCount > 0) totalCarbs / dayCount else 0.0
            val avgFat = if (dayCount > 0) totalFat / dayCount else 0.0

            val trends = getNutritionTrends(startDate, endDate)

            NutritionAnalytics(
                period = period,
                averageDailyCalories = avgCalories,
                averageDailyProtein = avgProtein,
                averageDailyCarbs = avgCarbs,
                averageDailyFat = avgFat,
                totalCaloriesConsumed = totalCalories,
                dailyTrends = trends,
                macroDistribution = MacroDistribution(
                    proteinPercentage = calculateMacroPercentage(avgProtein * 4, avgCalories),
                    carbsPercentage = calculateMacroPercentage(avgCarbs * 4, avgCalories),
                    fatPercentage = calculateMacroPercentage(avgFat * 9, avgCalories)
                ),
                calorieBalance = calculateCalorieBalance(period)
            )
        } catch (e: Exception) {
            NutritionAnalytics(period = period)
        }
    }

    // Goal progress analytics
    suspend fun getGoalAnalytics(): GoalAnalytics = withContext(Dispatchers.IO) {
        try {
            var totalGoals = 0
            var completedGoals = 0
            var overdueGoals = 0
            val goalsByCategory = mutableMapOf<GoalCategory, Int>()
            val goalsByPriority = mutableMapOf<Priority, Int>()

            goalRepository.getGoals().collect { goals ->
                totalGoals = goals.size
                completedGoals = goals.count { it.isCompleted }
                overdueGoals = goals.count { it.isOverdue }

                // Group by category
                goals.groupBy { it.category }.forEach { (category, goalList) ->
                    goalsByCategory[category] = goalList.size
                }

                // Group by priority
                goals.groupBy { it.priority }.forEach { (priority, goalList) ->
                    goalsByPriority[priority] = goalList.size
                }
            }

            val completionRate = if (totalGoals > 0) (completedGoals.toDouble() / totalGoals) * 100 else 0.0
            val onTrackGoals = totalGoals - completedGoals - overdueGoals

            GoalAnalytics(
                totalGoals = totalGoals,
                completedGoals = completedGoals,
                onTrackGoals = onTrackGoals,
                overdueGoals = overdueGoals,
                completionRate = completionRate,
                goalsByCategory = goalsByCategory,
                goalsByPriority = goalsByPriority,
                averageCompletionTime = calculateAverageCompletionTime(),
                goalMomentum = calculateGoalMomentum()
            )
        } catch (e: Exception) {
            GoalAnalytics()
        }
    }

    // Progress reports
    suspend fun generateProgressReport(period: AnalyticsPeriod): ProgressReport = withContext(Dispatchers.IO) {
        val workoutAnalytics = getWorkoutAnalytics(period)
        val nutritionAnalytics = getNutritionAnalytics(period)
        val goalAnalytics = getGoalAnalytics()
        val insights = generateInsights(workoutAnalytics, nutritionAnalytics, goalAnalytics)

        ProgressReport(
            period = period,
            workoutAnalytics = workoutAnalytics,
            nutritionAnalytics = nutritionAnalytics,
            goalAnalytics = goalAnalytics,
            insights = insights,
            recommendations = generateRecommendations(workoutAnalytics, nutritionAnalytics, goalAnalytics),
            generatedDate = Date()
        )
    }

    // Helper methods
    private fun getPeriodDates(period: AnalyticsPeriod): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        when (period) {
            AnalyticsPeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            AnalyticsPeriod.MONTH -> calendar.add(Calendar.MONTH, -1)
            AnalyticsPeriod.QUARTER -> calendar.add(Calendar.MONTH, -3)
            AnalyticsPeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
        }

        return Pair(calendar.time, endDate)
    }

    private suspend fun getWorkoutTrends(startDate: Date, endDate: Date): List<DailyTrend> {
        val trends = mutableListOf<DailyTrend>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val dayWorkouts = workoutRepository.getWorkoutsByDateRange(calendar.time, calendar.time)
            var totalMinutes = 0
            var totalCalories = 0

            dayWorkouts.collect { workouts ->
                totalMinutes = workouts.sumOf { it.duration }
                totalCalories = workouts.sumOf { it.calories }
            }

            trends.add(
                DailyTrend(
                    date = dateFormat.format(calendar.time),
                    value = totalMinutes.toDouble(),
                    secondaryValue = totalCalories.toDouble()
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return trends
    }

    private suspend fun getNutritionTrends(startDate: Date, endDate: Date): List<DailyTrend> {
        val trends = mutableListOf<DailyTrend>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val dayCalories = nutritionRepository.getDailyCalories(calendar.time)

            trends.add(
                DailyTrend(
                    date = dateFormat.format(calendar.time),
                    value = dayCalories.toDouble()
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return trends
    }

    private suspend fun getMostActiveDay(startDate: Date, endDate: Date): String {
        val dayWorkouts = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            val workoutCount = workoutRepository.getWorkoutCount(calendar.time, calendar.time)
            dayWorkouts[dayName] = dayWorkouts.getOrDefault(dayName, 0) + workoutCount
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dayWorkouts.maxByOrNull { it.value }?.key ?: "Monday"
    }

    private suspend fun calculateWorkoutImprovementRate(startDate: Date, endDate: Date): Double {
        // Calculate improvement rate based on workout duration increase
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_YEAR, 7) // First week
        val firstWeekEnd = calendar.time

        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Last week
        val lastWeekStart = calendar.time

        val firstWeekMinutes = workoutRepository.getTotalWorkoutMinutes(startDate, firstWeekEnd)
        val lastWeekMinutes = workoutRepository.getTotalWorkoutMinutes(lastWeekStart, endDate)

        return if (firstWeekMinutes > 0) {
            ((lastWeekMinutes - firstWeekMinutes).toDouble() / firstWeekMinutes) * 100
        } else 0.0
    }

    private fun calculateMacroPercentage(macroCalories: Double, totalCalories: Int): Double {
        return if (totalCalories > 0) (macroCalories / totalCalories) * 100 else 0.0
    }

    private suspend fun calculateCalorieBalance(period: AnalyticsPeriod): CalorieBalance {
        val (startDate, endDate) = getPeriodDates(period)
        val totalConsumed = nutritionRepository.getCaloriesForPeriod(startDate, endDate)
        val totalBurned = workoutRepository.getTotalCaloriesBurned(startDate, endDate)

        // Estimate BMR (this is simplified - real calculation would use user's age, weight, height)
        val user = userRepository.getCurrentUser()
        val estimatedBMR = estimateBasalMetabolicRate(user)
        val days = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
        val totalBMRCalories = estimatedBMR * days

        val netBalance = totalConsumed - (totalBurned + totalBMRCalories)

        return CalorieBalance(
            consumed = totalConsumed,
            burned = totalBurned + totalBMRCalories,
            net = netBalance
        )
    }

    private fun estimateBasalMetabolicRate(user: User?): Int {
        // Simplified BMR calculation using Harris-Benedict equation
        // This would typically use user's actual age, weight, height, and gender
        return 1800 // Default estimate
    }

    private suspend fun calculateAverageCompletionTime(): Double {
        // Calculate average time to complete goals
        return 30.0 // Placeholder - would need goal completion date tracking
    }

    private suspend fun calculateGoalMomentum(): Double {
        // Calculate momentum based on recent goal progress
        return 75.0 // Placeholder - would analyze recent goal updates
    }

    private fun generateInsights(
        workoutAnalytics: WorkoutAnalytics,
        nutritionAnalytics: NutritionAnalytics,
        goalAnalytics: GoalAnalytics
    ): List<String> {
        val insights = mutableListOf<String>()

        // Workout insights
        if (workoutAnalytics.totalWorkouts > 0) {
            insights.add("You completed ${workoutAnalytics.totalWorkouts} workouts this ${workoutAnalytics.period.name.lowercase()}")

            if (workoutAnalytics.improvementRate > 0) {
                insights.add("Your workout duration improved by ${workoutAnalytics.improvementRate.roundToInt()}%")
            }

            insights.add("Your most active day is ${workoutAnalytics.mostActiveDay}")
        }

        // Nutrition insights
        if (nutritionAnalytics.averageDailyCalories > 0) {
            insights.add("Average daily intake: ${nutritionAnalytics.averageDailyCalories} calories")

            val proteinPercent = nutritionAnalytics.macroDistribution.proteinPercentage.roundToInt()
            val carbPercent = nutritionAnalytics.macroDistribution.carbsPercentage.roundToInt()
            val fatPercent = nutritionAnalytics.macroDistribution.fatPercentage.roundToInt()

            insights.add("Macro distribution: ${proteinPercent}% protein, ${carbPercent}% carbs, ${fatPercent}% fat")
        }

        // Goal insights
        if (goalAnalytics.totalGoals > 0) {
            insights.add("Goal completion rate: ${goalAnalytics.completionRate.roundToInt()}%")

            if (goalAnalytics.overdueGoals > 0) {
                insights.add("${goalAnalytics.overdueGoals} goals are overdue and need attention")
            }
        }

        return insights
    }

    private fun generateRecommendations(
        workoutAnalytics: WorkoutAnalytics,
        nutritionAnalytics: NutritionAnalytics,
        goalAnalytics: GoalAnalytics
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Workout recommendations
        if (workoutAnalytics.totalWorkouts < 3) {
            recommendations.add("Try to increase workout frequency to at least 3 times per week")
        }

        if (workoutAnalytics.averageWorkoutLength < 30) {
            recommendations.add("Consider extending workout duration to 30+ minutes for better results")
        }

        // Nutrition recommendations
        val proteinPercent = nutritionAnalytics.macroDistribution.proteinPercentage
        if (proteinPercent < 20) {
            recommendations.add("Increase protein intake to support muscle recovery and growth")
        }

        if (nutritionAnalytics.calorieBalance.net > 500) {
            recommendations.add("Consider reducing calorie intake or increasing physical activity")
        }

        // Goal recommendations
        if (goalAnalytics.completionRate < 50) {
            recommendations.add("Break down large goals into smaller, more achievable milestones")
        }

        if (goalAnalytics.overdueGoals > 0) {
            recommendations.add("Review overdue goals and adjust deadlines or targets if needed")
        }

        return recommendations
    }

    // Data classes for analytics
    data class DashboardAnalytics(
        val weeklyWorkoutMinutes: Int = 0,
        val weeklyCaloriesBurned: Int = 0,
        val weeklyCaloriesConsumed: Int = 0,
        val monthlyWorkoutStats: WorkoutRepository.WorkoutStats = WorkoutRepository.WorkoutStats(0, 0, 0, 0, ""),
        val activeGoalsCount: Int = 0,
        val goalCompletionRate: Double = 0.0,
        val lastUpdated: Date = Date()
    )

    data class WorkoutAnalytics(
        val period: AnalyticsPeriod,
        val totalWorkouts: Int = 0,
        val totalMinutes: Int = 0,
        val totalCaloriesBurned: Int = 0,
        val averageWorkoutLength: Int = 0,
        val averageCaloriesPerWorkout: Int = 0,
        val workoutFrequencyByType: Map<WorkoutType, Int> = emptyMap(),
        val dailyTrends: List<DailyTrend> = emptyList(),
        val mostActiveDay: String = "",
        val improvementRate: Double = 0.0
    )

    data class NutritionAnalytics(
        val period: AnalyticsPeriod,
        val averageDailyCalories: Int = 0,
        val averageDailyProtein: Double = 0.0,
        val averageDailyCarbs: Double = 0.0,
        val averageDailyFat: Double = 0.0,
        val totalCaloriesConsumed: Int = 0,
        val dailyTrends: List<DailyTrend> = emptyList(),
        val macroDistribution: MacroDistribution = MacroDistribution(),
        val calorieBalance: CalorieBalance = CalorieBalance()
    )

    data class GoalAnalytics(
        val totalGoals: Int = 0,
        val completedGoals: Int = 0,
        val onTrackGoals: Int = 0,
        val overdueGoals: Int = 0,
        val completionRate: Double = 0.0,
        val goalsByCategory: Map<GoalCategory, Int> = emptyMap(),
        val goalsByPriority: Map<Priority, Int> = emptyMap(),
        val averageCompletionTime: Double = 0.0,
        val goalMomentum: Double = 0.0
    )

    data class DailyTrend(
        val date: String,
        val value: Double,
        val secondaryValue: Double = 0.0
    )

    data class MacroDistribution(
        val proteinPercentage: Double = 0.0,
        val carbsPercentage: Double = 0.0,
        val fatPercentage: Double = 0.0
    )

    data class CalorieBalance(
        val consumed: Int = 0,
        val burned: Int = 0,
        val net: Int = 0
    )

    data class ProgressReport(
        val period: AnalyticsPeriod,
        val workoutAnalytics: WorkoutAnalytics,
        val nutritionAnalytics: NutritionAnalytics,
        val goalAnalytics: GoalAnalytics,
        val insights: List<String>,
        val recommendations: List<String>,
        val generatedDate: Date
    )

    enum class AnalyticsPeriod {
        WEEK, MONTH, QUARTER, YEAR
    }
}