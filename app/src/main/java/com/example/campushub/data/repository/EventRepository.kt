package com.example.campushub.data.repository

import com.example.campushub.data.model.Evento

object EventRepository {

    val listaEventos = listOf(
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

    fun getEventoPorId(id: Int): Evento? {
        return listaEventos.find { it.id == id }
    }
}
