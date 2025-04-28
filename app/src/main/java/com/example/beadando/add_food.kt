package com.example.beadando

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.beadando.databinding.ActivityAddFoodBinding
import com.example.beadando.meals.Breakfast
import com.example.beadando.meals.Dinner
import com.example.beadando.meals.Lunch
import com.example.beadando.meals.Snack
import com.example.beadando.model.FoodResponse
import com.example.beadando.model.MealItem
import com.example.beadando.model.Product
import com.example.beadando.model.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class add_food : AppCompatActivity() {
    lateinit var binding: ActivityAddFoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) {
                fetchNutritionData(query)
            } else {
                Toast.makeText(this, "Please enter a food name!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.breakfastButton.setOnClickListener{
            val intent = Intent(this, Breakfast::class.java)
            startActivity(intent)
        }
        binding.snackButton.setOnClickListener{
            val intent = Intent(this, Snack::class.java)
            startActivity(intent)
        }
        binding.lunchButton.setOnClickListener{
            val intent = Intent(this, Lunch::class.java)
            startActivity(intent)
        }
        binding.dinnerButton.setOnClickListener{
            val intent = Intent(this, Dinner::class.java)
            startActivity(intent)
        }
    }


    private fun fetchNutritionData(query: String) {
        val call = RetrofitClient.apiService.searchByName(query)

        call.enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                if (response.isSuccessful) {
                    val foodResponse = response.body()
                    if (foodResponse != null && foodResponse.products.isNotEmpty()) {
                        showProductSelectionDialog(foodResponse.products)
                    } else {
                        Toast.makeText(this@add_food, "No products found for query: $query", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@add_food, "API response error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                Toast.makeText(this@add_food, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProductSelectionDialog(products: List<Product>) {
        val productNames = products.map { it.product_name ?: "Unnamed product" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select a product")
            .setItems(productNames) { _, which ->
                showGramInputDialog(products[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGramInputDialog(product: Product) {
        val input = EditText(this)
        input.hint = "Enter amount in grams"

        AlertDialog.Builder(this)
            .setTitle("Enter Portion Size")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val gramsText = input.text.toString()
                if (gramsText.isNotEmpty()) {
                    val grams = gramsText.toDoubleOrNull()
                    if (grams != null && grams > 0) {
                        showMealSelectionDialog(product, grams)
                    } else {
                        Toast.makeText(this, "Invalid input, please enter a positive number!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a value!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMealSelectionDialog(product: Product, grams: Double) {
        val mealOptions = arrayOf("Breakfast", "Lunch", "Dinner", "Snack")

        AlertDialog.Builder(this)
            .setTitle("Select a Meal")
            .setItems(mealOptions) { _, which ->
                val selectedMeal = mealOptions[which]
                displayProductDetails(product, grams, selectedMeal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun displayProductDetails(product: Product, grams: Double, meal: String) {
        val multiplier = grams / 100.0

        val energyKj = (product.nutriments?.energy ?: 0.0) * multiplier
        val energyKcal = energyKj * 0.239006

        val proteins = (product.nutriments?.proteins ?: 0.0) * multiplier
        val carbs = (product.nutriments?.carbohydrates ?: 0.0) * multiplier
        val fats = (product.nutriments?.fat ?: 0.0) * multiplier

        val mealItem = MealItem(
            productName = product.product_name ?: "N/A",
            portionSize = grams,
            calories = energyKcal,
            proteins = proteins,
            carbs = carbs,
            fats = fats
        )

        val intent = when (meal) {
            "Breakfast" -> Intent(this, Breakfast::class.java)
            "Lunch" -> Intent(this, Lunch::class.java)
            "Dinner" -> Intent(this, Dinner::class.java)
            "Snack" -> Intent(this, Snack::class.java)
            else -> null
        }

        intent?.putExtra("mealItems", arrayListOf(mealItem))
        startActivity(intent)
    }
}
