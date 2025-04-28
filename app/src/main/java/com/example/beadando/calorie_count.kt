package com.example.beadando

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.beadando.databinding.ActivityCalorieCountBinding
import kotlin.math.roundToInt

class calorie_count : AppCompatActivity() {
    private lateinit var binding: ActivityCalorieCountBinding

    companion object {
        var dailyCalorieIntake: Double = 0.0
        var calorieGoal: Int = 0
        var requiredProteinGrams: Double = 0.0
        var requiredCarbGrams: Double = 0.0
        var requiredFatGrams: Double = 0.0
        var BMI: Int = 0

        var protein_number = 0
        var carb_number = 0
        var fat_number = 0

        var userName = ""
        var userWeight = 0
        var userHeight = 0
        var userAge = 0
        var activityLevel = 0  // új változó a fizikai aktivitás tárolására
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Betöltjük a korábban elmentett adatokat
        loadUserData()

        val activityList = arrayOf("BMR", "Light activity", "Medium activity", "High activity", "Intense activity")
        val spinner = findViewById<Spinner>(R.id.activitySpinner)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.countResoultButton.setOnClickListener {
            val age_input = binding.ageEdittext.text.toString()
            val weight_input = binding.weightEdittext.text.toString()
            val height_input = binding.hightEdittext.text.toString()
            val name_input = binding.nameEdittext.text.toString()

            if (age_input.isEmpty() || weight_input.isEmpty() || height_input.isEmpty() || name_input.isEmpty()) {
                Toast.makeText(this, "Please enter all the necessary values!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Felhasználói adatok mentése
            userName = name_input
            userAge = age_input.toInt()
            userWeight = weight_input.toInt()
            userHeight = height_input.toInt()


            // Az aktivitás szintje alapján számoljuk ki a kalóriákat
            val selectedActivityLevel = spinner.selectedItemPosition
            activityLevel = selectedActivityLevel  // Mentjük el az aktivitás szintjét
            var multiplier: Double = 0.0
            // Aktivitás szintje alapján kalóriák módosítása
            when (activityLevel) {
                0 -> multiplier = 1.0  // Ülő munka
                1 -> multiplier = 1.2  // Enyhe aktivitás
                2 -> multiplier = 1.5 // Mérsékelt aktivitás
                3 -> multiplier = 1.7 // Nagyon aktív
                4 -> multiplier = 2.0  // Extra aktív
            }

            // Kalóriaszükséglet számítása
            val resoult: Double = if (binding.radioMale.isChecked) {
                ((10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5)*multiplier
            } else if (binding.radioFemale.isChecked) {
                ((10 * userWeight) + (6.25 * userHeight) - (5 * userAge) - 161)*multiplier
            } else {
                Toast.makeText(this, "Please pick your gender!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightInMeters = userHeight / 100.0
            BMI = (userWeight / (heightInMeters * heightInMeters)).roundToInt()

            binding.bmiResult.text = "Your BMI: $BMI"

            dailyCalorieIntake = resoult.roundToInt().toDouble()
            binding.resoultTextview.text = "Your Result: $dailyCalorieIntake kcal"

            // Makrók kiszámítása
            protein_number = binding.proteinEdittext.text.toString().toInt()
            carb_number = binding.carbEdittext.text.toString().toInt()
            fat_number = binding.fatEdittext.text.toString().toInt()
            val calorieGoalInput = binding.calorieGoalEdittext.text.toString()

            if (calorieGoalInput.isNullOrEmpty() || calorieGoalInput == "0") {
                calorieGoal = dailyCalorieIntake.toInt()
            } else {
                calorieGoal = calorieGoalInput.toInt()
            }
            binding.calorieGoalResultTextview.text = "Your Calorie Goal: $calorieGoal kcal"

            requiredProteinGrams = (calorieGoal * protein_number / 100.0) / 4.0
            requiredCarbGrams = (calorieGoal * carb_number / 100.0) / 4.0
            requiredFatGrams = (calorieGoal * fat_number / 100.0) / 9.0



            binding.resoultTextview.text = "Your Result: $dailyCalorieIntake kcal"

            // Mentés SharedPreferences-be
            saveUserData()



        }
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("userName", userName)
        editor.putInt("userAge", userAge)
        editor.putInt("userWeight", userWeight)
        editor.putInt("userHeight", userHeight)
        editor.putInt("dailyCalorieIntake", dailyCalorieIntake.toInt())
        editor.putInt("userCalorieGoal", calorieGoal)
        editor.putInt("proteinPercentage", protein_number)
        editor.putInt("carbPercentage", carb_number)
        editor.putInt("fatPercentage", fat_number)
        editor.putFloat("requiredProteinGrams", requiredProteinGrams.toFloat())
        editor.putFloat("requiredCarbGrams", requiredCarbGrams.toFloat())
        editor.putFloat("requiredFatGrams", requiredFatGrams.toFloat())
        editor.putInt("userBMI", BMI)
        editor.putInt("activityLevel", activityLevel)

        if (binding.radioMale.isChecked) {
            editor.putBoolean("userGenderMale", true)
        } else if (binding.radioFemale.isChecked) {
            editor.putBoolean("userGenderMale", false)
        }

        activityLevel = prefs.getInt("activityLevel", 0)
        Log.d("CalorieCount", "Loaded activity level: $activityLevel")

        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserData() {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        if (prefs.contains("dailyCalorieIntake")) {
            userName = prefs.getString("userName", "") ?: ""
            userAge = prefs.getInt("userAge", 0)
            userWeight = prefs.getInt("userWeight", 0)
            userHeight = prefs.getInt("userHeight", 0)
            dailyCalorieIntake = prefs.getInt("dailyCalorieIntake", 0).toDouble()
            protein_number = prefs.getInt("proteinPercentage", 0)
            carb_number = prefs.getInt("carbPercentage", 0)
            fat_number = prefs.getInt("fatPercentage", 0)
            requiredProteinGrams = prefs.getFloat("requiredProteinGrams", 0f).toDouble()
            requiredCarbGrams = prefs.getFloat("requiredCarbGrams", 0f).toDouble()
            requiredFatGrams = prefs.getFloat("requiredFatGrams", 0f).toDouble()
            BMI = prefs.getInt("userBMI", 0)
            calorieGoal = prefs.getInt("userCalorieGoal", 0)
            activityLevel = prefs.getInt("activityLevel", 0) 

            binding.nameEdittext.setText(userName)
            binding.ageEdittext.setText(userAge.toString())
            binding.weightEdittext.setText(userWeight.toString())
            binding.hightEdittext.setText(userHeight.toString())

            binding.resoultTextview.text = "Your Result: $dailyCalorieIntake kcal"
            binding.bmiResult.text = "Your BMI: $BMI"
            binding.calorieGoalResultTextview.text = "Your Calorie Goal: $calorieGoal kcal"
            binding.proteinEdittext.setText(protein_number.toString())
            binding.carbEdittext.setText(carb_number.toString())
            binding.fatEdittext.setText(fat_number.toString())

            val isMale = prefs.getBoolean("userGenderMale", true)
            if (isMale) {
                binding.radioMale.isChecked = true
            } else {
                binding.radioFemale.isChecked = true
            }



            // Az aktivitás szintjének beállítása a spinnerben
            val spinner = binding.activitySpinner
            spinner.setSelection(activityLevel)
        }
    }
}
