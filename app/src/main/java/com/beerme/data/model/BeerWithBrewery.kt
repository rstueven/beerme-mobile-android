package com.beerme.data.model

import androidx.room.Embedded

/**
 * A [Beer] paired with the name of the brewery that makes it, for search
 * results where the brewery name disambiguates same-named beers. [breweryName]
 * is null if the beer references a brewery not present in the local database.
 */
data class BeerWithBrewery(
    @Embedded val beer: Beer,
    val breweryName: String?
)
