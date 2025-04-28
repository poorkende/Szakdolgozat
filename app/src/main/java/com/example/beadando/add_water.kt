package com.example.beadando

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.beadando.databinding.ActivityAddWaterBinding
import java.text.SimpleDateFormat
import java.util.*

class add_water : AppCompatActivity() {

    private var dailyWaterIntake: Double = 0.0
    private var userWeight: Double = 0.0
    private var currentWaterIntake: Double = 0.0  // Az eddig ivott víz mennyisége
    private lateinit var binding: ActivityAddWaterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddWaterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndResetWaterIntake()
        loadUserDataFromPrefs()
        loadWaterIntake() // Betöltjük az előző fogyasztást, ha aznap már volt

        userWeight = calorie_count.userWeight.toDouble()
        dailyWaterIntake = userWeight * 0.03

        binding.recommendedWaterIntake.text = "Recommended water intake: $dailyWaterIntake L"
        updateWaterIntakeText()

        binding.addWaterButton.setOnClickListener {
            addWater()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun checkAndResetWaterIntake() {
        val prefs = getSharedPreferences("WaterIntake", Context.MODE_PRIVATE)
        val lastUpdateDate = prefs.getString("lastUpdateDate", "")
        val currentDate = getCurrentDate()

        if (lastUpdateDate != currentDate) {
            val editor = prefs.edit()
            editor.clear() // Töröljük az összes korábbi adatot
            editor.putString("lastUpdateDate", currentDate) // Új dátum mentése
            editor.apply()
        }
    }

    private fun loadUserDataFromPrefs() {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        calorie_count.userWeight = prefs.getInt("userWeight", 0)
    }

    private fun loadWaterIntake() {
        val prefs = getSharedPreferences("WaterIntake", Context.MODE_PRIVATE)
        currentWaterIntake = prefs.getFloat("currentWaterIntake", 0.0f).toDouble()
    }

    private fun saveWaterIntake() {
        val prefs = getSharedPreferences("WaterIntake", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putFloat("currentWaterIntake", currentWaterIntake.toFloat())
        editor.putString("lastUpdateDate", getCurrentDate())
        editor.apply()
    }

    private fun updateWaterIntakeText() {
        binding.yourWaterIntake.text = "Current total water intake: ${"%.2f".format(currentWaterIntake)} L"
    }

    private fun addWater() {
        val inputText = binding.addWaterEdittext.text.toString()
        if (inputText.isNotEmpty()) {
            val addedWater = inputText.toDoubleOrNull()
            if (addedWater != null && addedWater > 0) {
                currentWaterIntake += addedWater / 10  // dl-t literre váltjuk
                saveWaterIntake()
                updateWaterIntakeText()
                binding.addWaterEdittext.text.clear()
                Toast.makeText(this, "Water intake updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid number!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter a value!", Toast.LENGTH_SHORT).show()
        }
        if (dailyWaterIntake<currentWaterIntake)
            Toast.makeText(this, "Congratulation you reached your goal!\uD83E\uDD64",Toast.LENGTH_SHORT).show()
    }
}
