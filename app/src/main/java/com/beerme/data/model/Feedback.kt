package com.beerme.data.model

import com.squareup.moshi.Json

/**
 * Payload for POST mobile/v3/submitFeedback.php — a single endpoint that
 * handles both a correction to an existing brewery and a brand-new brewery
 * suggestion, distinguished by [type].
 *
 * Null fields are dropped by Moshi during serialization (no serializeNulls()),
 * so optional values — notably [email] — are only ever sent when present.
 */
data class FeedbackRequest(
    /** [TYPE_CORRECTION] for an existing brewery, [TYPE_NEW] for a suggestion. */
    @Json(name = "type") val type: String,
    /** The brewery being corrected; null for a new-brewery suggestion. */
    @Json(name = "breweryId") val breweryId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "hours") val hours: String? = null,
    @Json(name = "web") val web: String? = null,
    /** Bitmask of BreweryService flags; null for a correction. */
    @Json(name = "services") val services: Int? = null,
    /** Free-text description of the problem or details. Required. */
    @Json(name = "message") val message: String,
    /** Optional reply-to address; omitted from the payload when blank. */
    @Json(name = "email") val email: String? = null,
    @Json(name = "appVersion") val appVersion: String? = null,
    @Json(name = "platform") val platform: String = "android"
) {
    companion object {
        const val TYPE_CORRECTION = "correction"
        const val TYPE_NEW = "new"
    }
}

/** Server reply to a feedback submission. */
data class FeedbackResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "message") val message: String? = null
)
