package com.example.campushub.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.EventRepository
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
class E2EUserEnrollmentJourneyTest {

    private lateinit var context: Context
    private lateinit var userRepo: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userRepo = UserPreferencesRepository(context)
    }

    @Test
    fun testCompleteStudentOnboardingAndEnrollmentJourney() {
        val userName = "Mariana Oliveira"
        val userMatricula = "20262099"
        val userEmail = "mariana@universidade.edu.br"
        val userPass = "senha123"

        userRepo.salvarUsuario(userName, userMatricula, userEmail, userPass)

        assertEquals(userName, userRepo.getUsuarioNome())
        assertEquals(userMatricula, userRepo.getUsuarioMatricula())
        assertEquals(userEmail, userRepo.getUsuarioEmail())

        val allEvents = EventRepository.listaEventosIniciais
        assertFalse(allEvents.isEmpty())

        val searchedEvents = allEvents.filter { it.titulo.contains("Workshop", ignoreCase = true) }
        assertFalse(searchedEvents.isEmpty())

        val selectedEvent = searchedEvents[0]
        val eventId = selectedEvent.id

        assertFalse(userRepo.isInscrito(eventId))
        assertFalse(userRepo.isFavorito(eventId))

        userRepo.salvarInscricao(eventId)
        assertTrue(userRepo.isInscrito(eventId))

        userRepo.salvarFavorito(eventId)
        assertTrue(userRepo.isFavorito(eventId))

        val mySubscriptions = userRepo.getInscricoes()
        assertTrue(mySubscriptions.contains(eventId.toString()))

        val myFavorites = userRepo.getFavoritos()
        assertTrue(myFavorites.contains(eventId.toString()))

        userRepo.removerInscricao(eventId)
        assertFalse(userRepo.isInscrito(eventId))
    }
}
