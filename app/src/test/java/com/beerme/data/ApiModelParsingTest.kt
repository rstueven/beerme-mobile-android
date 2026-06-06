package com.beerme.data

import com.beerme.data.model.Beer
import com.beerme.data.model.Brewery
import com.beerme.data.model.BreweryService
import com.beerme.data.model.TastingNote
import com.beerme.data.model.getAvailableServices
import com.beerme.data.remote.FlexibleDoubleAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies the Moshi configuration against verbatim samples of the
 * beerme.com/mobile/v3 API responses (numbers arrive as JSON strings).
 */
class ApiModelParsingTest {

    private val moshi = Moshi.Builder()
        .add(FlexibleDoubleAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `parses brewery list sample`() {
        val json = """
            [{"id":"35436","name":"Lady Brewery","address":"Grandagarður 93, Reykjavík, Iceland",
              "latitude":"64.1556368","longitude":"-21.9419939","status":"1","updated":"2026-04-18",
              "services":145,"phone":"+354 6624864","hours":"Thursday-Friday 11am-8pm.","web":"www.ladybrewery.com/"},
             {"id":"35434","name":"Slieve Bloom Brewing","address":"Main Street, The Walk, Co. Offaly, Ireland",
              "latitude":"53.09784","longitude":"-7.7191","status":"8","updated":"2026-04-18","services":0}]
        """.trimIndent()

        val type = Types.newParameterizedType(List::class.java, Brewery::class.java)
        val breweries = moshi.adapter<List<Brewery>>(type).fromJson(json)!!

        assertEquals(2, breweries.size)
        val lady = breweries[0]
        assertEquals("35436", lady.id)
        assertEquals(64.1556368, lady.latitude!!, 1e-9)
        assertEquals(-21.9419939, lady.longitude!!, 1e-9)
        assertEquals("1", lady.status)
        assertEquals("2026-04-18", lady.updated)
        assertEquals(
            listOf(BreweryService.OPEN, BreweryService.GIFTSHOP, BreweryService.RETAIL),
            lady.getAvailableServices()
        )
        assertEquals("www.ladybrewery.com/", lady.websiteUrl)
        // Optional fields absent in the second record
        assertNull(breweries[1].phone)
        assertNull(breweries[1].hours)
    }

    @Test
    fun `parses beer list sample`() {
        val json = """
            [{"id":"65537","brewery_id":"17243","name":"Pizza Time!","style":"American-Style India Pale Ale",
              "abv":"7.20","updated":"2026-05-31","score":"20.000000"},
             {"id":"65538","brewery_id":"13556","name":"Schlitz","style":"American-Style Lager",
              "abv":null,"updated":"2026-06-05"}]
        """.trimIndent()

        val type = Types.newParameterizedType(List::class.java, Beer::class.java)
        val beers = moshi.adapter<List<Beer>>(type).fromJson(json)!!

        assertEquals(2, beers.size)
        val pizza = beers[0]
        assertEquals("17243", pizza.breweryId)
        assertEquals(7.20, pizza.abv!!, 1e-9)
        assertEquals(20.0, pizza.score!!, 1e-9)
        assertNull(beers[1].abv)
        assertNull(beers[1].score)
    }

    @Test
    fun `parses beer note list sample`() {
        val json = """
            [{"id":"12477","beer_id":"65537","package":"draught","score":"20.00","sampled":"2026-05-30",
              "place":"at the brewery","appearancescore":"3.00","appearance":"Bright golden. Thick head.",
              "aromascore":"4.00","aroma":"Fruity hops, citrusy and floral. Mild biscuity malt.",
              "mouthfeelscore":"10.00","mouthfeel":"Medium-big body, smooth.",
              "overallscore":"3.00","notes":"Great beer."},
             {"id":"100","beer_id":"1234","package":"bottle","score":"14.00","sampled":"2001-07-04",
              "place":"home","appearancescore":"2.00","appearance":null,"aromascore":"3.00","aroma":null,
              "mouthfeelscore":"7.00","mouthfeel":null,"overallscore":"2.00","notes":null}]
        """.trimIndent()

        val type = Types.newParameterizedType(List::class.java, TastingNote::class.java)
        val notes = moshi.adapter<List<TastingNote>>(type).fromJson(json)!!

        assertEquals(2, notes.size)
        val full = notes[0]
        assertEquals("65537", full.beerId)
        assertEquals("draught", full.packaging)
        assertEquals(20.0, full.score!!, 1e-9)
        assertEquals(3.0, full.appearanceScore!!, 1e-9)
        assertEquals(4.0, full.aromaScore!!, 1e-9)
        assertEquals(10.0, full.mouthfeelScore!!, 1e-9)
        assertEquals(3.0, full.overallScore!!, 1e-9)
        assertEquals("Great beer.", full.notes)
        // Older notes carry scores but no descriptive text
        val sparse = notes[1]
        assertEquals(14.0, sparse.score!!, 1e-9)
        assertNull(sparse.appearance)
        assertNull(sparse.notes)
    }
}
