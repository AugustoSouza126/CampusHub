package com.example.campushub.ui

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.EventCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para a construção do Card de Eventos [EventCard].
 * Testa a geração visual de cards para eventos abertos e encerrados.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EventCardTest {

    private lateinit var context: Context
    private lateinit var userRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).get()
        activity.setTheme(com.example.campushub.R.style.Theme_CampusHub)
        context = activity
        userRepository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())
    }

    /**
     * Testa a inclusão de um card de evento normal em um layout pai.
     */
    @Test
    fun testAddUpcomingEventCard() {
        val parentLayout = LinearLayout(context)
        val eventItem = Event(
            id = 1,
            titulo = "Workshop Kotlin",
            categoria = "Workshop",
            descricao = "Aprenda Kotlin do zero",
            horario = "14:00",
            local = "Lab 01",
            endereco = "Rua Teste, 100",
            vagas = 30
        )

        var isDetailsClicked = false
        EventCard.adicionarCardEvento(
            context = context,
            parent = parentLayout,
            evento = eventItem,
            userRepo = userRepository,
            onVerDetalhes = {
                isDetailsClicked = true
            }
        )

        assertEquals(1, parentLayout.childCount)
        assertNotNull(parentLayout.getChildAt(0))
        assertFalse(isDetailsClicked)
    }

    /**
     * Testa a geração de card para evento encerrado onde o usuário possui inscrição ativa e avaliação.
     */
    @Test
    fun testAddEndedAndSubscribedEventCard() {
        val parentLayout = LinearLayout(context)
        val eventId = 99
        userRepository.salvarInscricao(eventId)
        userRepository.salvarOuAtualizarAvaliacao(eventId, 5)

        val endedEvent = Event(
            id = eventId,
            titulo = "Evento Antigo",
            categoria = "Palestra",
            encerrado = true
        )

        EventCard.adicionarCardEvento(
            context = context,
            parent = parentLayout,
            evento = endedEvent,
            userRepo = userRepository,
            onVerDetalhes = {}
        )

        assertEquals(1, parentLayout.childCount)
        assertTrue(userRepository.isInscrito(eventId))
    }
}
