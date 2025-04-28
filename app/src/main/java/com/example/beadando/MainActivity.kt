package com.example.beadando

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beadando.databinding.ActivityMainBinding
import com.example.beadando.model.MealItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gombkezelők
        binding.countCaloriesButton.setOnClickListener {
            val intent = Intent(this, calorie_count::class.java)
            startActivity(intent)
        }
        binding.addFoodButton.setOnClickListener {
            val intent = Intent(this, add_food::class.java)
            startActivity(intent)
        }
        binding.addWaterButton.setOnClickListener {
            val intent = Intent(this, add_water::class.java)
            startActivity(intent)
        }
        binding.addBurntCalButton.setOnClickListener {
            val intent = Intent(this, add_excersise::class.java)
            startActivity(intent)
        }

        checkAndResetData()
        loadExerciseDataFromPrefs()
        updateProgressBars()
    }

    override fun onResume() {
        super.onResume()
        updateProgressBars()
    }
//Adatok betöltése(Shared Preferences)--------------------------------------------------------------
    private fun loadMealItemsFromPref(prefName: String): ArrayList<MealItem> {
        val prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("mealItems", null)
        val type = object : TypeToken<ArrayList<MealItem>>() {}.type
        return if (json != null) gson.fromJson(json, type) else ArrayList()
    }

    private fun loadUserDataFromPrefs() {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        // Betöltjük a UserData-ból a tápanyagcélokat
        calorie_count.requiredProteinGrams = prefs.getFloat("requiredProteinGrams", 0f).toDouble()
        calorie_count.requiredCarbGrams = prefs.getFloat("requiredCarbGrams", 0f).toDouble()
        calorie_count.requiredFatGrams = prefs.getFloat("requiredFatGrams", 0f).toDouble()
        calorie_count.dailyCalorieIntake = prefs.getInt("dailyCalorieIntake", 0).toDouble()
        calorie_count.calorieGoal = prefs.getInt("userCalorieGoal",0)
    }

    private fun loadExerciseDataFromPrefs() {
        val prefs = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)

        // Betöltjük az elégetett kalóriákat
        val burntCalories = prefs.getInt("total_calories", 0)

        // Beállítjuk a MainActivity-ben lévő változót
        add_excersise.result_cal_burnt = burntCalories

        // Frissítjük a UI-t, hogy az új értéket megjelenítsük
        updateProgressBars()
    }
//Adatok megjelenítése--------------------------------------------------------------
    private fun updateProgressBars() {

        loadUserDataFromPrefs()

        val breakfastItems = loadMealItemsFromPref("BreakfastData")
        val lunchItems = loadMealItemsFromPref("LunchData")
        val dinnerItems = loadMealItemsFromPref("DinnerData")
        val snackItems = loadMealItemsFromPref("SnackData")

        val allMealItems = ArrayList<MealItem>()
        allMealItems.addAll(breakfastItems)
        allMealItems.addAll(lunchItems)
        allMealItems.addAll(dinnerItems)
        allMealItems.addAll(snackItems)

        var totalProteins = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0
        var totalCal = 0.0

        for (item in allMealItems) {
            totalProteins += item.proteins
            totalCarbs += item.carbs
            totalFats += item.fats
            totalCal += item.calories
        }

        val proteinGoal = calorie_count.requiredProteinGrams.roundToInt()
        val carbGoal = calorie_count.requiredCarbGrams.roundToInt()
        val fatGoal = calorie_count.requiredFatGrams.roundToInt()
        val calGoal = if (calorie_count.calorieGoal > 0) {
            calorie_count.calorieGoal
        } else {
            calorie_count.dailyCalorieIntake.roundToInt()
        }

        binding.macroProgressProtein.max = proteinGoal
        binding.macroProgressCarbs.max = carbGoal
        binding.macroProgressFat.max = fatGoal
        binding.calProgress.max = calGoal

        val currentProtein = totalProteins.roundToInt()
        val currentCarbs = totalCarbs.roundToInt()
        val currentFat = totalFats.roundToInt()
        val currentCal = totalCal.roundToInt()

        binding.macroProgressProtein.progress = currentProtein
        binding.macroProgressCarbs.progress = currentCarbs
        binding.macroProgressFat.progress = currentFat
        binding.calProgress.progress = currentCal

        binding.proteinGramTextview.text = "$currentProtein/$proteinGoal"
        binding.carbGramTextview.text = "$currentCarbs/$carbGoal"
        binding.fatGramTextview.text = "$currentFat/$fatGoal"
        binding.currentCalTextview.text = "$currentCal/$calGoal"

        binding.burntCaloriesTextview.text = "Burnt calories\n${add_excersise.result_cal_burnt}"
    }
//Dátumok és adatok ellenőrzése---------------------------------------------------------------------------
    private fun saveDateToPrefs(){
        val prefs = getSharedPreferences("Date", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val savedDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())

        editor.putString("savedDate", savedDate)
        editor.apply()
    }

    private fun saveExerciseDataDate() {
        val prefs = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val savedDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        editor.putString("savedDate", savedDate)
        editor.apply()
    }

    private fun checkAndResetData() {
        val prefs = getSharedPreferences("Date", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("savedDate", null)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())

        if (savedDate != today) {
            // Ha az étkezési adatok dátuma nem egyezik a mai dátummal
            val editor = getSharedPreferences("BreakfastData", Context.MODE_PRIVATE).edit()
            editor.clear()
            editor.apply()

            val lunchEditor = getSharedPreferences("LunchData", Context.MODE_PRIVATE).edit()
            lunchEditor.clear()
            lunchEditor.apply()

            val dinnerEditor = getSharedPreferences("DinnerData", Context.MODE_PRIVATE).edit()
            dinnerEditor.clear()
            dinnerEditor.apply()

            val snackEditor = getSharedPreferences("SnackData", Context.MODE_PRIVATE).edit()
            snackEditor.clear()
            snackEditor.apply()

            updateProgressBars()

            saveDateToPrefs()

            checkAndResetExerciseData()
        }
    }

    private fun checkAndResetExerciseData() {
        val prefs = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("savedDate", null)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())

        if (savedDate != today) {
            val editor = prefs.edit()
            editor.clear()
            editor.apply()

            updateProgressBars()

            saveExerciseDataDate()
        }
    }
}
