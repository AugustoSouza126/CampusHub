package com.example.campushub.data.model

import java.util.UUID

/**
 * Data model representing an Audio message recorded for an Event in CampusHub.
 */
data class EventAudio(
    val id: String = UUID.randomUUID().toString(),
    val eventoId: Int = 0,
    val caminhoArquivo: String = "",
    val duracaoTexto: String = "00:00",
    val autorNome: String = "",
    val autorEmail: String = "",
    val dataPublicacao: String = ""
)
