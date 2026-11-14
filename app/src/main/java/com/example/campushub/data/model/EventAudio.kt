package com.example.campushub.data.model

import java.util.UUID

data class EventAudio(
    val id: String = UUID.randomUUID().toString(),
    val eventoId: Int = 0,
    val caminhoArquivo: String = "",
    val duracaoTexto: String = "00:00",
    val autorNome: String = "",
    val autorEmail: String = "",
    val dataPublicacao: String = ""
)
