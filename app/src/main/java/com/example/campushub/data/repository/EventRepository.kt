package com.example.campushub.data.repository

import com.example.campushub.data.model.Evento
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repositório responsável por gerenciar operações CRUD de Eventos no Firebase Firestore.
 */
object EventRepository {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private const val COLLECTION_NAME = "eventos"

    /**
     * Lista inicial de eventos pré-cadastrados no campus com dados de localização (Endereço, Latitude e Longitude).
     */
    val listaEventosIniciais = listOf(
        Evento(
            id = 0,
            titulo = "Seminário de Boas-Vindas 2026",
            categoria = "Acadêmico",
            descricao = "Encontro de integração dos novos alunos para apresentação do campus e corpo docente.",
            data = "01/10/2026",
            dataBadge = "01 OUT",
            horario = "10:00",
            local = "Auditório Central",
            endereco = "Av. Professor Luciano Gualberto, 380 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5583,
            longitude = -46.7314,
            vagas = 200,
            encerrado = true
        ),
        Evento(
            id = 1,
            titulo = "Semana Acadêmica 2026",
            categoria = "Acadêmico",
            descricao = "Semana acadêmica com palestras, workshops e atividades para os alunos de todos os cursos.",
            data = "15/10/2026",
            dataBadge = "15 OUT",
            horario = "19:00",
            local = "Auditório Principal",
            endereco = "Rua do Matão, 1010 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5562,
            longitude = -46.7328,
            vagas = 100
        ),
        Evento(
            id = 2,
            titulo = "Workshop de Programação Mobile",
            categoria = "Workshop",
            descricao = "Workshop prático sobre desenvolvimento de software com Android e Kotlin.",
            data = "20/10/2026",
            dataBadge = "20 OUT",
            horario = "14:00",
            local = "Laboratório 02",
            endereco = "Av. Lineu Prestes, 580 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5621,
            longitude = -46.7305,
            vagas = 40
        ),
        Evento(
            id = 3,
            titulo = "Hackathon Universitário",
            categoria = "Hackathon",
            descricao = "Competição intensa de programação e inovação com prêmios para os melhores projetos.",
            data = "25/10/2026",
            dataBadge = "25 OUT",
            horario = "08:00",
            local = "Laboratório de Informática",
            endereco = "Av. Professor Mello Moraes, 1721 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5595,
            longitude = -46.7289,
            vagas = 60
        ),
        Evento(
            id = 4,
            titulo = "Palestra: Inteligência Artificial",
            categoria = "Palestra",
            descricao = "Discussão sobre inteligência artificial, novos modelos de IA e o mercado de trabalho.",
            data = "02/11/2026",
            dataBadge = "02 NOV",
            horario = "19:30",
            local = "Auditório Central",
            endereco = "Av. Professor Luciano Gualberto, 380 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5583,
            longitude = -46.7314,
            vagas = 150
        ),
        Evento(
            id = 5,
            titulo = "Feira de Profissões e Carreiras",
            categoria = "Carreiras",
            descricao = "Oportunidades de estágio, estandes de empresas parceiras e networking.",
            data = "10/11/2026",
            dataBadge = "10 NOV",
            horario = "09:00",
            local = "Ginásio Universitário",
            endereco = "Praça do Relógio, 109 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5608,
            longitude = -46.7265,
            vagas = 300
        ),
        Evento(
            id = 6,
            titulo = "Workshop de Cibersegurança",
            categoria = "Segurança",
            descricao = "Introdução à segurança da informação, análise de vulnerabilidades e proteção de redes.",
            data = "18/11/2026",
            dataBadge = "18 NOV",
            horario = "18:30",
            local = "Laboratório 03",
            endereco = "Av. Lineu Prestes, 580 - Cidade Universitária, São Paulo - SP",
            latitude = -23.5621,
            longitude = -46.7305,
            vagas = 50
        )
    )

    fun getEventos(
        onSuccess: (List<Evento>) -> Unit,
        onError: (String) -> Unit
    ) {
        val collRef: CollectionReference = db.collection(COLLECTION_NAME)
        collRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    seearEventosIniciais {
                        getEventos(onSuccess, onError)
                    }
                } else {
                    val lista = mutableListOf<Evento>()
                    for (doc in querySnapshot.documents) {
                        val evento = doc.toObject(Evento::class.java)
                        if (evento != null) {
                            lista.add(evento)
                        }
                    }
                    lista.sortBy { it.id }
                    onSuccess(lista)
                }
            }
            .addOnFailureListener { exception ->
                val msg = exception.localizedMessage ?: "Erro ao buscar eventos no Firestore."
                onError(msg)
            }
    }

    fun adicionarEvento(
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = db.collection(COLLECTION_NAME).document(evento.id.toString())
        docRef.set(evento)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val msg = exception.localizedMessage ?: "Erro ao salvar evento no Firestore."
                onError(msg)
            }
    }

    fun atualizarEvento(
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = db.collection(COLLECTION_NAME).document(evento.id.toString())
        docRef.set(evento)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val msg = exception.localizedMessage ?: "Erro ao atualizar evento no Firestore."
                onError(msg)
            }
    }

    fun deletarEvento(
        eventoId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = db.collection(COLLECTION_NAME).document(eventoId.toString())
        docRef.delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val msg = exception.localizedMessage ?: "Erro ao excluir evento no Firestore."
                onError(msg)
            }
    }

    fun encerrarEvento(
        eventoId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = db.collection(COLLECTION_NAME).document(eventoId.toString())
        docRef.update("encerrado", true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val msg = exception.localizedMessage ?: "Erro ao encerrar evento no Firestore."
                onError(msg)
            }
    }

    private fun seearEventosIniciais(onComplete: () -> Unit) {
        val batch = db.batch()
        for (e in listaEventosIniciais) {
            val docRef = db.collection(COLLECTION_NAME).document(e.id.toString())
            batch.set(docRef, e)
        }
        batch.commit().addOnCompleteListener {
            onComplete()
        }
    }
}
