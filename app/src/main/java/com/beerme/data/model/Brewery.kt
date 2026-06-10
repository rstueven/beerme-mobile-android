package com.beerme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.beerme.data.remote.FlexibleDouble
import com.beerme.data.remote.FlexibleInt
import com.squareup.moshi.Json

/**
 * Matches the beerme.com/mobile/v3/breweryList.php response:
 * {"id":"35436","name":"Lady Brewery","address":"Grandagarður 93, Reykjavík, Iceland",
 *  "latitude":"64.1556368","longitude":"-21.9419939","status":"1","updated":"2026-04-18",
 *  "services":145,"phone":"+354 6624864","hours":"...","web":"www.ladybrewery.com/"}
 *
 * Parsed via Moshi reflection (KotlinJsonAdapterFactory): codegen (KSP2) cannot
 * process the in-module @FlexibleDouble qualifier.
 */
@Entity(tableName = "breweries")
data class Brewery(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "address") val address: String?,
    @FlexibleDouble
    @Json(name = "latitude") val latitude: Double?,
    @FlexibleDouble
    @Json(name = "longitude") val longitude: Double?,
    // How precisely the brewery is geolocated. Below 8 the coordinates are only
    // approximate, so directions are launched with a warning (see launchDirections).
    @FlexibleInt
    @Json(name = "geoprecision") val geoprecision: Int? = null,
    @Json(name = "status") val status: String?,
    @Json(name = "services") val services: Int = 0,
    @Json(name = "phone") val phone: String?,
    @Json(name = "hours") val hours: String?,
    @Json(name = "web") val websiteUrl: String?,
    @Json(name = "image") val image: String? = null,
    @Json(name = "updated") val updated: String? = null
)

enum class BreweryStatus(val code: String, val label: String) {
    OPEN("1", "Open"),
    PLANNED("2", "Planned"),
    UNKNOWN("4", "No Longer Brewing"),
    CLOSED("8", "Closed")
}

enum class BreweryService(val mask: Int, val label: String) {
    OPEN(0x0001, "Open to the Public"),
    BAR(0x0002, "Bar"),
    BEERGARDEN(0x0004, "Beer Garden"),
    FOOD(0x0008, "Food"),
    GIFTSHOP(0x0010, "Gift Shop"),
    HOTEL(0x0020, "Hotel"),
    INTERNET(0x0040, "Internet Access"),
    RETAIL(0x0080, "Beer To Go"),
    TOURS(0x0100, "Tours")
}

fun Brewery.getAvailableServices(): List<BreweryService> {
    return BreweryService.entries.filter { (services and it.mask) != 0 }
}
