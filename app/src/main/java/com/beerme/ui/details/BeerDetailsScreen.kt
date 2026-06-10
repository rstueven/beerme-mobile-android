package com.beerme.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beerme.data.model.Beer
import com.beerme.data.model.TastingNote
import com.beerme.ui.ZoomableThumbnail
import com.beerme.ui.theme.BeerMeMobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerDetailsScreen(
    viewModel: BeerDetailsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beer by viewModel.beer.collectAsState()
    val breweryName by viewModel.breweryName.collectAsState()
    val notes by viewModel.tastingNotes.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = beer?.name ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Brewery name as a subtitle directly under the beer name.
                        breweryName?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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
            beer?.let { b ->
                item {
                    BeerHeader(b)
                }
                item {
                    TastingNotesHeader(score = b.score)
                }
                if (notes.isEmpty()) {
                    item {
                        Text(
                            text = "No tasting notes yet.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(notes) { note ->
                        TastingNoteItem(note)
                    }
                }
            }
        }
    }
}

@Composable
fun BeerHeader(beer: Beer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Style and ABV at the top-left.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            beer.style?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            beer.abv?.let {
                Text(text = "ABV: $it%", style = MaterialTheme.typography.bodyLarge)
            }
        }
        // Beermat (coaster) image on the right, when the feed has one: a
        // tappable thumbnail that opens full-screen.
        beer.beermatFile?.takeIf { it.isNotBlank() }?.let { beermatUrl ->
            ZoomableThumbnail(
                url = beermatUrl,
                contentDescription = "${beer.name} beermat"
            )
        }
    }
}

/**
 * Section header for the tasting notes, with the beer's overall (average) score
 * shown on the right, aligned with the "Tasting Notes" title.
 */
@Composable
fun TastingNotesHeader(score: Double?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tasting Notes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        score?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${"%.1f".format(it)}/20",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TastingNoteItem(note: TastingNote) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.sampled?.let { formatSampledDate(it) } ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                note.score?.let {
                    Text(
                        text = "${formatHalfScore(it)}/20",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            val context = listOfNotNull(note.packaging, note.place).joinToString(", ")
            if (context.isNotEmpty()) {
                Text(
                    text = context,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            TastingAspect("Appearance", note.appearanceScore, 3, note.appearance)
            TastingAspect("Aroma", note.aromaScore, 4, note.aroma)
            TastingAspect("Mouthfeel & Flavor", note.mouthfeelScore, 10, note.mouthfeel)
            TastingAspect("Overall", note.overallScore, 3, note.notes)
        }
    }
}

@Composable
private fun TastingAspect(label: String, score: Double?, maxScore: Int, text: String?) {
    if (score == null && text.isNullOrBlank()) return
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        score?.let {
            Text(
                text = "${formatHalfScore(it)}/$maxScore",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    if (!text.isNullOrBlank()) {
        // Tasting-note prose may contain HTML (e.g. <em>, <a href>).
        Text(
            text = AnnotatedString.fromHtml(
                text,
                linkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val previewBeer = Beer(
    id = "1",
    breweryId = "1",
    name = "Dark Times",
    style = "Baltic-Style Porter",
    abv = 7.8,
    score = 16.0,
    beermatFile = "https://example.com/beermat.png"
)

@Preview(showBackground = true)
@Composable
private fun BeerHeaderPreview() {
    BeerMeMobileTheme {
        BeerHeader(previewBeer)
    }
}

@Preview(showBackground = true)
@Composable
private fun TastingNotesHeaderPreview() {
    BeerMeMobileTheme {
        TastingNotesHeader(score = 16.0)
    }
}
