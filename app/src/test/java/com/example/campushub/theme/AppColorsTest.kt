package com.example.campushub.theme

import android.graphics.Color
import com.example.campushub.ui.theme.AppColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para a paleta de cores oficial [AppColors].
 * Valida a exatidão dos códigos hexadecimais configurados no tema.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppColorsTest {

    /**
     * Valida os valores de cor em formato int convertidos do hexadecimal.
     */
    @Test
    fun testAppColorsHexadecimalValues() {
        assertEquals(Color.parseColor("#1E40AF"), AppColors.COLOR_PRIMARY)
        assertEquals(Color.parseColor("#2563EB"), AppColors.COLOR_PRIMARY_LIGHT)
        assertEquals(Color.parseColor("#3B82F6"), AppColors.COLOR_ACCENT)
        assertEquals(Color.parseColor("#F1F5F9"), AppColors.COLOR_BG)
        assertEquals(Color.parseColor("#FFFFFF"), AppColors.COLOR_CARD_BG)
        assertEquals(Color.parseColor("#CBD5E1"), AppColors.COLOR_STROKE)
        assertEquals(Color.parseColor("#0F172A"), AppColors.COLOR_TEXT_MAIN)
        assertEquals(Color.parseColor("#64748B"), AppColors.COLOR_TEXT_MUTED)
        assertEquals(Color.parseColor("#059669"), AppColors.COLOR_GREEN_BG)
        assertEquals(Color.WHITE, AppColors.COLOR_GREEN_TEXT)
        assertEquals(Color.parseColor("#2563EB"), AppColors.COLOR_CHIP_BG)
        assertEquals(Color.WHITE, AppColors.COLOR_CHIP_TEXT)
    }

    /**
     * Valida o contraste e distinção entre cores primárias e secundárias do aplicativo.
     */
    @Test
    fun testColorsContrastAndDistinctness() {
        assertNotEquals(AppColors.COLOR_PRIMARY, AppColors.COLOR_BG)
        assertNotEquals(AppColors.COLOR_TEXT_MAIN, AppColors.COLOR_TEXT_MUTED)
    }
}
