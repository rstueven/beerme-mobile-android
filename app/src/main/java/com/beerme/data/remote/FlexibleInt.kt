package com.beerme.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Like [FlexibleDouble], but for integer fields the beerme.com API may encode as
 * JSON strings, e.g. "geoprecision":"6". Accepts either a string or a number for
 * the annotated Int field.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@JsonQualifier
annotation class FlexibleInt

class FlexibleIntAdapter {
    @FromJson
    @FlexibleInt
    fun fromJson(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.STRING -> reader.nextString().toIntOrNull()
            JsonReader.Token.NUMBER -> reader.nextInt()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @FlexibleInt value: Int?) {
        writer.value(value)
    }
}
