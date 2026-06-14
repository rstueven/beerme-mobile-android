package com.beerme.ui.feedback

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beerme.data.model.BreweryService
import com.beerme.data.model.FeedbackRequest
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.FeedbackRepository
import com.beerme.data.repository.SubmitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Form fields plus submission status for the feedback screen. */
data class FeedbackUiState(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val hours: String = "",
    val web: String = "",
    val services: Set<BreweryService> = emptySet(),
    val message: String = "",
    val email: String = "",
    val submitting: Boolean = false,
    /** True once the server has accepted the submission. */
    val done: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Backs the feedback form. With a non-null [breweryId] it reports a correction
 * to that brewery (the name is pre-filled for context); with null it collects a
 * brand-new brewery suggestion.
 */
class FeedbackViewModel(
    private val breweryId: String?,
    private val appVersion: String,
    private val feedbackRepository: FeedbackRepository,
    private val breweryRepository: BreweryRepository
) : ViewModel() {

    val isCorrection: Boolean = breweryId != null

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        if (breweryId != null) {
            viewModelScope.launch {
                breweryRepository.getBreweryById(breweryId)?.let { brewery ->
                    _uiState.update { it.copy(name = brewery.name) }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onHoursChange(value: String) = _uiState.update { it.copy(hours = value) }
    fun onWebChange(value: String) = _uiState.update { it.copy(web = value) }

    /** Toggle whether the suggested brewery offers [service]. */
    fun onToggleService(service: BreweryService) = _uiState.update {
        it.copy(
            services = if (service in it.services) {
                it.services - service
            } else {
                it.services + service
            }
        )
    }

    fun onMessageChange(value: String) = _uiState.update { it.copy(message = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    /** Whether [state] has the minimum valid input to submit. */
    fun canSubmit(state: FeedbackUiState): Boolean {
        if (state.submitting) return false
        if (state.message.isBlank()) return false
        // A new brewery needs at least a name; a correction already has one.
        if (!isCorrection && state.name.isBlank()) return false
        // Email is optional, but if supplied it must look valid.
        if (state.email.isNotBlank() && !isValidEmail(state.email)) return false
        return true
    }

    fun submit() {
        val state = _uiState.value
        if (!canSubmit(state)) return
        _uiState.update { it.copy(submitting = true, errorMessage = null) }
        viewModelScope.launch {
            val request = FeedbackRequest(
                type = if (isCorrection) {
                    FeedbackRequest.TYPE_CORRECTION
                } else {
                    FeedbackRequest.TYPE_NEW
                },
                breweryId = breweryId,
                name = state.name.trim().ifBlank { null },
                address = state.address.trim().ifBlank { null },
                phone = state.phone.trim().ifBlank { null },
                hours = state.hours.trim().ifBlank { null },
                web = state.web.trim().ifBlank { null },
                // Services only apply to a new-brewery suggestion; fold the
                // selected flags into the bitmask the feed uses.
                services = if (isCorrection) {
                    null
                } else {
                    state.services.fold(0) { acc, service -> acc or service.mask }
                },
                message = state.message.trim(),
                // Only ever transmitted when the user typed one.
                email = state.email.trim().ifBlank { null },
                appVersion = appVersion
            )
            when (val result = feedbackRepository.submit(request)) {
                is SubmitResult.Success -> _uiState.update {
                    it.copy(submitting = false, done = true, successMessage = result.message)
                }
                is SubmitResult.Error -> _uiState.update {
                    it.copy(submitting = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()
}
