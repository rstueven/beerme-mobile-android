package com.beerme.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beerme.data.remote.FlexibleDouble
import com.squareup.moshi.Json

/**
 * Matches the beerme.com/mobile/v3/beerList.php response:
 * {"id":"65537","brewery_id":"17243","name":"Pizza Time!",
 *  "style":"American-Style India Pale Ale","abv":"7.20",
 *  "updated":"2026-05-31","score":"20.000000"}
 *
 * No DB foreign key to Brewery: the feeds are synced independently, and a
 * beer referencing a not-yet-synced brewery must not abort the whole batch.
 */
@Entity(
    tableName = "beers",
    indices = [Index(value = ["breweryId"])]
)
data class Beer(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "brewery_id") val breweryId: String,
    @Json(name = "name") val name: String,
    @Json(name = "style") val style: String?,
    @FlexibleDouble
    @Json(name = "abv") val abv: Double?,
    @FlexibleDouble
    @Json(name = "score") val score: Double? = null,
    @Json(name = "updated") val updated: String? = null,
    // URL of the beer's beermat (coaster) image, shown on the tasting-notes page.
    @Json(name = "beermatFile") val beermatFile: String? = null
)
