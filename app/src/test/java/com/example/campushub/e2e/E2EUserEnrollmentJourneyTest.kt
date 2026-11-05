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

/**
 * Teste E2E 1: Jornada completa do Aluno (Cadastro, Busca, Inscrição e Favoritos).
 * Executa localmente testando a integração de ponta a ponta.
 */
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

    /**
     * Jornada completa do aluno:
     * 1. Cadastro do perfil do aluno.
     * 2. Busca e filtragem de eventos por título.
     * 3. Inscrição e adição do evento aos favoritos.
     * 4. Validação do evento nas abas de Inscrições e Favoritos.
     * 5. Cancelamento de inscrição e limpeza.
     */
    @Test
    fun testCompleteStudentOnboardingAndEnrollmentJourney() {
        // Passo 1: Cadastro do perfil do aluno
        val userName = "Mariana Oliveira"
        val userMatricula = "20262099"
        val userEmail = "mariana@universidade.edu.br"
        val userPass = "senha123"

        userRepo.salvarUsuario(userName, userMatricula, userEmail, userPass)

        assertEquals(userName, userRepo.getUsuarioNome())
        assertEquals(userMatricula, userRepo.getUsuarioMatricula())
        assertEquals(userEmail, userRepo.getUsuarioEmail())

        // Passo 2: Busca e filtragem por título de evento
        val allEvents = EventRepository.listaEventosIniciais
        assertFalse(allEvents.isEmpty())

        val searchedEvents = allEvents.filter { it.titulo.contains("Workshop", ignoreCase = true) }
        assertFalse(searchedEvents.isEmpty())

        val selectedEvent = searchedEvents[0]
        val eventId = selectedEvent.id

        // Passo 3: Inscrição e Favoritismo
        assertFalse(userRepo.isInscrito(eventId))
        assertFalse(userRepo.isFavorito(eventId))

        userRepo.salvarInscricao(eventId)
        assertTrue(userRepo.isInscrito(eventId))

        userRepo.salvarFavorito(eventId)
        assertTrue(userRepo.isFavorito(eventId))

        // Passo 4: Validação nas listas de Inscrições e Favoritos
        val mySubscriptions = userRepo.getInscricoes()
        assertTrue(mySubscriptions.contains(eventId.toString()))

        val myFavorites = userRepo.getFavoritos()
        assertTrue(myFavorites.contains(eventId.toString()))

        // Passo 5: Cancelamento de inscrição
        userRepo.removerInscricao(eventId)
        assertFalse(userRepo.isInscrito(eventId))
    }
}
