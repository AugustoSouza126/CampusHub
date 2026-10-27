package com.example.campushub.data.model

import java.util.UUID

/**
 * Data model representing a Comment posted for an Event in CampusHub.
 */
data class EventComment(
    val id: String = UUID.randomUUID().toString(),
    val eventoId: Int = 0,
    val autorNome: String = "",
    val autorEmail: String = "",
    val texto: String = "",
    val dataPublicacao: String = ""
)
