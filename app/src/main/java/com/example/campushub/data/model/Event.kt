package com.example.campushub.data.model

/**
 * Data model representing an Academic Event in CampusHub.
 */
data class Event(
    val id: Int = 0,
    val titulo: String = "",
    val categoria: String = "",
    val descricao: String = "",
    val data: String = "",
    val dataBadge: String = "",
    val horario: String = "",
    val local: String = "",
    val endereco: String = "",
    val latitude: Double = -23.55052,
    val longitude: Double = -46.633308,
    val vagas: Int = 0,
    val encerrado: Boolean = false,
    val criadorEmail: String = ""
)
