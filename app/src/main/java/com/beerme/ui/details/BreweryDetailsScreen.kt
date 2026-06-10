package com.beerme.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.BreweryService
import com.beerme.data.model.BreweryStatus
import com.beerme.data.model.getAvailableServices
import com.beerme.ui.launchDirections

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
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf(false) }
    val imageUrl = brewery.image
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Premises photo, when the feed has one: a tappable thumbnail that
        // opens full-screen (see the dialog below).
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "${brewery.name} premises",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showFullImage = true }
            )
        }
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
        // Directions to the brewery, opened in an external navigation app where
        // the user picks the travel mode (driving/transit/biking/walking).
        val hasDestination = !brewery.address.isNullOrBlank() ||
            (brewery.latitude != null && brewery.longitude != null)
        if (hasDestination) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { launchDirections(context, brewery) }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Directions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Directions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        if (brewery.status != BreweryStatus.CLOSED.code) {
            brewery.hours?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
        brewery.phone?.let { phone ->
            Text(
                text = "Phone: $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    // ACTION_DIAL opens the dialer pre-filled; needs no permission.
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                        )
                    }
                }
            )
        }
        brewery.websiteUrl?.let { web ->
            // The feed omits the scheme (e.g. "www.example.com/"); add one so
            // the intent resolves to a browser.
            val url = if (web.startsWith("http://", true) || web.startsWith("https://", true)) {
                web
            } else {
                "https://$web"
            }
            Text(
                text = web,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            )
        }
    }

    // Full-screen view of the premises photo; tap anywhere (or back) to dismiss.
    if (showFullImage && !imageUrl.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${brewery.name} premises",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
