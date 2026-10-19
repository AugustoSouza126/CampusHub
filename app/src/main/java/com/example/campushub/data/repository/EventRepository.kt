package com.example.campushub.data.repository

import com.example.campushub.data.model.Evento
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object EventRepository {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private const val COLLECTION_NAME = "eventos"

    val listaEventosIniciais = listOf(
        Evento(
            0,
            "Seminário de Boas-Vindas 2026",
            "Acadêmico",
            "Encontro de integração dos novos alunos para apresentação do campus e corpo docente.",
            "01/10/2026",
            "01 OUT",
            "10:00",
            "Auditório Central",
            200,
            encerrado = true
        ),
        Evento(
            1,
            "Semana Acadêmica 2026",
            "Acadêmico",
            "Semana acadêmica com palestras, workshops e atividades para os alunos de todos os cursos.",
            "15/10/2026",
            "15 OUT",
            "19:00",
            "Auditório Principal",
            100
        ),
        Evento(
            2,
            "Workshop de Programação Mobile",
            "Workshop",
            "Workshop prático sobre desenvolvimento de software com Android e Kotlin.",
            "20/10/2026",
            "20 OUT",
            "14:00",
            "Laboratório 02",
            40
        ),
        Evento(
            3,
            "Hackathon Universitário",
            "Hackathon",
            "Competição intensa de programação e inovação com prêmios para os melhores projetos.",
            "25/10/2026",
            "25 OUT",
            "08:00",
            "Laboratório de Informática",
            60
        ),
        Evento(
            4,
            "Palestra: Inteligência Artificial",
            "Palestra",
            "Discussão sobre inteligência artificial, novos modelos de IA e o mercado de trabalho.",
            "02/11/2026",
            "02 NOV",
            "19:30",
            "Auditório Central",
            150
        ),
        Evento(
            5,
            "Feira de Profissões e Carreiras",
            "Carreiras",
            "Oportunidades de estágio, estandes de empresas parceiras e networking.",
            "10/11/2026",
            "10 NOV",
            "09:00",
            "Ginásio Universitário",
            300
        ),
        Evento(
            6,
            "Workshop de Cibersegurança",
            "Segurança",
            "Introdução à segurança da informação, análise de vulnerabilidades e proteção de redes.",
            "18/11/2026",
            "18 NOV",
            "18:30",
            "Laboratório 03",
            50
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
