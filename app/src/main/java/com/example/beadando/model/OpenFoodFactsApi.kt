package com.example.beadando.model

import com.example.beadando.model.FoodResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")  // A végpont pontosítása
    fun searchByName(
        @Query("search_terms") searchTerm: String,
        @Query("json") json: Boolean = true // Az Open Food Facts API json=true paramétert vár
    ): Call<FoodResponse>
}
