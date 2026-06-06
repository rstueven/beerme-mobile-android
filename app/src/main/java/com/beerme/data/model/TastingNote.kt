package com.beerme.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beerme.data.remote.FlexibleDouble
import com.squareup.moshi.Json

/**
 * Matches the beerme.com/mobile/v3/beerNoteList.php response:
 * {"id":"12477","beer_id":"65537","package":"draught","score":"20.00",
 *  "sampled":"2026-05-30","place":"at the brewery",
 *  "appearancescore":"3.00","appearance":"Bright golden. Thick head.",
 *  "aromascore":"4.00","aroma":"Fruity hops...",
 *  "mouthfeelscore":"10.00","mouthfeel":"Medium-big body...",
 *  "overallscore":"3.00","notes":"Great beer."}
 *
 * Sub-scores follow the BeerMe 20-point scale: appearance /3, aroma /4,
 * mouthfeel /10, overall /3. A beer may have several notes (re-tastings).
 * Older notes carry only the total score; all detail fields are nullable.
 *
 * No DB foreign key to Beer: the feeds sync independently, and REPLACE
 * inserts on beers must not cascade-delete notes.
 */
@Entity(
    tableName = "tasting_notes",
    indices = [Index(value = ["beerId"])]
)
data class TastingNote(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "beer_id") val beerId: String,
    @Json(name = "package") val packaging: String?,
    @FlexibleDouble
    @Json(name = "score") val score: Double?,
    @Json(name = "sampled") val sampled: String?,
    @Json(name = "place") val place: String?,
    @FlexibleDouble
    @Json(name = "appearancescore") val appearanceScore: Double?,
    @Json(name = "appearance") val appearance: String?,
    @FlexibleDouble
    @Json(name = "aromascore") val aromaScore: Double?,
    @Json(name = "aroma") val aroma: String?,
    @FlexibleDouble
    @Json(name = "mouthfeelscore") val mouthfeelScore: Double?,
    @Json(name = "mouthfeel") val mouthfeel: String?,
    @FlexibleDouble
    @Json(name = "overallscore") val overallScore: Double?,
    @Json(name = "notes") val notes: String?
)
