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
