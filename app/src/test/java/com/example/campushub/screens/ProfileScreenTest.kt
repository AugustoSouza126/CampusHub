package com.example.campushub.screens

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.screens.ProfileScreen
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [ProfileScreen].
 * Valida a exibição dos dados de perfil acadêmico e formulário de edição.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileScreenTest {

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
     * Testa a construção da tela de visualização do perfil acadêmico do usuário.
     */
    @Test
    fun testBuildUserProfileScreen() {
        val parentLayout = LinearLayout(context)
        userRepository.salvarUsuario("Carlos Silva", "2026100", "carlos@campus.edu.br", "123456")

        ProfileScreen.construirTelaPerfil(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            onEditarPerfil = {},
            onVerEventosCriados = {},
            onDeslogar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }

    /**
     * Testa a construção do formulário de edição de informações de perfil.
     */
    @Test
    fun testBuildEditProfileScreen() {
        val parentLayout = LinearLayout(context)

        ProfileScreen.construirTelaEditarPerfil(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            onPerfilAtualizado = {},
            onCancelar = {}
        )

        assertNotNull(parentLayout)
        assertTrue(parentLayout.childCount > 0)
    }
}
