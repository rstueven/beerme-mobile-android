package com.beerme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

/**
 * A 120dp rounded thumbnail of a remote image that opens full-screen when
 * tapped (tap anywhere on the full-screen view, or back, to dismiss). Used for
 * brewery premises photos and beer beermats. The thumbnail is cropped to a
 * clean square; the full-screen view fits the whole image without cropping.
 */
@Composable
fun ZoomableThumbnail(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val thumbnailModifier = modifier
        .size(120.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable { expanded = true }
    if (LocalInspectionMode.current) {
        // AsyncImage can't fetch over the network in an IDE @Preview; show a
        // placeholder so the layout still renders.
        Box(modifier = thumbnailModifier.background(Color.LightGray))
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = thumbnailModifier
        )
    }
    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { expanded = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
