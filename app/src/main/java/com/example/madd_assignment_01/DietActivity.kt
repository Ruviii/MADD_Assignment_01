package com.example.madd_assignment_01

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val caloriesPerServing: Int,
    val servingSize: String,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

data class SelectedFoodItem(
    val foodItem: FoodItem,
    val quantity: Double = 1.0,
    val customPortion: String = ""
) {
    val totalCalories: Int
        get() = (foodItem.caloriesPerServing * quantity).toInt()

    val displayPortion: String
        get() = if (customPortion.isNotEmpty()) customPortion else "${quantity} ${foodItem.servingSize}"
}

data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val type: MealType,
    val time: String,
    val foodItems: MutableList<SelectedFoodItem> = mutableListOf(),
    val date: Date = Date()
) {
    val totalCalories: Int
        get() = foodItems.sumOf { it.totalCalories }

    val totalProtein: Double
        get() = foodItems.sumOf { it.foodItem.protein * it.quantity }

    val totalCarbs: Double
        get() = foodItems.sumOf { it.foodItem.carbs * it.quantity }

    val totalFat: Double
        get() = foodItems.sumOf { it.foodItem.fat * it.quantity }
}

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

data class DailyNutrition(
    val calorieGoal: Int = 2200,
    val proteinGoal: Double = 125.0,
    val carbsGoal: Double = 250.0,
    val fatGoal: Double = 73.0
) {
    fun getConsumedCalories(meals: List<Meal>): Int = meals.sumOf { it.totalCalories }
    fun getConsumedProtein(meals: List<Meal>): Double = meals.sumOf { it.totalProtein }
    fun getConsumedCarbs(meals: List<Meal>): Double = meals.sumOf { it.totalCarbs }
    fun getConsumedFat(meals: List<Meal>): Double = meals.sumOf { it.totalFat }

    fun getRemainingCalories(meals: List<Meal>): Int = calorieGoal - getConsumedCalories(meals)
    fun getCalorieProgress(meals: List<Meal>): Int = ((getConsumedCalories(meals).toDouble() / calorieGoal) * 100).toInt()
    fun getProteinProgress(meals: List<Meal>): Int = ((getConsumedProtein(meals) / proteinGoal) * 100).toInt()
    fun getCarbsProgress(meals: List<Meal>): Int = ((getConsumedCarbs(meals) / carbsGoal) * 100).toInt()
    fun getFatProgress(meals: List<Meal>): Int = ((getConsumedFat(meals) / fatGoal) * 100).toInt()
}

class DietActivity : AppCompatActivity() {

    // Views
    private lateinit var addMealFab: CardView
    private lateinit var dietSettingsButton: ImageView
    private lateinit var addMealButtonCard: CardView

    // Summary views
    private lateinit var calorieGoal: TextView
    private lateinit var calorieConsumed: TextView
    private lateinit var calorieRemaining: TextView
    private lateinit var calorieProgress: ProgressBar

    // Macronutrient views
    private lateinit var proteinValue: TextView
    private lateinit var carbsValue: TextView
    private lateinit var fatValue: TextView
    private lateinit var proteinProgress: ProgressBar
    private lateinit var carbsProgress: ProgressBar
    private lateinit var fatProgress: ProgressBar

    // Meal plans
    private lateinit var highProteinPlan: CardView
    private lateinit var weightLossPlan: CardView

    // Nutrition tips
    private lateinit var nutritionTipsCard: CardView
    private lateinit var readMoreButton: Button

    // Meals
    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var mealsAdapter: MealsAdapter

    // Bottom navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navWorkouts: LinearLayout
    private lateinit var navDiet: LinearLayout
    private lateinit var navGoals: LinearLayout
    private lateinit var navReminders: LinearLayout
    private lateinit var navAnalytics: LinearLayout

    // Data
    private val todaysMeals = mutableListOf<Meal>()
    private val dailyNutrition = DailyNutrition()
    private val foodDatabase = mutableListOf<FoodItem>()

    // Dialog
    private var addMealDialog: AlertDialog? = null

    companion object {
        private const val TAG = "DietActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_diet)
            initializeViews()
            setupClickListeners()
            setupRecyclerView()
            loadFoodDatabase()
            loadSampleMeals()
            updateNutritionSummary()

            Log.d(TAG, "DietActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading diet screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            // Header
            addMealFab = findViewById(R.id.add_meal_fab)
            dietSettingsButton = findViewById(R.id.diet_settings_button)
            addMealButtonCard = findViewById(R.id.add_meal_button_card)

            // Summary
            calorieGoal = findViewById(R.id.calorie_goal)
            calorieConsumed = findViewById(R.id.calorie_consumed)
            calorieRemaining = findViewById(R.id.calorie_remaining)
            calorieProgress = findViewById(R.id.calorie_progress)

            // Macronutrients
            proteinValue = findViewById(R.id.protein_value)
            carbsValue = findViewById(R.id.carbs_value)
            fatValue = findViewById(R.id.fat_value)
            proteinProgress = findViewById(R.id.protein_progress)
            carbsProgress = findViewById(R.id.carbs_progress)
            fatProgress = findViewById(R.id.fat_progress)

            // Meal plans
            highProteinPlan = findViewById(R.id.high_protein_plan)
            weightLossPlan = findViewById(R.id.weight_loss_plan)

            // Nutrition tips
            nutritionTipsCard = findViewById(R.id.nutrition_tips_card)
            readMoreButton = findViewById(R.id.read_more_button)

            // Meals
            mealsRecyclerView = findViewById(R.id.meals_recyclerview)

            // Bottom navigation
            navHome = findViewById(R.id.nav_home)
            navWorkouts = findViewById(R.id.nav_workouts)
            navDiet = findViewById(R.id.nav_diet)
            navGoals = findViewById(R.id.nav_goals)
            navReminders = findViewById(R.id.nav_reminders)
            navAnalytics = findViewById(R.id.nav_analytics)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Add meal buttons
        addMealFab.setOnClickListener {
            Log.d(TAG, "Add meal FAB clicked")
            showAddMealDialog()
        }

        addMealButtonCard.setOnClickListener {
            Log.d(TAG, "Add meal button card clicked")
            showAddMealDialog()
        }

        // Settings button
        dietSettingsButton.setOnClickListener {
            Log.d(TAG, "Diet settings button clicked")
            showDietSettings()
        }

        // Meal plans
        highProteinPlan.setOnClickListener {
            Log.d(TAG, "High protein plan clicked")
            showMealPlanDetails("High Protein Plan", "Build muscle with our protein-rich meal plan")
        }

        weightLossPlan.setOnClickListener {
            Log.d(TAG, "Weight loss plan clicked")
            showMealPlanDetails("Weight Loss Diet", "Healthy meals to help you reach your goals")
        }

        // Nutrition tips
        nutritionTipsCard.setOnClickListener {
            Log.d(TAG, "Nutrition tips card clicked")
            showNutritionTips()
        }

        readMoreButton.setOnClickListener {
            Log.d(TAG, "Read more button clicked")
            showNutritionTips()
        }

        // Bottom navigation
        navHome.setOnClickListener {
            Log.d(TAG, "Home navigation clicked")
            finish() // Go back to dashboard
        }

        navWorkouts.setOnClickListener {
            Log.d(TAG, "Workouts navigation clicked")
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        navDiet.setOnClickListener {
            Log.d(TAG, "Diet navigation clicked - already here")
        }

        navGoals.setOnClickListener {
            Log.d(TAG, "Goals navigation clicked")
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        navReminders.setOnClickListener {
            Log.d(TAG, "Reminders navigation clicked")
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }

        navAnalytics.setOnClickListener {
            Log.d(TAG, "Analytics navigation clicked")
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        mealsAdapter = MealsAdapter(
            meals = todaysMeals,
            onViewDetails = { meal ->
                Log.d(TAG, "View meal details: ${meal.type.displayName}")
                showMealDetails(meal)
            }
        )

        mealsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DietActivity)
            adapter = mealsAdapter
        }
    }

    private fun loadFoodDatabase() {
        foodDatabase.addAll(listOf(
            // Breakfast items
            FoodItem("oatmeal", "Oatmeal with Berries", 280, "1 bowl", 8.0, 54.0, 6.0),
            FoodItem("greek_yogurt", "Greek Yogurt", 120, "1 cup (200g)", 20.0, 9.0, 0.5),
            FoodItem("black_coffee", "Black Coffee", 20, "1 cup", 0.3, 0.0, 0.0),
            FoodItem("banana", "Banana", 105, "1 medium (118g)", 1.3, 27.0, 0.4),
            FoodItem("eggs", "Scrambled Eggs", 91, "1 large egg", 6.3, 0.6, 6.3),

            // Lunch items
            FoodItem("chicken_salad", "Grilled Chicken Salad", 450, "1 serving", 35.0, 15.0, 25.0),
            FoodItem("whole_grain_bread", "Whole Grain Bread", 120, "2 slices", 4.0, 24.0, 2.0),
            FoodItem("apple", "Apple", 80, "1 medium", 0.4, 21.0, 0.3),
            FoodItem("quinoa", "Quinoa", 222, "1 cup cooked", 8.1, 39.4, 3.6),

            // Snacks
            FoodItem("protein_bar", "Protein Bar", 200, "1 bar", 20.0, 15.0, 8.0),
            FoodItem("almonds", "Almonds", 164, "1 oz (28g)", 6.0, 6.0, 14.0),
            FoodItem("protein_shake", "Protein Shake", 150, "1 scoop", 25.0, 5.0, 2.0),

            // Common foods
            FoodItem("rice", "White Rice", 205, "1 cup cooked", 4.3, 45.0, 0.4),
            FoodItem("broccoli", "Broccoli", 25, "1 cup", 3.0, 5.0, 0.3),
            FoodItem("salmon", "Grilled Salmon", 206, "100g", 22.0, 0.0, 12.0),
            FoodItem("avocado", "Avocado", 234, "1 whole", 2.9, 12.0, 21.0),
            FoodItem("sweet_potato", "Sweet Potato", 112, "1 medium", 2.0, 26.0, 0.1)
        ))

        Log.d(TAG, "Food database loaded: ${foodDatabase.size} items")
    }

    private fun loadSampleMeals() {
        // Breakfast
        val breakfast = Meal(
            type = MealType.BREAKFAST,
            time = "08:30 AM"
        ).apply {
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "oatmeal" }!!, 1.0, "1 bowl"))
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "greek_yogurt" }!!, 1.0, "1 cup (200g)"))
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "black_coffee" }!!, 1.0, "1 cup"))
        }

        // Lunch
        val lunch = Meal(
            type = MealType.LUNCH,
            time = "12:45 PM"
        ).apply {
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "chicken_salad" }!!, 1.0, "1 serving"))
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "whole_grain_bread" }!!, 1.0, "2 slices"))
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "apple" }!!, 1.0, "1 medium"))
        }

        // Snack
        val snack = Meal(
            type = MealType.SNACK,
            time = "03:30 PM"
        ).apply {
            foodItems.add(SelectedFoodItem(foodDatabase.find { it.id == "protein_bar" }!!, 1.0, "1 bar"))
        }

        todaysMeals.addAll(listOf(breakfast, lunch, snack))
        mealsAdapter.notifyDataSetChanged()

        Log.d(TAG, "Sample meals loaded: ${todaysMeals.size} meals")
    }

    private fun updateNutritionSummary() {
        val consumed = dailyNutrition.getConsumedCalories(todaysMeals)
        val remaining = dailyNutrition.getRemainingCalories(todaysMeals)
        val consumedProtein = dailyNutrition.getConsumedProtein(todaysMeals)
        val consumedCarbs = dailyNutrition.getConsumedCarbs(todaysMeals)
        val consumedFat = dailyNutrition.getConsumedFat(todaysMeals)

        // Update calorie summary
        calorieGoal.text = "${dailyNutrition.calorieGoal} kcal"
        calorieConsumed.text = "$consumed kcal"
        calorieRemaining.text = "$remaining kcal"
        calorieProgress.progress = dailyNutrition.getCalorieProgress(todaysMeals)

        // Update macronutrients
        proteinValue.text = "${consumedProtein.toInt()}g"
        carbsValue.text = "${consumedCarbs.toInt()}g"
        fatValue.text = "${consumedFat.toInt()}g"

        proteinProgress.progress = dailyNutrition.getProteinProgress(todaysMeals)
        carbsProgress.progress = dailyNutrition.getCarbsProgress(todaysMeals)
        fatProgress.progress = dailyNutrition.getFatProgress(todaysMeals)

        Log.d(TAG, "Nutrition summary updated - Calories: $consumed/$remaining, Protein: ${consumedProtein.toInt()}g")
    }

    private fun showAddMealDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null)

            val mealTypeSpinner = dialogView.findViewById<Spinner>(R.id.meal_type_spinner)
            val mealTypeDisplay = dialogView.findViewById<TextView>(R.id.meal_type_display)
            val foodSearchInput = dialogView.findViewById<EditText>(R.id.food_search_input)
            val foodSuggestionsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.food_suggestions_recyclerview)
            val selectedItemsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.selected_items_recyclerview)
            val totalCaloriesText = dialogView.findViewById<TextView>(R.id.total_calories_text)
            val closeMealDialog = dialogView.findViewById<ImageView>(R.id.close_meal_dialog)
            val cancelMealButton = dialogView.findViewById<Button>(R.id.cancel_meal_button)
            val addMealButton = dialogView.findViewById<Button>(R.id.add_meal_button)

            // Setup meal type spinner (hidden) and display
            val mealTypes = MealType.values()
            val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_meal_type, mealTypes.map { it.displayName })
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_workout_type)
            mealTypeSpinner.adapter = spinnerAdapter

            // Setup meal type display (clickable to show spinner)
            mealTypeDisplay.setOnClickListener {
                mealTypeSpinner.performClick()
            }

            mealTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    mealTypeDisplay.text = mealTypes[position].displayName
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Setup selected items
            val selectedItems = mutableListOf<SelectedFoodItem>()
            lateinit var selectedItemsAdapter: SelectedFoodItemsAdapter
            selectedItemsAdapter = SelectedFoodItemsAdapter(selectedItems) { item ->
                selectedItems.remove(item)
                selectedItemsAdapter.notifyDataSetChanged()
                updateTotalCalories(selectedItems, totalCaloriesText)
            }

            selectedItemsRecyclerView.layoutManager = LinearLayoutManager(this)
            selectedItemsRecyclerView.adapter = selectedItemsAdapter

            // Setup food suggestions
            val foodSuggestionsAdapter = FoodSuggestionsAdapter(emptyList()) { foodItem ->
                val selectedFood = SelectedFoodItem(foodItem)
                selectedItems.add(selectedFood)
                selectedItemsAdapter.notifyDataSetChanged()
                updateTotalCalories(selectedItems, totalCaloriesText)
                foodSuggestionsRecyclerView.visibility = View.GONE
                foodSearchInput.text.clear()
            }

            foodSuggestionsRecyclerView.layoutManager = LinearLayoutManager(this)
            foodSuggestionsRecyclerView.adapter = foodSuggestionsAdapter

            // Setup search functionality
            foodSearchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString().trim()
                    if (query.length >= 2) {
                        val suggestions = searchFood(query)
                        foodSuggestionsAdapter.updateSuggestions(suggestions)
                        foodSuggestionsRecyclerView.visibility = if (suggestions.isNotEmpty()) View.VISIBLE else View.GONE
                    } else {
                        foodSuggestionsRecyclerView.visibility = View.GONE
                    }
                }
            })

            addMealDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            addMealDialog?.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
                setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
            }

            // Close dialog listeners
            closeMealDialog.setOnClickListener {
                addMealDialog?.dismiss()
            }

            cancelMealButton.setOnClickListener {
                addMealDialog?.dismiss()
            }

            // Add meal listener
            addMealButton.setOnClickListener {
                if (selectedItems.isEmpty()) {
                    Toast.makeText(this, "Please select at least one food item", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedMealType = mealTypes[mealTypeSpinner.selectedItemPosition]
                val currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

                val newMeal = Meal(
                    type = selectedMealType,
                    time = currentTime,
                    foodItems = selectedItems.toMutableList()
                )

                addMeal(newMeal)
                addMealDialog?.dismiss()

                Toast.makeText(this, "Meal added successfully!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "New meal added: ${selectedMealType.displayName} with ${selectedItems.size} items")
            }

            updateTotalCalories(selectedItems, totalCaloriesText)
            addMealDialog?.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing add meal dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening add meal dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchFood(query: String): List<FoodItem> {
        return foodDatabase.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(5)
    }

    private fun updateTotalCalories(selectedItems: List<SelectedFoodItem>, totalCaloriesText: TextView) {
        val totalCalories = selectedItems.sumOf { it.totalCalories }
        totalCaloriesText.text = "$totalCalories kcal"
    }

    private fun addMeal(meal: Meal) {
        // Find existing meal of same type and merge, or add new meal
        val existingMealIndex = todaysMeals.indexOfFirst { it.type == meal.type }

        if (existingMealIndex >= 0) {
            todaysMeals[existingMealIndex].foodItems.addAll(meal.foodItems)
            mealsAdapter.notifyItemChanged(existingMealIndex)
        } else {
            todaysMeals.add(meal)
            todaysMeals.sortBy { it.type.ordinal }
            mealsAdapter.notifyDataSetChanged()
        }

        updateNutritionSummary()
    }

    private fun showMealDetails(meal: Meal) {
        val message = StringBuilder()
        message.append("Time: ${meal.time}\n")
        message.append("Total Calories: ${meal.totalCalories} kcal\n\n")
        message.append("Food Items:\n")

        meal.foodItems.forEach { item ->
            message.append("• ${item.foodItem.name} (${item.displayPortion}) - ${item.totalCalories} kcal\n")
        }

        message.append("\nNutrition:\n")
        message.append("Protein: ${meal.totalProtein.toInt()}g\n")
        message.append("Carbs: ${meal.totalCarbs.toInt()}g\n")
        message.append("Fat: ${meal.totalFat.toInt()}g")

        AlertDialog.Builder(this)
            .setTitle(meal.type.displayName)
            .setMessage(message.toString())
            .setPositiveButton("Edit") { _, _ ->
                editMeal(meal)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteMeal(meal)
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun editMeal(meal: Meal) {
        Toast.makeText(this, "Edit meal feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMeal(meal: Meal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete this ${meal.type.displayName}?")
            .setPositiveButton("Delete") { _, _ ->
                todaysMeals.remove(meal)
                mealsAdapter.notifyDataSetChanged()
                updateNutritionSummary()
                Toast.makeText(this, "Meal deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMealPlanDetails(title: String, description: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description + "\n\nThis feature will provide personalized meal plans based on your goals and preferences.")
            .setPositiveButton("Learn More") { _, _ ->
                Toast.makeText(this, "Meal plan details coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showNutritionTips() {
        val tips = """
            💡 Nutrition Tips for Optimal Results:

            • Stay hydrated - aim for 8 glasses of water daily
            • Include protein in every meal to maintain muscle mass
            • Choose complex carbs over simple sugars
            • Don't skip meals - eat every 3-4 hours
            • Include healthy fats like avocado, nuts, and olive oil
            • Fill half your plate with vegetables
            • Practice portion control
            • Plan your meals in advance
            • Listen to your body's hunger cues
            • Allow yourself occasional treats in moderation
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Nutrition Tips")
            .setMessage(tips)
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun showDietSettings() {
        val options = arrayOf(
            "Set Calorie Goal",
            "Set Macronutrient Goals",
            "Food Preferences",
            "Dietary Restrictions",
            "Export Data"
        )

        AlertDialog.Builder(this)
            .setTitle("Diet Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCalorieGoalSetting()
                    1 -> showMacronutrientGoals()
                    2 -> showFoodPreferences()
                    3 -> showDietaryRestrictions()
                    4 -> exportDietData()
                }
            }
            .show()
    }

    private fun showCalorieGoalSetting() {
        Toast.makeText(this, "Calorie goal setting coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showMacronutrientGoals() {
        Toast.makeText(this, "Macronutrient goals setting coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showFoodPreferences() {
        Toast.makeText(this, "Food preferences setting coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showDietaryRestrictions() {
        Toast.makeText(this, "Dietary restrictions setting coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun exportDietData() {
        Toast.makeText(this, "Export data feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        addMealDialog?.dismiss()
    }
}

// Adapter Classes
class MealsAdapter(
    private var meals: List<Meal>,
    private val onViewDetails: (Meal) -> Unit
) : RecyclerView.Adapter<MealsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealTypeName: TextView = view.findViewById(R.id.meal_type_name)
        val mealTime: TextView = view.findViewById(R.id.meal_time)
        val mealTotalCalories: TextView = view.findViewById(R.id.meal_total_calories)
        val foodItemsRecyclerView: RecyclerView = view.findViewById(R.id.food_items_recyclerview)
        val viewDetailsButton: LinearLayout = view.findViewById(R.id.view_details_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = meals[position]

        holder.mealTypeName.text = meal.type.displayName
        holder.mealTime.text = meal.time
        holder.mealTotalCalories.text = "${meal.totalCalories} kcal"

        // Setup food items recycler view
        val foodItemsAdapter = FoodItemsAdapter(meal.foodItems.take(3)) // Show max 3 items
        holder.foodItemsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.foodItemsRecyclerView.adapter = foodItemsAdapter

        holder.viewDetailsButton.setOnClickListener {
            onViewDetails(meal)
        }
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}

class FoodItemsAdapter(
    private val foodItems: List<SelectedFoodItem>
) : RecyclerView.Adapter<FoodItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.food_name)
        val foodCalories: TextView = view.findViewById(R.id.food_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = foodItems[position]
        holder.foodName.text = item.foodItem.name
        holder.foodCalories.text = "${item.totalCalories} kcal"
    }

    override fun getItemCount() = foodItems.size
}

class FoodSuggestionsAdapter(
    private var suggestions: List<FoodItem>,
    private val onFoodSelected: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodSuggestionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionFoodName: TextView = view.findViewById(R.id.suggestion_food_name)
        val suggestionCalories: TextView = view.findViewById(R.id.suggestion_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = suggestions[position]

        holder.suggestionFoodName.text = food.name
        holder.suggestionCalories.text = "${food.caloriesPerServing} kcal"

        holder.itemView.setOnClickListener {
            onFoodSelected(food)
        }
    }

    override fun getItemCount() = suggestions.size

    fun updateSuggestions(newSuggestions: List<FoodItem>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }
}

class SelectedFoodItemsAdapter(
    private var selectedItems: MutableList<SelectedFoodItem>,
    private val onRemoveItem: (SelectedFoodItem) -> Unit
) : RecyclerView.Adapter<SelectedFoodItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val selectedFoodName: TextView = view.findViewById(R.id.selected_food_name)
        val selectedFoodPortion: TextView = view.findViewById(R.id.selected_food_portion)
        val selectedFoodCalories: TextView = view.findViewById(R.id.selected_food_calories)
        val removeFoodButton: ImageView = view.findViewById(R.id.remove_food_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = selectedItems[position]

        holder.selectedFoodName.text = item.foodItem.name
        holder.selectedFoodPortion.text = item.displayPortion
        holder.selectedFoodCalories.text = "${item.totalCalories} kcal"

        holder.removeFoodButton.setOnClickListener {
            onRemoveItem(item)
        }
    }

    override fun getItemCount() = selectedItems.size

    fun updateItems(newItems: MutableList<SelectedFoodItem>) {
        selectedItems = newItems
        notifyDataSetChanged()
    }
}