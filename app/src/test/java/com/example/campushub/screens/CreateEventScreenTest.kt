package com.example.campushub.screens

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.screens.CreateEventScreen
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CreateEventScreenTest {

    private lateinit var context: Context
    private lateinit var userRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).get()
        activity.setTheme(com.example.campushub.R.style.Theme_CampusHub)
        context = activity
        userRepository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testBuildCreateNewEventScreen() {
        val parentLayout = LinearLayout(context)

        CreateEventScreen.construirTelaCriarEvento(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            eventoParaEditar = null,
            onSucesso = {},
            onVoltar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }

    @Test
    fun testBuildEditExistingEventScreen() {
        val parentLayout = LinearLayout(context)
        val existingEvent = Event(
            id = 10,
            titulo = "Evento para Editar",
            categoria = "Workshop",
            descricao = "Descrição atual",
            data = "10/10/2026",
            dataBadge = "10 OUT",
            horario = "15:00",
            local = "Lab 05",
            vagas = 30
        )

        CreateEventScreen.construirTelaCriarEvento(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            eventoParaEditar = existingEvent,
            onSucesso = {},
            onVoltar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }
}
