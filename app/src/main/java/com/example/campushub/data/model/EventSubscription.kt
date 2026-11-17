// Modelo de dados representando a inscrição e status de check-in do aluno no evento.
package com.example.campushub.data.model

data class EventSubscription(
    val id: String = "",
    val eventoId: Int = 0,
    val alunoNome: String = "",
    val alunoMatricula: String = "",
    val alunoEmail: String = "",
    val checkedIn: Boolean = false,
    val dataInscricao: String = ""
)
