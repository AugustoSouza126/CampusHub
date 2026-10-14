package com.example.campushub.data.model

import java.util.UUID

data class Comentario(
    val id: String = UUID.randomUUID().toString(),
    val eventoId: Int,
    val autorNome: String,
    val autorEmail: String,
    val texto: String,
    val dataPublicacao: String
)
