package com.example.beadando.meals

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.beadando.R
import com.example.beadando.model.MealItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class Dinner : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mealItems: ArrayList<MealItem>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var deleteButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dinner)

        sharedPreferences = getSharedPreferences("DinnerData", MODE_PRIVATE)

        // Ellenőrizd, hogy szükséges-e reset-elni (napi váltáskor)
        checkAndResetData()

        // Betöltjük az elmentett adatokat
        loadMealItems()

        listView = findViewById(R.id.dinner_listview)
        deleteButton = findViewById(R.id.delete_food_button)

        // Adapter beállítása (fehér szöveg az elemeknél)
        adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, formatMealItems()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                view.setSingleLine(false)
                view.ellipsize = null
                view.maxLines = Integer.MAX_VALUE // Engedélyezi a korlátlan sort
                view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) // Dinamikus méret
                view.isClickable = false
                view.isFocusable = false
                return view
            }
        }
        listView.adapter = adapter

        // Új adatok hozzáadása az Intentből, ha vannak
        val newItems = intent?.getSerializableExtra("mealItems") as? ArrayList<MealItem>
        if (newItems != null) {
            mealItems.addAll(newItems) // Új elemek hozzáadása a meglévő listához
            saveMealItems()           // Mentés SharedPreferences-be
            updateListView()          // Frissítés
        }
        deleteButton.setOnClickListener {
            deleteSelectedItem()
        }
    }

    private fun deleteSelectedItem() {
        val selectedPosition = listView.checkedItemPosition

        if (selectedPosition != ListView.INVALID_POSITION) {
            // Csak a kijelölt elemet töröljük
            mealItems.removeAt(selectedPosition)  // Törlés a listából

            // Mentés a SharedPreferences-be
            saveMealItems()

            // Frissítjük a ListView-t
            updateListView()

            // Kijelölés törlése
            listView.clearChoices()
        } else {
            // Ha nincs kijelölve elem
            Toast.makeText(this, "Select a meal you want to remove.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatMealItems(): List<String> {
        return mealItems.map {
            "\nProduct: ${it.productName}\n" +
                    "Portion: ${it.portionSize}g\n" +
                    "Calories: ${String.format("%.2f", it.calories)} kcal\n" +
                    "Protein: ${String.format("%.2f", it.proteins)}g\n" +
                    "Carbs: ${String.format("%.2f", it.carbs)}g\n" +
                    "Fats: ${String.format("%.2f", it.fats)}g\n"
        }
    }

    override fun onPause() {
        super.onPause()
        saveMealItems()
    }

    private fun saveMealItems() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(mealItems)
        editor.putString("mealItems", json)
        editor.apply()
    }

    private fun loadMealItems() {
        val gson = Gson()
        val json = sharedPreferences.getString("mealItems", null)
        val type = object : TypeToken<ArrayList<MealItem>>() {}.type
        mealItems = if (json != null) gson.fromJson(json, type) else ArrayList()
    }

    private fun updateListView() {
        adapter.clear()
        adapter.addAll(formatMealItems())
        adapter.notifyDataSetChanged()
    }

    // Ellenőrzi, hogy a mentett adatok dátuma egyezik-e a mai dátummal,
    // ha nem, akkor törli a mentett adatokat.
    private fun checkAndResetData() {
        val prefs = sharedPreferences
        val lastReset = prefs.getString("lastResetDate", null)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        if (lastReset == null || lastReset != today) {
            // Ha nincs dátum vagy más nap van, reseteljük a mentett adatokat
            mealItems = ArrayList()  // Vagy ha már betöltötted, akkor töröld: mealItems.clear()
            saveMealItems()
            // Frissítjük a lastResetDate értékét
            prefs.edit().putString("lastResetDate", today).apply()
        }
    }
}
