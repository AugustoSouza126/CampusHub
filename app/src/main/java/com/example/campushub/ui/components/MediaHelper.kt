package com.example.campushub.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object MediaHelper {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioEmAndamentoPath: String? = null

    fun salvarFotoGaleriaLocalmente(context: Context, imageUri: Uri): String? {
        return try {
            val pastaFotos = File(context.filesDir, "event_photos").apply {
                if (!exists()) mkdirs()
            }
            val arquivoDestino = File(pastaFotos, "galeria_${UUID.randomUUID()}.jpg")

            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(arquivoDestino)

            if (inputStream != null) {
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                arquivoDestino.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun criarArquivoFotoCamera(context: Context): File {
        val pastaFotos = File(context.filesDir, "event_photos").apply {
            if (!exists()) mkdirs()
        }
        return File(pastaFotos, "camera_${UUID.randomUUID()}.jpg")
    }

    fun iniciarGravacaoAudio(context: Context): String? {
        return try {
            pararGravacaoAudio()

            val pastaAudios = File(context.filesDir, "event_audios").apply {
                if (!exists()) mkdirs()
            }
            val arquivoAudio = File(pastaAudios, "audio_${UUID.randomUUID()}.m4a")
            val caminho = arquivoAudio.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(caminho)
                prepare()
                start()
            }

            audioEmAndamentoPath = caminho
            caminho
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun pararGravacaoAudio(): String? {
        return try {
            val caminho = audioEmAndamentoPath
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
            mediaRecorder = null
            audioEmAndamentoPath = null
            caminho
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isGravando(): Boolean = mediaRecorder != null

    fun reproduzirAudio(caminhoArquivo: String, onConcluido: () -> Unit = {}) {
        try {
            pararAudio()

            val file = File(caminhoArquivo)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(caminhoArquivo)
                prepare()
                setOnCompletionListener {
                    pararAudio()
                    onConcluido()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pararAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isTocando(): Boolean {
        return try {
            mediaPlayer != null && mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    fun salvarVideoGaleriaLocalmente(context: Context, videoUri: Uri): String? {
        return try {
            val pastaVideos = File(context.filesDir, "event_videos").apply {
                if (!exists()) mkdirs()
            }
            val arquivoDestino = File(pastaVideos, "video_${UUID.randomUUID()}.mp4")

            val inputStream: InputStream? = context.contentResolver.openInputStream(videoUri)
            val outputStream = FileOutputStream(arquivoDestino)

            if (inputStream != null) {
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                arquivoDestino.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun criarArquivoVideoCamera(context: Context): File {
        val pastaVideos = File(context.filesDir, "event_videos").apply {
            if (!exists()) mkdirs()
        }
        return File(pastaVideos, "camera_${UUID.randomUUID()}.mp4")
    }

    fun abrirPlayerVideoDialog(context: Context, caminhoArquivo: String, titulo: String = "Vídeo do Evento") {
        val file = File(caminhoArquivo)
        if (!file.exists()) return

        val dialog = AlertDialog.Builder(context).create()
        val containerDialog = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            setBackgroundColor(Color.parseColor("#0F172A"))
        }

        val tvTitulo = TextView(context).apply {
            text = "🎬 $titulo"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 10))
        }
        containerDialog.addView(tvTitulo)

        val videoView = VideoView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UIComponents.dpToPx(context, 260)
            )
            setVideoPath(caminhoArquivo)
            val mediaController = MediaController(context)
            mediaController.setAnchorView(this)
            setMediaController(mediaController)
            start()
        }
        containerDialog.addView(videoView)

        val btnFechar = UIComponents.criarBotao(context, "FECHAR VÍDEO", primario = true) {
            videoView.stopPlayback()
            dialog.dismiss()
        }
        containerDialog.addView(btnFechar)

        dialog.setView(containerDialog)
        dialog.show()
    }
}
