package com.example.campushub.data.model

/**
 * Data model representing a Rating (1 to 5 stars) given for an Event in CampusHub.
 */
data class EventRating(
    val eventoId: Int = 0,
    val autorEmail: String = "",
    val nota: Int = 0
)
