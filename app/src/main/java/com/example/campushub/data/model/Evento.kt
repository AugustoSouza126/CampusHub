package com.example.campushub.data.model

/**
 * Modelo de dados que representa um Evento Acadêmico no CampusHub.
 * Contém informações gerais, dados de inscrição e geolocalização (endereço, latitude e longitude).
 */
data class Evento(
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
