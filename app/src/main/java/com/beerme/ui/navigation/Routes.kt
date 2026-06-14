package com.beerme.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object MapRoute : NavKey

@Serializable
data class BreweryDetailsRoute(val breweryId: String) : NavKey

@Serializable
data class BeerDetailsRoute(val beerId: String) : NavKey

@Serializable
object RoutePlannerRoute : NavKey

/**
 * The user feedback form. A non-null [breweryId] reports a correction to that
 * brewery; null opens a blank form to suggest a brewery not yet in the list.
 */
@Serializable
data class FeedbackRoute(val breweryId: String? = null) : NavKey
