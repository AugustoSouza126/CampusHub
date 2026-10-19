package com.example.campushub.data.model

data class Evento(
    val id: Int = 0,
    val titulo: String = "",
    val categoria: String = "",
    val descricao: String = "",
    val data: String = "",
    val dataBadge: String = "",
    val horario: String = "",
    val local: String = "",
    val vagas: Int = 0,
    val encerrado: Boolean = false,
    val criadorEmail: String = ""
)
