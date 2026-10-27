package com.example.campushub.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Utilitário responsável por lidar com gravação/reprodução de áudio e
 * persistência de fotos salvas localmente no armazenamento do dispositivo.
 */
object MediaHelper {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioEmAndamentoPath: String? = null

    // =========================================================
    // FOTOS (SALVAMENTO LOCAL DE IMAGENS)
    // =========================================================

    /**
     * Copia uma foto selecionada da galeria (via [Uri]) para o diretório
     * de arquivos internos da aplicação e retorna o caminho absoluto do arquivo.
     */
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

    /**
     * Prepara e retorna um novo arquivo [File] para armazenar a foto capturada pela câmera.
     */
    fun criarArquivoFotoCamera(context: Context): File {
        val pastaFotos = File(context.filesDir, "event_photos").apply {
            if (!exists()) mkdirs()
        }
        return File(pastaFotos, "camera_${UUID.randomUUID()}.jpg")
    }

    // =========================================================
    // ÁUDIOS (GRAVAÇÃO E REPRODUÇÃO LOCAL)
    // =========================================================

    /**
     * Inicia a gravação de áudio do microfone salvando no arquivo especificado.
     */
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

    /**
     * Parar e finalizar a gravação de áudio em andamento.
     * Retorna o caminho do arquivo gravado ou null em caso de falha.
     */
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

    /**
     * Reproduz o arquivo de áudio no caminho fornecido.
     */
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

    /**
     * Para a reprodução de áudio atual.
     */
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
}
