package com.example.campushub.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.ui.components.MediaHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MediaHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCreateCameraPhotoFile() {
        val photoFile = MediaHelper.criarArquivoFotoCamera(context)
        assertNotNull(photoFile)
        assertTrue(photoFile.name.startsWith("camera_"))
        assertTrue(photoFile.name.endsWith(".jpg"))
    }

    @Test
    fun testCreateCameraVideoFile() {
        val videoFile = MediaHelper.criarArquivoVideoCamera(context)
        assertNotNull(videoFile)
        assertTrue(videoFile.name.startsWith("camera_"))
        assertTrue(videoFile.name.endsWith(".mp4"))
    }

    @Test
    fun testInitialMediaStateAndStopMethods() {
        assertFalse(MediaHelper.isGravando())
        assertFalse(MediaHelper.isTocando())

        MediaHelper.pararAudio()
        MediaHelper.pararGravacaoAudio()

        assertFalse(MediaHelper.isGravando())
        assertFalse(MediaHelper.isTocando())
    }
}
