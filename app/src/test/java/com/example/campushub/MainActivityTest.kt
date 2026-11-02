package com.example.campushub

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para a atividade principal [MainActivity].
 * Valida a instanciação da Activity orquestradora da aplicação.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    @Before
    fun setUp() {
        // Configura ambiente do FirebaseApp para evitar erros de inicialização durante a criação de atividades
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            val firebaseOptions = FirebaseOptions.Builder()
                .setApplicationId("1:123456789012:android:abcdef1234567890")
                .setApiKey("AIzaSyFakeKeyForTestUnitExecution123")
                .setProjectId("campushub-test")
                .build()
            FirebaseApp.initializeApp(appContext, firebaseOptions)
        }
    }

    /**
     * Testa a instanciação do controlador e criação do objeto [MainActivity].
     */
    @Test
    fun testMainActivityCreation() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        activityController.get().setTheme(R.style.Theme_CampusHub)
        val activityInstance = activityController.get()
        assertNotNull(activityInstance)
    }
}
