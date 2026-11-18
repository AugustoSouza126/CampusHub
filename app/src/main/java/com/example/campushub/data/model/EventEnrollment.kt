package com.example.campushub.data.model

data class EventEnrollment(
    val email: String = "",
    val nome: String = "",
    val matricula: String = "",
    val checkedIn: Boolean = false,
    val dataInscricao: String = ""
)