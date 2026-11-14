package com.example.campushub.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class E2EEventManagementJourneyTest {

    private lateinit var context: Context
    private lateinit var userRepo: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userRepo = UserPreferencesRepository(context)

        userRepo.salvarUsuario("Prof. Carlos Mendes", "1001", "carlos@universidade.edu.br", "senha123")
    }

    @Test
    fun testCompleteCreatorLifecycleAndEngagementJourney() {
        val creatorEmail = userRepo.getUsuarioEmail()
        assertEquals("carlos@universidade.edu.br", creatorEmail)

        val newEvent = Event(
            id = 505,
            titulo = "Inteligência Artificial na Prática",
            categoria = "Palestra",
            descricao = "Palestra sobre aplicação de IA e aprendizado de máquina",
            data = "12/12/2026",
            dataBadge = "12 DEZ",
            horario = "19:00",
            local = "Auditório Central",
            vagas = 120,
            encerrado = false,
            criadorEmail = creatorEmail
        )

        assertEquals(505, newEvent.id)
        assertEquals(creatorEmail, newEvent.criadorEmail)
        assertFalse(newEvent.encerrado)

        val commentText = "Excelente evento! Inscrições abertas para todos."
        userRepo.adicionarComentario(newEvent.id, commentText)

        val comments = userRepo.getComentariosDoEvento(newEvent.id)
        assertFalse(comments.isEmpty())

        val (firstComment) = comments
        assertEquals(commentText, firstComment.texto)
        assertEquals(creatorEmail, firstComment.autorEmail)

        userRepo.atualizarComentario(firstComment.id, "Texto do comentário atualizado")
        val updatedComments = userRepo.getComentariosDoEvento(newEvent.id)
        assertEquals("Texto do comentário atualizado", updatedComments[0].texto)

        userRepo.deletarComentario(firstComment.id)
        assertTrue(userRepo.getComentariosDoEvento(newEvent.id).isEmpty())

        val endedEvent = newEvent.copy(encerrado = true)
        assertTrue(endedEvent.encerrado)

        userRepo.salvarInscricao(endedEvent.id)
        userRepo.salvarOuAtualizarAvaliacao(endedEvent.id, 5)

        val (avg, count) = userRepo.getMediaAvaliacoes(endedEvent.id)
        assertEquals(5.0f, avg, 0.01f)
        assertEquals(1, count)
    }
}
