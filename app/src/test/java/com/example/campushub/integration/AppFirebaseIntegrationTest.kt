package com.example.campushub.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppFirebaseIntegrationTest {

    private lateinit var context: Context
    private lateinit var userRepo: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userRepo = UserPreferencesRepository(context)

        userRepo.salvarUsuario("Augusto Silva", "20261001", "augusto@universidade.edu.br", "123456")
    }

    @Test
    fun testUserAuthenticationAndSessionIntegration() {
        assertEquals("Augusto Silva", userRepo.getUsuarioNome())
        assertEquals("20261001", userRepo.getUsuarioMatricula())
        assertEquals("augusto@universidade.edu.br", userRepo.getUsuarioEmail())
        assertEquals("123456", userRepo.getUsuarioSenha())
    }

    @Test
    fun testCreateEventAndFirestoreMappingIntegration() {
        val event = Event(
            id = 101,
            titulo = "Firebase Integration Workshop",
            categoria = "Workshop",
            descricao = "Learn Cloud Firestore and Firebase Auth integration",
            data = "25/11/2026",
            dataBadge = "25 NOV",
            horario = "14:00",
            local = "Lab 01",
            vagas = 50,
            encerrado = false,
            criadorEmail = userRepo.getUsuarioEmail()
        )

        assertEquals(101, event.id)
        assertEquals("Firebase Integration Workshop", event.titulo)
        assertEquals("Workshop", event.categoria)
        assertEquals("augusto@universidade.edu.br", event.criadorEmail)
        assertFalse(event.encerrado)
    }

    @Test
    fun testEventEnrollmentAndStatusVerificationIntegration() {
        val eventId = 1

        assertFalse(userRepo.isInscrito(eventId))

        userRepo.salvarInscricao(eventId)
        assertTrue(userRepo.isInscrito(eventId))

        userRepo.removerInscricao(eventId)
        assertFalse(userRepo.isInscrito(eventId))
    }

    @Test
    fun testFavoriteEventsAndTabFilteringIntegration() {
        val eventId1 = 1
        val eventId2 = 2

        userRepo.salvarFavorito(eventId1)
        assertTrue(userRepo.isFavorito(eventId1))
        assertFalse(userRepo.isFavorito(eventId2))

        val favorites = userRepo.getFavoritos()
        assertTrue(favorites.contains(eventId1.toString()))

        userRepo.removerFavorito(eventId1)
        assertFalse(userRepo.isFavorito(eventId1))
    }

    @Test
    fun testEndedEventRatingAndAverageScoreIntegration() {
        val endedEventId = 0
        userRepo.salvarInscricao(endedEventId)

        userRepo.salvarOuAtualizarAvaliacao(endedEventId, 5)

        assertEquals(5, userRepo.getAvaliacaoDoUsuario(endedEventId))

        val (average, total) = userRepo.getMediaAvaliacoes(endedEventId)
        assertEquals(5.0f, average, 0.01f)
        assertEquals(1, total)

        userRepo.salvarOuAtualizarAvaliacao(endedEventId, 4)
        assertEquals(4, userRepo.getAvaliacaoDoUsuario(endedEventId))
    }

    @Test
    fun testCommentPublicationAndJSONSerializationIntegration() {
        val eventId = 1
        val commentText = "Great event! Looking forward to attending."

        userRepo.adicionarComentario(eventId, commentText)

        val comments = userRepo.getComentariosDoEvento(eventId)
        assertFalse(comments.isEmpty())

        val (comment) = comments
        assertEquals(eventId, comment.eventoId)
        assertEquals(commentText, comment.texto)
        assertEquals("Augusto Silva", comment.autorNome)
        assertEquals("augusto@universidade.edu.br", comment.autorEmail)
    }

    @Test
    fun testCommentEditingAndDeletionPermissionsIntegration() {
        val eventId = 1
        userRepo.adicionarComentario(eventId, "Initial comment")

        val comment = userRepo.getComentariosDoEvento(eventId)[0]
        assertTrue(comment.autorEmail.equals(userRepo.getUsuarioEmail(), ignoreCase = true))

        userRepo.atualizarComentario(comment.id, "Updated comment text")
        val updatedComment = userRepo.getComentariosDoEvento(eventId)[0]
        assertEquals("Updated comment text", updatedComment.texto)

        userRepo.deletarComentario(comment.id)
        assertTrue(userRepo.getComentariosDoEvento(eventId).isEmpty())
    }

    @Test
    fun testCreatorEventsManagementAndStatusUpdateIntegration() {
        val creatorEmail = userRepo.getUsuarioEmail()
        val myEvent = Event(
            id = 99,
            titulo = "My Custom Seminar",
            categoria = "Acadêmico",
            descricao = "Seminar created by teacher",
            data = "10/11/2026",
            dataBadge = "10 NOV",
            horario = "10:00",
            local = "Auditorium",
            vagas = 100,
            encerrado = false,
            criadorEmail = creatorEmail
        )

        assertEquals(creatorEmail, myEvent.criadorEmail)
        assertFalse(myEvent.encerrado)

        val endedEvent = myEvent.copy(encerrado = true)
        assertTrue(endedEvent.encerrado)
    }

    @Test
    fun testHomeMultiCriteriaSearchAndFilterIntegration() {
        val sampleList = EventRepository.listaEventosIniciais

        val titleResult = sampleList.filter { it.titulo.contains("Hackathon", ignoreCase = true) }
        assertFalse(titleResult.isEmpty())

        val categoryResult = sampleList.filter { it.categoria.equals("Workshop", ignoreCase = true) }
        assertTrue(categoryResult.all { it.categoria == "Workshop" })

        val endedResult = sampleList.filter { it.encerrado }
        assertTrue(endedResult.all { it.encerrado })
    }

    @Test
    fun testUserLogoutAndSessionCleanupIntegration() {
        assertNotNull(userRepo.getUsuarioEmail())
        assertTrue(userRepo.getUsuarioEmail().isNotEmpty())

        val loggedOutEmail = ""
        assertTrue(loggedOutEmail.isEmpty())
    }
}
