package com.example.campushub.ui

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.EventMediaComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [EventMediaComponent].
 * Valida as seções de publicação e exibição de fotos, vídeos e áudios do evento.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EventMediaComponentTest {

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
     * Testa a geração da seção de fotos para usuário não inscrito (bloqueio de envio).
     */
    @Test
    fun testBuildPhotosSectionNonSubscribedUser() {
        val parentLayout = LinearLayout(context)
        val targetEvent = Event(id = 1, titulo = "Evento Teste")

        EventMediaComponent.construirSecaoFotos(
            context = context,
            layout = parentLayout,
            evento = targetEvent,
            userRepo = userRepository,
            onTirarFotoCamera = {},
            onEscolherFotoGaleria = {},
            onAtualizarTela = {}
        )

        assertEquals(1, parentLayout.childCount)
        assertNotNull(parentLayout.getChildAt(0))
    }

    /**
     * Testa a geração da seção de fotos para usuário inscrito com foto cadastrada.
     */
    @Test
    fun testBuildPhotosSectionSubscribedUser() {
        val parentLayout = LinearLayout(context)
        val eventId = 2
        userRepository.salvarInscricao(eventId)
        userRepository.adicionarFoto(eventId, "/path/fake/foto.jpg")

        val targetEvent = Event(id = eventId, titulo = "Evento Inscrito")

        EventMediaComponent.construirSecaoFotos(
            context = context,
            layout = parentLayout,
            evento = targetEvent,
            userRepo = userRepository,
            onTirarFotoCamera = {},
            onEscolherFotoGaleria = {},
            onAtualizarTela = {}
        )

        assertEquals(1, parentLayout.childCount)
    }

    /**
     * Testa a construção da seção de vídeos para usuário não inscrito e posteriormente inscrito.
     */
    @Test
    fun testBuildVideosSectionForSubscribedAndNonSubscribed() {
        val parentLayout = LinearLayout(context)
        val eventId = 3
        val targetEvent = Event(id = eventId, titulo = "Evento Videos")

        EventMediaComponent.construirSecaoVideos(
            context = context,
            layout = parentLayout,
            evento = targetEvent,
            userRepo = userRepository,
            onGravarVideoCamera = {},
            onEscolherVideoGaleria = {},
            onAtualizarTela = {}
        )

        assertEquals(1, parentLayout.childCount)

        userRepository.salvarInscricao(eventId)
        EventMediaComponent.construirSecaoVideos(
            context = context,
            layout = parentLayout,
            evento = targetEvent,
            userRepo = userRepository,
            onGravarVideoCamera = {},
            onEscolherVideoGaleria = {},
            onAtualizarTela = {}
        )

        assertEquals(2, parentLayout.childCount)
    }

    /**
     * Testa a construção da seção de gravação/reprodução de áudio.
     */
    @Test
    fun testBuildAudiosSection() {
        val parentLayout = LinearLayout(context)
        val eventId = 4
        val targetEvent = Event(id = eventId, titulo = "Evento Audios")

        EventMediaComponent.construirSecaoAudios(
            context = context,
            layout = parentLayout,
            evento = targetEvent,
            userRepo = userRepository,
            onPedirPermissaoAudio = {},
            onAtualizarTela = {}
        )

        assertEquals(1, parentLayout.childCount)
    }
}
