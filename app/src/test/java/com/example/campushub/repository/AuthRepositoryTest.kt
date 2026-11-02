package com.example.campushub.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para a classe de gerenciamento de autenticação [AuthRepository].
 * Gerencia serviços de autenticação do Firebase.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AuthRepositoryTest {

    @Before
    fun setUp() {
        // Inicializa o FirebaseApp com credenciais fictícias de teste se ainda não estiver configurado
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            val firebaseOptions = FirebaseOptions.Builder()
                .setApplicationId("1:123456789012:android:abcdef1234567890")
                .setApiKey("AIzaSyFakeKeyForTestUnitExecution123")
                .setProjectId("campushub-test")
                .build()
            FirebaseApp.initializeApp(context, firebaseOptions)
        }
    }

    /**
     * Valida que o objeto singleton do AuthRepository não é nulo.
     */
    @Test
    fun testAuthRepositoryObjectNotNull() {
        assertNotNull(AuthRepository)
    }

    /**
     * Verifica o estado inicial de usuário autenticado em ambiente isolado de teste.
     */
    @Test
    fun testAuthRepositoryInitialState() {
        val currentUser = AuthRepository.getUsuarioAtual()
        val isLoggedIn = AuthRepository.isUsuarioLogado()
        assertTrue(currentUser == null || !isLoggedIn)

        // Garantir que efetuar logout em estado deslogado não gera erros
        AuthRepository.deslogar()
    }
}
