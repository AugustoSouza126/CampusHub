package com.example.campushub.data.model

import java.util.UUID

/**
 * Data model representing a Photo uploaded for an Event in CampusHub.
 */
data class EventPhoto(
    val id: String = UUID.randomUUID().toString(),
    val eventoId: Int = 0,
    val caminhoArquivo: String = "",
    val autorNome: String = "",
    val autorEmail: String = "",
    val dataPublicacao: String = ""
)
