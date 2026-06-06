package com.beerme.data.remote

import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import retrofit2.http.GET
import retrofit2.http.Query

interface BreweryApiService {
    @GET("mobile/v3/breweryList.php")
    suspend fun getBreweries(
        @Query("t") timestamp: String? = null
    ): List<Brewery>

    @GET("mobile/v3/beerList.php")
    suspend fun getBeers(
        @Query("t") timestamp: String? = null
    ): List<Beer>
}
