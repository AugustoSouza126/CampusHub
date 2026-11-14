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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AuthRepositoryTest {

    @Before
    fun setUp() {
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

    @Test
    fun testAuthRepositoryObjectNotNull() {
        assertNotNull(AuthRepository)
    }

    @Test
    fun testAuthRepositoryInitialState() {
        val currentUser = AuthRepository.getUsuarioAtual()
        val isLoggedIn = AuthRepository.isUsuarioLogado()
        assertTrue(currentUser == null || !isLoggedIn)

        AuthRepository.deslogar()
    }
}
