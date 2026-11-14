package com.example.campushub.theme

import android.graphics.Color
import com.example.campushub.ui.theme.AppColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppColorsTest {

    @Test
    fun testAppColorsHexadecimalValues() {
        assertEquals(Color.parseColor("#1D4ED8"), AppColors.COLOR_PRIMARY)
        assertEquals(Color.parseColor("#EFF6FF"), AppColors.COLOR_PRIMARY_LIGHT)
        assertEquals(Color.parseColor("#1D4ED8"), AppColors.COLOR_ACCENT)
        assertEquals(Color.parseColor("#F8FAFC"), AppColors.COLOR_BG)
        assertEquals(Color.parseColor("#FFFFFF"), AppColors.COLOR_CARD_BG)
        assertEquals(Color.parseColor("#E2E8F0"), AppColors.COLOR_STROKE)
        assertEquals(Color.parseColor("#0F172A"), AppColors.COLOR_TEXT_MAIN)
        assertEquals(Color.parseColor("#64748B"), AppColors.COLOR_TEXT_MUTED)
        assertEquals(Color.parseColor("#ECFDF5"), AppColors.COLOR_GREEN_BG)
        assertEquals(Color.parseColor("#047857"), AppColors.COLOR_GREEN_TEXT)
        assertEquals(Color.parseColor("#F1F5F9"), AppColors.COLOR_CHIP_BG)
        assertEquals(Color.parseColor("#334155"), AppColors.COLOR_CHIP_TEXT)
    }

    @Test
    fun testColorsContrastAndDistinctness() {
        assertNotEquals(AppColors.COLOR_PRIMARY, AppColors.COLOR_BG)
        assertNotEquals(AppColors.COLOR_TEXT_MAIN, AppColors.COLOR_TEXT_MUTED)
    }
}
