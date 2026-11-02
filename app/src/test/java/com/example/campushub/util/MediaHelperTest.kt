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

/**
 * Testes unitários para utilitários de mídia [MediaHelper].
 * Testa geração de arquivos locais para foto/vídeo e controle de estados de áudio.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MediaHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Testa a criação de um novo arquivo temporário JPG para captura de foto via câmera.
     */
    @Test
    fun testCreateCameraPhotoFile() {
        val photoFile = MediaHelper.criarArquivoFotoCamera(context)
        assertNotNull(photoFile)
        assertTrue(photoFile.name.startsWith("camera_"))
        assertTrue(photoFile.name.endsWith(".jpg"))
    }

    /**
     * Testa a criação de um novo arquivo temporário MP4 para gravação de vídeo via câmera.
     */
    @Test
    fun testCreateCameraVideoFile() {
        val videoFile = MediaHelper.criarArquivoVideoCamera(context)
        assertNotNull(videoFile)
        assertTrue(videoFile.name.startsWith("camera_"))
        assertTrue(videoFile.name.endsWith(".mp4"))
    }

    /**
     * Verifica os estados iniciais do gravador/reprodutor e a parada segura de áudio e gravação.
     */
    @Test
    fun testInitialMediaStateAndStopMethods() {
        assertFalse(MediaHelper.isGravando())
        assertFalse(MediaHelper.isTocando())

        // Efetua chamadas de interrupção quando nenhum áudio está em execução para garantir resiliência
        MediaHelper.pararAudio()
        MediaHelper.pararGravacaoAudio()

        assertFalse(MediaHelper.isGravando())
        assertFalse(MediaHelper.isTocando())
    }
}
