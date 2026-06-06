package com.beerme.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * The beerme.com API encodes numeric values (latitude, longitude, abv, score)
 * as JSON strings, e.g. "latitude":"64.1556368". This qualifier tells Moshi to
 * accept either a string or a number for the annotated Double field.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@JsonQualifier
annotation class FlexibleDouble

class FlexibleDoubleAdapter {
    @FromJson
    @FlexibleDouble
    fun fromJson(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.STRING -> reader.nextString().toDoubleOrNull()
            JsonReader.Token.NUMBER -> reader.nextDouble()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @FlexibleDouble value: Double?) {
        writer.value(value)
    }
}
