package com.example.campushub.screens

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.screens.EventDetail
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [EventDetail].
 * Valida a tela de detalhes de evento para alunos e para o criador do evento.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EventDetailTest {

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
     * Testa a exibição dos detalhes de um evento comum para alunos.
     */
    @Test
    fun testBuildEventDetailsScreenForStudent() {
        val parentLayout = LinearLayout(context)
        val eventItem = Event(
            id = 1,
            titulo = "Semana Acadêmica",
            categoria = "Acadêmico",
            descricao = "Palestra principal",
            horario = "19:00",
            local = "Auditório"
        )

        EventDetail.construirTelaDetalhes(
            context = context,
            layout = parentLayout,
            evento = eventItem,
            userRepo = userRepository,
            onEditarEvento = {},
            onTirarFotoCamera = {},
            onEscolherFotoGaleria = {},
            onPedirPermissaoAudio = {},
            onGravarVideoCamera = {},
            onEscolherVideoGaleria = {},
            onAtualizarTela = {},
            onVoltar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }

    /**
     * Testa a renderização da tela de detalhes exibindo o painel exclusivo para o criador do evento.
     */
    @Test
    fun testBuildEventDetailsScreenForCreatorAndEndedEvent() {
        val parentLayout = LinearLayout(context)
        userRepository.salvarUsuario("Professor", "11111", "prof@campus.edu.br", "123456")

        val creatorEvent = Event(
            id = 2,
            titulo = "Seminário Passado",
            categoria = "Acadêmico",
            criadorEmail = "prof@campus.edu.br",
            encerrado = true
        )

        EventDetail.construirTelaDetalhes(
            context = context,
            layout = parentLayout,
            evento = creatorEvent,
            userRepo = userRepository,
            onEditarEvento = {},
            onTirarFotoCamera = {},
            onEscolherFotoGaleria = {},
            onPedirPermissaoAudio = {},
            onGravarVideoCamera = {},
            onEscolherVideoGaleria = {},
            onAtualizarTela = {},
            onVoltar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }
}
