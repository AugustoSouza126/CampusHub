package com.example.campushub.ui

import android.content.Context
import android.graphics.Color
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import org.junit.Assert.assertEquals
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
class UIComponentsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).get()
        activity.setTheme(com.example.campushub.R.style.Theme_CampusHub)
        context = activity
    }

    @Test
    fun testDpToPxConversion() {
        val convertedPx = UIComponents.dpToPx(context, 16)
        assertTrue(convertedPx >= 16)
    }

    @Test
    fun testCreateShape() {
        val createdShape = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 12f)
        assertNotNull(createdShape)
    }

    @Test
    fun testCreateLabel() {
        val labelView = UIComponents.criarRotulo(context, "E-mail Institucional")
        assertNotNull(labelView)
        assertEquals("E-mail Institucional", labelView.text.toString())
    }

    @Test
    fun testCreateInputField() {
        val inputEditText = UIComponents.criarCampoTexto(context, "Digite seu nome", valorInicial = "João")
        assertNotNull(inputEditText)
        assertEquals("João", inputEditText.text.toString())
        assertEquals("Digite seu nome", inputEditText.hint.toString())
    }

    @Test
    fun testCreatePrimaryButton() {
        var isButtonClicked = false
        val primaryButton = UIComponents.criarBotao(context, "ENTRAR", primario = true) {
            isButtonClicked = true
        }
        assertNotNull(primaryButton)
        assertEquals("ENTRAR", primaryButton.text.toString())

        primaryButton.performClick()
        assertTrue(isButtonClicked)
    }

    @Test
    fun testCreateDangerButton() {
        var isButtonClicked = false
        val dangerButton = UIComponents.criarBotao(context, "EXCLUIR", primario = false, perigo = true) {
            isButtonClicked = true
        }
        assertNotNull(dangerButton)
        assertEquals("EXCLUIR", dangerButton.text.toString())

        dangerButton.performClick()
        assertTrue(isButtonClicked)
    }

    @Test
    fun testCreateHeaderBarWithAndWithoutBackButton() {
        var isBackButtonClicked = false
        val headerWithBack = UIComponents.criarHeaderBar(context, "Título", "Subtítulo") {
            isBackButtonClicked = true
        }
        assertNotNull(headerWithBack)

        val headerWithoutBack = UIComponents.criarHeaderBar(context, "Título")
        assertNotNull(headerWithoutBack)
        assertEquals(false, isBackButtonClicked)
    }

    @Test
    fun testCreateBottomNavigation() {
        var selectedTab = ""
        val bottomNavView = UIComponents.criarBottomNav(context, "HOME") { tab ->
            selectedTab = tab
        }
        assertNotNull(bottomNavView)
        assertEquals("", selectedTab)
    }

    @Test
    fun testCreateBaseScreenLayout() {
        var navigatedTab = ""
        val (rootLayout, bodyLayout) = UIComponents.criarTelaBase(
            context = context,
            exibirBottomNav = true,
            abaAtiva = "HOME",
            restoreScrollY = 10
        ) { tab ->
            navigatedTab = tab
        }

        assertNotNull(rootLayout)
        assertNotNull(bodyLayout)
        assertEquals("", navigatedTab)
    }
}
