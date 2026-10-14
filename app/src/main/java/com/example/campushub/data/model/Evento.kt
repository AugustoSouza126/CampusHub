package com.example.campushub.data.model

data class Evento(
    val id: Int,
    val titulo: String,
    val categoria: String,
    val descricao: String,
    val data: String,
    val dataBadge: String,
    val horario: String,
    val local: String,
    val vagas: Int,
    val encerrado: Boolean = false
)
