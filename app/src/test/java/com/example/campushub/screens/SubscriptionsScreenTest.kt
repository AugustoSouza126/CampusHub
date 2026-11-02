package com.example.campushub.screens

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.screens.SubscriptionsScreen
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [SubscriptionsScreen].
 * Valida a tela de acompanhamento das inscrições do aluno.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SubscriptionsScreenTest {

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
     * Testa a construção da tela de eventos inscritos do usuário.
     */
    @Test
    fun testBuildSubscribedEventsScreen() {
        val parentLayout = LinearLayout(context)

        SubscriptionsScreen.construirTelaMeusEventos(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            onVerDetalhes = {},
            onExplorarEventos = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }
}
