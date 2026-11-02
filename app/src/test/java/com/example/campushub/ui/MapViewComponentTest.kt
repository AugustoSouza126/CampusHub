package com.example.campushub.ui

import android.content.Context
import com.example.campushub.ui.components.MapViewComponent
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [MapViewComponent].
 * Valida a instanciação do container visual do mapa interativo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MapViewComponentTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).get()
        activity.setTheme(com.example.campushub.R.style.Theme_CampusHub)
        context = activity
    }

    /**
     * Testa a criação do container de mapa com coordenadas geográficas especificadas.
     */
    @Test
    fun testCreateLocationMapViewContainer() {
        val mapViewContainer = MapViewComponent.criarMapaLocalizacao(
            context = context,
            latitude = -23.5583,
            longitude = -46.7314,
            titulo = "Auditório Central",
            subtitulo = "Campus USP"
        )
        assertNotNull(mapViewContainer)
    }
}
