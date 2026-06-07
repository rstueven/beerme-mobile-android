package com.beerme.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.BreweryService
import com.beerme.data.model.BreweryStatus
import com.beerme.data.model.getAvailableServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreweryDetailsScreen(
    viewModel: BreweryDetailsViewModel,
    onBeerClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brewery by viewModel.brewery.collectAsState()
    val beers by viewModel.beers.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = brewery?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            brewery?.let { b ->
                item {
                    BreweryHeader(b)
                }
                item {
                    BreweryServices(b)
                }
                if (beers.isNotEmpty()) {
                    item {
                        Text(
                            text = "Beer Menu",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(beers) { beer ->
                        BeerListItem(
                            beer = beer,
                            onClick = { onBeerClick(beer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreweryHeader(brewery: Brewery) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Status is shown only when it isn't "Open" (the common, unremarkable case).
        BreweryStatus.entries.firstOrNull { it.code == brewery.status }
            ?.takeUnless { it == BreweryStatus.OPEN }
            ?.let { status ->
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (status == BreweryStatus.CLOSED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        brewery.address?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        if (brewery.status != BreweryStatus.CLOSED.code) {
            brewery.hours?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
        brewery.phone?.let {
            Text(text = "Phone: $it", style = MaterialTheme.typography.bodyMedium)
        }
        brewery.websiteUrl?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreweryServices(brewery: Brewery) {
    val services = brewery.getAvailableServices()
    // "Open to the Public" is the unremarkable default: never show it as a
    // positive chip; instead surface its absence with a "Not Open to the
    // Public" chip.
    val isOpenToPublic = BreweryService.OPEN in services
    val otherServices = services.filter { it != BreweryService.OPEN }
    if (otherServices.isNotEmpty() || !isOpenToPublic) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isOpenToPublic) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Not Open to the Public") }
                    )
                }
                otherServices.forEach { service ->
                    AssistChip(
                        onClick = {},
                        label = { Text(service.label) }
                    )
                }
            }
        }
    }
}

@Composable
fun BeerListItem(
    beer: Beer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = beer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            beer.style?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                beer.abv?.let {
                    Text(text = "ABV: $it%", style = MaterialTheme.typography.bodySmall)
                }
                beer.score?.let {
                    Text(
                        text = "Score: ${"%.1f".format(it)}/20",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
