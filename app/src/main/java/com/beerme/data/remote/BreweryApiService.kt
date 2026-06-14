package com.beerme.data.remote

import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.FeedbackRequest
import com.beerme.data.model.FeedbackResponse
import com.beerme.data.model.TastingNote
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @GET("mobile/v3/beerNoteList.php")
    suspend fun getBeerNotes(
        @Query("t") timestamp: String? = null
    ): List<TastingNote>

    // The app's only write call: a user-submitted brewery correction or a new
    // brewery suggestion. See FeedbackRequest for the JSON contract.
    @POST("mobile/v3/submitFeedback.php")
    suspend fun submitFeedback(@Body report: FeedbackRequest): FeedbackResponse
}
