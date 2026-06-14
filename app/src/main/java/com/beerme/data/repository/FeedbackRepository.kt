package com.beerme.data.repository

import com.beerme.data.model.FeedbackRequest
import com.beerme.data.remote.BreweryApiService
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/** Outcome of a feedback submission, distinguishing the cases the UI surfaces. */
sealed interface SubmitResult {
    data class Success(val message: String?) : SubmitResult
    data class Error(val message: String?) : SubmitResult
}

/**
 * Sends user-submitted corrections and new-brewery suggestions to beerme.com.
 * The read-only sync lives in [BreweryRepository]; this is the app's only
 * write path, so it shares that service's Retrofit instance and base URL.
 */
class FeedbackRepository(
    private val apiService: BreweryApiService
) {
    suspend fun submit(request: FeedbackRequest): SubmitResult {
        return try {
            val response = apiService.submitFeedback(request)
            if (response.ok) {
                SubmitResult.Success(response.message)
            } else {
                SubmitResult.Error(response.message ?: "Couldn't send your submission")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            SubmitResult.Error("Couldn't send your submission (${e.code()})")
        } catch (e: Exception) {
            SubmitResult.Error("Couldn't send your submission — check your connection")
        }
    }
}
