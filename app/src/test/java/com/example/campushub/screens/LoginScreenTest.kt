package com.example.campushub.screens

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.screens.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [LoginScreen].
 * Valida a construção das telas de Login, Cadastro e Recuperação de Senha.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginScreenTest {

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
     * Testa a construção visual da tela de login e vinculação de botões de atalho.
     */
    @Test
    fun testBuildLoginScreen() {
        val parentLayout = LinearLayout(context)
        var isLoginSuccess = false
        var isNavigateToSignUp = false
        var isNavigateToPasswordRecovery = false

        LoginScreen.construirTelaLogin(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            onLoginSucesso = { isLoginSuccess = true },
            onIrParaCadastro = { isNavigateToSignUp = true },
            onIrParaRecuperacao = { isNavigateToPasswordRecovery = true }
        )

        assertTrue(parentLayout.childCount > 0)
        assertEquals(false, isLoginSuccess)
        assertEquals(false, isNavigateToSignUp)
        assertEquals(false, isNavigateToPasswordRecovery)
    }

    /**
     * Testa a construção do formulário de cadastro de novo usuário.
     */
    @Test
    fun testBuildSignUpScreen() {
        val parentLayout = LinearLayout(context)
        var isSignUpSuccess = false
        var isBackClicked = false

        LoginScreen.construirTelaCadastro(
            context = context,
            layout = parentLayout,
            userRepo = userRepository,
            onCadastroSucesso = { isSignUpSuccess = true },
            onVoltar = { isBackClicked = true }
        )

        assertTrue(parentLayout.childCount > 0)
        assertEquals(false, isSignUpSuccess)
        assertEquals(false, isBackClicked)
    }

    /**
     * Testa a construção da tela de recuperação de senha por e-mail.
     */
    @Test
    fun testBuildPasswordRecoveryScreen() {
        val parentLayout = LinearLayout(context)
        var isBackClicked = false

        LoginScreen.construirTelaRecuperacao(
            context = context,
            layout = parentLayout,
            onVoltar = { isBackClicked = true }
        )

        assertTrue(parentLayout.childCount > 0)
        assertEquals(false, isBackClicked)
    }
}
