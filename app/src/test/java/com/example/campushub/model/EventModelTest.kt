package com.example.campushub.model

import com.example.campushub.data.model.Event
import com.example.campushub.data.model.EventAudio
import com.example.campushub.data.model.EventComment
import com.example.campushub.data.model.EventPhoto
import com.example.campushub.data.model.EventRating
import com.example.campushub.data.model.EventVideo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para verificação dos modelos de dados (Data Classes) da aplicação.
 */
class EventModelTest {

    @Test
    fun testEventDefaultValuesAndCopy() {
        val event = Event()
        assertEquals(0, event.id)
        assertEquals("", event.titulo)
        assertEquals("", event.categoria)
        assertEquals("", event.descricao)
        assertEquals("", event.data)
        assertEquals("", event.dataBadge)
        assertEquals("", event.horario)
        assertEquals("", event.local)
        assertEquals("", event.endereco)
        assertEquals(-23.55052, event.latitude, 0.0001)
        assertEquals(-46.633308, event.longitude, 0.0001)
        assertEquals(0, event.vagas)
        assertFalse(event.encerrado)
        assertEquals("", event.criadorEmail)

        val customEvent = event.copy(
            id = 10,
            titulo = "Android Workshop",
            categoria = "Workshop",
            vagas = 50,
            encerrado = true
        )
        assertEquals(10, customEvent.id)
        assertEquals("Android Workshop", customEvent.titulo)
        assertEquals("Workshop", customEvent.categoria)
        assertEquals(50, customEvent.vagas)
        assertTrue(customEvent.encerrado)
    }

    @Test
    fun testEventAudioModel() {
        val audio = EventAudio(
            eventoId = 1,
            caminhoArquivo = "/storage/audio1.m4a",
            duracaoTexto = "00:30",
            autorNome = "Maria Silva",
            autorEmail = "maria@campus.edu.br",
            dataPublicacao = "15/10/2026 14:00"
        )
        assertNotNull(audio.id)
        assertEquals(1, audio.eventoId)
        assertEquals("/storage/audio1.m4a", audio.caminhoArquivo)
        assertEquals("00:30", audio.duracaoTexto)
        assertEquals("Maria Silva", audio.autorNome)
        assertEquals("maria@campus.edu.br", audio.autorEmail)
        assertEquals("15/10/2026 14:00", audio.dataPublicacao)
    }

    @Test
    fun testEventCommentModel() {
        val comment = EventComment(
            eventoId = 2,
            autorNome = "João Pedro",
            autorEmail = "joao@campus.edu.br",
            texto = "Excelente evento!",
            dataPublicacao = "20/10/2026 16:30"
        )
        assertNotNull(comment.id)
        assertEquals(2, comment.eventoId)
        assertEquals("João Pedro", comment.autorNome)
        assertEquals("joao@campus.edu.br", comment.autorEmail)
        assertEquals("Excelente evento!", comment.texto)
        assertEquals("20/10/2026 16:30", comment.dataPublicacao)
    }

    @Test
    fun testEventPhotoModel() {
        val photo = EventPhoto(
            eventoId = 3,
            caminhoArquivo = "/storage/photo1.jpg",
            autorNome = "Ana Souza",
            autorEmail = "ana@campus.edu.br",
            dataPublicacao = "25/10/2026 10:15"
        )
        assertNotNull(photo.id)
        assertEquals(3, photo.eventoId)
        assertEquals("/storage/photo1.jpg", photo.caminhoArquivo)
        assertEquals("Ana Souza", photo.autorNome)
        assertEquals("ana@campus.edu.br", photo.autorEmail)
        assertEquals("25/10/2026 10:15", photo.dataPublicacao)
    }

    @Test
    fun testEventRatingModel() {
        val rating = EventRating(
            eventoId = 4,
            autorEmail = "aluno@campus.edu.br",
            nota = 5
        )
        assertEquals(4, rating.eventoId)
        assertEquals("aluno@campus.edu.br", rating.autorEmail)
        assertEquals(5, rating.nota)
    }

    @Test
    fun testEventVideoModel() {
        val video = EventVideo(
            eventoId = 5,
            caminhoArquivo = "/storage/video1.mp4",
            autorNome = "Carlos Andrade",
            autorEmail = "carlos@campus.edu.br",
            dataPublicacao = "30/10/2026 18:00"
        )
        assertNotNull(video.id)
        assertEquals(5, video.eventoId)
        assertEquals("/storage/video1.mp4", video.caminhoArquivo)
        assertEquals("Carlos Andrade", video.autorNome)
        assertEquals("carlos@campus.edu.br", video.autorEmail)
        assertEquals("30/10/2026 18:00", video.dataPublicacao)
    }

    @Test
    fun testDataClassesEqualityAndToString() {
        val rating1 = EventRating(eventoId = 1, autorEmail = "a@a.com", nota = 5)
        val rating2 = EventRating(eventoId = 1, autorEmail = "a@a.com", nota = 5)
        assertEquals(rating1, rating2)
        assertEquals(rating1.hashCode(), rating2.hashCode())
        assertTrue(rating1.toString().contains("EventRating"))

        val event1 = Event(id = 1, titulo = "Evento 1")
        val event2 = Event(id = 1, titulo = "Evento 1")
        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
        assertTrue(event1.toString().contains("Event"))
    }
}
