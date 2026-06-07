package com.beerme.ui.details

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tasting-note scores come in whole- or half-point increments: render 15.0 as
 * "15" and 15.5 as "15½" (rounding any unexpected value to the nearest half).
 */
fun formatHalfScore(score: Double): String {
    val whole = score.toInt()
    val fraction = score - whole
    return when {
        fraction < 0.25 -> "$whole"
        fraction < 0.75 -> "$whole½"
        else -> "${whole + 1}"
    }
}

/** Formats a "yyyy-MM-dd" sampled date as e.g. "January 7, 2023". */
fun formatSampledDate(sampled: String): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(sampled)
    SimpleDateFormat("MMMM d, yyyy", Locale.US).format(parsed!!)
}.getOrDefault(sampled)
