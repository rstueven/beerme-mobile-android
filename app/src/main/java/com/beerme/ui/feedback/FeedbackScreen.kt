package com.beerme.ui.feedback

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.beerme.data.model.BreweryService

/** Where the in-app "Privacy Policy" links point. Update to your hosted page. */
private const val PRIVACY_URL = "https://beerme.com/privacy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isCorrection = viewModel.isCorrection
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface submission failures as a snackbar, then clear so it doesn't repeat.
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isCorrection) "Report a Problem" else "Suggest a Brewery")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.done) {
            SubmissionConfirmation(
                message = state.successMessage,
                onDone = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            FeedbackForm(
                state = state,
                isCorrection = isCorrection,
                canSubmit = viewModel.canSubmit(state),
                onName = viewModel::onNameChange,
                onAddress = viewModel::onAddressChange,
                onPhone = viewModel::onPhoneChange,
                onHours = viewModel::onHoursChange,
                onWeb = viewModel::onWebChange,
                onToggleService = viewModel::onToggleService,
                onMessage = viewModel::onMessageChange,
                onEmail = viewModel::onEmailChange,
                onSubmit = viewModel::submit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedbackForm(
    state: FeedbackUiState,
    isCorrection: Boolean,
    canSubmit: Boolean,
    onName: (String) -> Unit,
    onAddress: (String) -> Unit,
    onPhone: (String) -> Unit,
    onHours: (String) -> Unit,
    onWeb: (String) -> Unit,
    onToggleService: (BreweryService) -> Unit,
    onMessage: (String) -> Unit,
    onEmail: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isCorrection) {
                "Spotted something wrong? Tell us what to fix and we'll review it."
            } else {
                "Know a brewery that's missing? Share what you can — only the name " +
                    "and a note are required."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = onName,
            label = { Text(if (isCorrection) "Brewery" else "Brewery name") },
            singleLine = true,
            // For a correction the name is read-only context, not editable.
            readOnly = isCorrection,
            enabled = !isCorrection,
            modifier = Modifier.fillMaxWidth()
        )

        // The structured fields only make sense when proposing a new brewery;
        // for a correction the free-text note carries the detail.
        if (!isCorrection) {
            OutlinedTextField(
                value = state.address,
                onValueChange = onAddress,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = onPhone,
                label = { Text("Phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.hours,
                onValueChange = onHours,
                label = { Text("Hours") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.web,
                onValueChange = onWeb,
                label = { Text("Website") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Services",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BreweryService.entries.forEach { service ->
                    val selected = service in state.services
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleService(service) },
                        label = { Text(service.label) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.message,
            onValueChange = onMessage,
            label = {
                Text(if (isCorrection) "What's wrong?" else "Anything else we should know?")
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmail,
            label = { Text("Email (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Point-of-collection disclosure for the optional email (Google Play
        // Data Safety / personal-data handling).
        Column {
            Text(
                text = "Optional. We'll only use your email to follow up about this " +
                    "submission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_URL.toUri()))
                    }
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("Privacy Policy")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun SubmissionConfirmation(
    message: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message ?: "Thanks — we'll review your submission.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}
