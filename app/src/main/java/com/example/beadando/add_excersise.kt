package com.example.beadando

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beadando.databinding.ActivityAddExcersiseBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class add_excersise : AppCompatActivity() {
    companion object {
        var result_cal_burnt: Int = 0
        val exerciseList = mutableListOf<Exercise>()
    }

    lateinit var binding: ActivityAddExcersiseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddExcersiseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAndResetData() // Ellenőrzi a dátumot és resetel, ha kell
        loadSavedData()
        updateUI()

        binding.saveExerciseButton.setOnClickListener {
            val exerciseName = binding.addExerciseNameEdittext.text.toString()
            val exerciseTime = binding.addExerciseTimeEdittext.text.toString()
            val caloriesText = binding.addExerciseEdittext.text.toString()

            if (exerciseName.isNotEmpty() && exerciseTime.isNotEmpty() && caloriesText.isNotEmpty()) {
                val caloriesBurnt = caloriesText.toIntOrNull()

                if (caloriesBurnt != null && caloriesBurnt > 0) {
                    val newExercise = Exercise(exerciseName, exerciseTime, caloriesBurnt)
                    exerciseList.add(newExercise)
                    result_cal_burnt += caloriesBurnt
                    saveData()
                    updateUI()

                    binding.addExerciseNameEdittext.text.clear()
                    binding.addExerciseTimeEdittext.text.clear()
                    binding.addExerciseEdittext.text.clear()
                } else {
                    Toast.makeText(this, "Please enter a valid calorie value.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backToMainMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CaloriesBurnt", result_cal_burnt)
            startActivity(intent)
        }
    }

    private fun checkAndResetData() {
        val sharedPreferences = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)
        val savedDate = sharedPreferences.getString("last_workout_date", null)
        val todayDate = getCurrentDate()

        if (savedDate == null || savedDate != todayDate) {
            // Előző adatok törlése
            sharedPreferences.edit().clear().apply()

            // Új dátum mentése
            sharedPreferences.edit()
                .putString("last_workout_date", todayDate)
                .apply()

            // Memóriában is nullázunk
            result_cal_burnt = 0
            exerciseList.clear()
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonArray = JSONArray()
        for (exercise in exerciseList) {
            val jsonObject = JSONObject()
            jsonObject.put("name", exercise.name)
            jsonObject.put("time", exercise.time)
            jsonObject.put("calories", exercise.caloriesBurnt)
            jsonArray.put(jsonObject)
        }

        editor.putString("exercises", jsonArray.toString())
        editor.putInt("total_calories", result_cal_burnt)
        editor.apply()
    }

    private fun loadSavedData() {
        val sharedPreferences = getSharedPreferences("exercise_data", Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("exercises", null)
        result_cal_burnt = sharedPreferences.getInt("total_calories", 0)

        exerciseList.clear()

        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val exercise = Exercise(
                    jsonObject.getString("name"),
                    jsonObject.getString("time"),
                    jsonObject.getInt("calories")
                )
                exerciseList.add(exercise)
            }
        }
    }

    private fun updateUI() {
        binding.yourBurntCalories.text = "Total burnt calories\n $result_cal_burnt Calories"
        binding.totalWorkouts.text = "Total workouts\n ${exerciseList.size}"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    data class Exercise(val name: String, val time: String, val caloriesBurnt: Int)
}
