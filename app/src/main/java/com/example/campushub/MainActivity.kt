package com.example.campushub

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.MediaHelper
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.screens.CreateEventScreen
import com.example.campushub.ui.screens.EventDetail
import com.example.campushub.ui.screens.FavoritesScreen
import com.example.campushub.ui.screens.HomeScreen
import com.example.campushub.ui.screens.LoginScreen
import com.example.campushub.ui.screens.MyCreatedEventsScreen
import com.example.campushub.ui.screens.ProfileScreen
import com.example.campushub.ui.screens.SubscriptionsScreen
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var userRepo: UserPreferencesRepository
    private var telaAtual = "HOME"

    private var currentDetailRoot: FrameLayout? = null
    private var currentDetailLayout: LinearLayout? = null
    private var currentDetailScrollView: ScrollView? = null

    private var eventoIdAtualParaMidia: Int = 0
    private var photoFileCamera: File? = null
    private var videoFileCamera: File? = null
    private var onMidiaAtualizadaCallback: (() -> Unit)? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoFileCamera != null && photoFileCamera!!.exists()) {
            userRepo.adicionarFoto(eventoIdAtualParaMidia, photoFileCamera!!.absolutePath)
            Toast.makeText(this, "Foto capturada e publicada no evento!", Toast.LENGTH_SHORT).show()
            onMidiaAtualizadaCallback?.invoke()
        }
    }

    private var onFotoCapaCallback: ((String) -> Unit)? = null

    private val pickCapaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val caminhoSalvo = MediaHelper.salvarFotoGaleriaLocalmente(this, uri)
            if (caminhoSalvo != null) {
                onFotoCapaCallback?.invoke(caminhoSalvo)
            }
        }
    }

    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val caminhoSalvo = MediaHelper.salvarFotoGaleriaLocalmente(this, uri)
            if (caminhoSalvo != null) {
                userRepo.adicionarFoto(eventoIdAtualParaMidia, caminhoSalvo)
                Toast.makeText(this, "Foto da galeria publicada no evento!", Toast.LENGTH_SHORT).show()
                onMidiaAtualizadaCallback?.invoke()
            }
        }
    }

    private val recordVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && videoFileCamera != null && videoFileCamera!!.exists()) {
            userRepo.adicionarVideo(eventoIdAtualParaMidia, videoFileCamera!!.absolutePath)
            Toast.makeText(this, "Vídeo gravado e publicado no evento!", Toast.LENGTH_SHORT).show()
            onMidiaAtualizadaCallback?.invoke()
        }
    }

    private val pickVideoGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val caminhoSalvo = MediaHelper.salvarVideoGaleriaLocalmente(this, uri)
            if (caminhoSalvo != null) {
                userRepo.adicionarVideo(eventoIdAtualParaMidia, caminhoSalvo)
                Toast.makeText(this, "Vídeo da galeria publicado no evento!", Toast.LENGTH_SHORT).show()
                onMidiaAtualizadaCallback?.invoke()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map[android.Manifest.permission.RECORD_AUDIO] == true) {
            Toast.makeText(this, "Permissão de gravação de áudio concedida!", Toast.LENGTH_SHORT).show()
            onMidiaAtualizadaCallback?.invoke()
        }
        if (map[android.Manifest.permission.CAMERA] == true) {
            Toast.makeText(this, "Permissão de câmera concedida!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (telaAtual != "HOME" && telaAtual != "LOGIN") {
                    mostrarHome()
                } else {
                    finish()
                }
            }
        })

        if (AuthRepository.isUsuarioLogado()) {
            mostrarHome()
        } else {
            mostrarLogin()
        }
    }

    private fun criarTelaBase(
        exibirBottomNav: Boolean = true,
        abaAtiva: String = "HOME",
        restoreScrollY: Int = 0
    ): Pair<FrameLayout, LinearLayout> {
        val (root, layout) = UIComponents.criarTelaBase(
            context = this,
            exibirBottomNav = exibirBottomNav,
            abaAtiva = abaAtiva,
            restoreScrollY = restoreScrollY,
            onNavigate = { aba ->
                limparDetalhesCache()
                when (aba) {
                    "HOME" -> mostrarHome()
                    "FAVORITOS" -> mostrarFavoritos()
                    "MEUS_EVENTOS" -> mostrarMeusEventos()
                    "PERFIL" -> mostrarPerfil()
                }
            }
        )
        setContentView(root)
        return Pair(root, layout)
    }

    private fun deslogarUsuario() {
        limparDetalhesCache()
        AuthRepository.deslogar()
        mostrarLogin()
    }

    private fun tirarFotoCamera(eventoId: Int, onConcluido: () -> Unit) {
        eventoIdAtualParaMidia = eventoId
        onMidiaAtualizadaCallback = onConcluido

        val temPermissaoCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!temPermissaoCamera) {
            requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            return
        }

        try {
            photoFileCamera = MediaHelper.criarArquivoFotoCamera(this)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFileCamera!!)
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir câmera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escolherFotoGaleria(eventoId: Int, onConcluido: () -> Unit) {
        eventoIdAtualParaMidia = eventoId
        onMidiaAtualizadaCallback = onConcluido
        pickGalleryLauncher.launch("image/*")
    }

    private fun gravarVideoCamera(eventoId: Int, onConcluido: () -> Unit) {
        eventoIdAtualParaMidia = eventoId
        onMidiaAtualizadaCallback = onConcluido

        val temPermissaoCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!temPermissaoCamera) {
            requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            return
        }

        try {
            videoFileCamera = MediaHelper.criarArquivoVideoCamera(this)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", videoFileCamera!!)
            recordVideoLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir câmera para vídeo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escolherVideoGaleria(eventoId: Int, onConcluido: () -> Unit) {
        eventoIdAtualParaMidia = eventoId
        onMidiaAtualizadaCallback = onConcluido
        pickVideoGalleryLauncher.launch("video/*")
    }

    private fun solicitarPermissaoAudio(onConcluido: () -> Unit) {
        onMidiaAtualizadaCallback = onConcluido
        requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
    }

    private fun mostrarLogin() {
        limparDetalhesCache()
        telaAtual = "LOGIN"
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        LoginScreen.construirTelaLogin(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onLoginSucesso = { mostrarHome() },
            onIrParaCadastro = { mostrarCadastro() },
            onIrParaRecuperacao = { mostrarRecuperacao() }
        )
    }

    private fun mostrarCadastro() {
        limparDetalhesCache()
        telaAtual = "CADASTRO"
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        LoginScreen.construirTelaCadastro(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onCadastroSucesso = { mostrarHome() },
            onVoltar = { mostrarLogin() }
        )
    }

    private fun mostrarRecuperacao() {
        limparDetalhesCache()
        telaAtual = "RECUPERACAO"
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        LoginScreen.construirTelaRecuperacao(
            context = this,
            layout = layout,
            onVoltar = { mostrarLogin() }
        )
    }

    private fun mostrarHome() {
        limparDetalhesCache()
        telaAtual = "HOME"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        HomeScreen.construirTelaHome(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onCriarNovoEvento = { mostrarCriarEvento() },
            onVerEventosCriados = { mostrarEventosCriados() },
            onVerDetalhes = { evento -> mostrarDetalhes(evento) }
        )
    }

    private fun mostrarCriarEvento(eventoParaEditar: Event? = null) {
        limparDetalhesCache()
        telaAtual = "CRIAR_EVENTO"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        CreateEventScreen.construirTelaCriarEvento(
            context = this,
            layout = layout,
            userRepo = userRepo,
            eventoParaEditar = eventoParaEditar,
            onEscolherFotoCapa = { callback ->
                onFotoCapaCallback = callback
                pickCapaLauncher.launch("image/*")
            },
            onSucesso = { mostrarHome() },
            onVoltar = { mostrarHome() }
        )
    }

    private fun mostrarEditarEvento(evento: Event) {
        mostrarCriarEvento(evento)
    }

    private fun mostrarEventosCriados() {
        limparDetalhesCache()
        telaAtual = "EVENTOS_CRIADOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        MyCreatedEventsScreen.construirTelaEventosCriados(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onEditarEvento = { evento -> mostrarEditarEvento(evento) },
            onCriarNovoEvento = { mostrarCriarEvento() },
            onVerDetalhes = { evento -> mostrarDetalhes(evento) },
            onVoltar = { mostrarHome() }
        )
    }

    private fun recarregarDetalhesEmMesmaPosicao(evento: Event) {
        if (currentDetailLayout != null && currentDetailScrollView != null && telaAtual == "DETALHES") {
            val savedY = currentDetailScrollView!!.scrollY

            EventRepository.getEventos(
                onSuccess = { lista ->
                    val eventoAtualizado = lista.find { it.id == evento.id } ?: evento
                    currentDetailLayout!!.removeAllViews()

                    EventDetail.construirTelaDetalhes(
                        context = this,
                        layout = currentDetailLayout!!,
                        evento = eventoAtualizado,
                        userRepo = userRepo,
                        onEditarEvento = { eventoParaEditar -> mostrarEditarEvento(eventoParaEditar) },
                        onTirarFotoCamera = { tirarFotoCamera(eventoAtualizado.id) { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) } },
                        onEscolherFotoGaleria = { escolherFotoGaleria(eventoAtualizado.id) { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) } },
                        onPedirPermissaoAudio = { solicitarPermissaoAudio { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) } },
                        onGravarVideoCamera = { gravarVideoCamera(eventoAtualizado.id) { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) } },
                        onEscolherVideoGaleria = { escolherVideoGaleria(eventoAtualizado.id) { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) } },
                        onAtualizarTela = { recarregarDetalhesEmMesmaPosicao(eventoAtualizado) },
                        onVoltar = {
                            limparDetalhesCache()
                            mostrarHome()
                        }
                    )

                    currentDetailScrollView!!.post {
                        currentDetailScrollView!!.scrollTo(0, savedY)
                    }
                },
                onError = {
                    currentDetailLayout!!.removeAllViews()

                    EventDetail.construirTelaDetalhes(
                        context = this,
                        layout = currentDetailLayout!!,
                        evento = evento,
                        userRepo = userRepo,
                        onEditarEvento = { eventoParaEditar -> mostrarEditarEvento(eventoParaEditar) },
                        onTirarFotoCamera = { tirarFotoCamera(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
                        onEscolherFotoGaleria = { escolherFotoGaleria(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
                        onPedirPermissaoAudio = { solicitarPermissaoAudio { recarregarDetalhesEmMesmaPosicao(evento) } },
                        onGravarVideoCamera = { gravarVideoCamera(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
                        onEscolherVideoGaleria = { escolherVideoGaleria(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
                        onAtualizarTela = { recarregarDetalhesEmMesmaPosicao(evento) },
                        onVoltar = {
                            limparDetalhesCache()
                            mostrarHome()
                        }
                    )

                    currentDetailScrollView!!.post {
                        currentDetailScrollView!!.scrollTo(0, savedY)
                    }
                }
            )
        } else {
            mostrarDetalhes(evento)
        }
    }

    private fun mostrarDetalhes(evento: Event) {
        telaAtual = "DETALHES"
        val (root, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")
        currentDetailRoot = root
        currentDetailLayout = layout
        currentDetailScrollView = root.getChildAt(0) as? ScrollView

        EventDetail.construirTelaDetalhes(
            context = this,
            layout = layout,
            evento = evento,
            userRepo = userRepo,
            onEditarEvento = { eventoParaEditar -> mostrarEditarEvento(eventoParaEditar) },
            onTirarFotoCamera = { tirarFotoCamera(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
            onEscolherFotoGaleria = { escolherFotoGaleria(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
            onPedirPermissaoAudio = { solicitarPermissaoAudio { recarregarDetalhesEmMesmaPosicao(evento) } },
            onGravarVideoCamera = { gravarVideoCamera(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
            onEscolherVideoGaleria = { escolherVideoGaleria(evento.id) { recarregarDetalhesEmMesmaPosicao(evento) } },
            onAtualizarTela = { recarregarDetalhesEmMesmaPosicao(evento) },
            onVoltar = {
                limparDetalhesCache()
                mostrarHome()
            }
        )
    }

    private fun limparDetalhesCache() {
        currentDetailRoot = null
        currentDetailLayout = null
        currentDetailScrollView = null
    }

    private fun mostrarFavoritos() {
        limparDetalhesCache()
        telaAtual = "FAVORITOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "FAVORITOS")

        FavoritesScreen.construirTelaFavoritos(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onVerDetalhes = { evento -> mostrarDetalhes(evento) },
            onExplorarEventos = { mostrarHome() }
        )
    }

    private fun mostrarMeusEventos() {
        limparDetalhesCache()
        telaAtual = "MEUS_EVENTOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "MEUS_EVENTOS")

        SubscriptionsScreen.construirTelaMeusEventos(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onVerDetalhes = { evento -> mostrarDetalhes(evento) },
            onExplorarEventos = { mostrarHome() }
        )
    }

    private fun mostrarPerfil() {
        limparDetalhesCache()
        telaAtual = "PERFIL"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "PERFIL")

        ProfileScreen.construirTelaPerfil(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onEditarPerfil = { mostrarEditarPerfil() },
            onVerEventosCriados = { mostrarEventosCriados() },
            onDeslogar = { deslogarUsuario() }
        )
    }

    private fun mostrarEditarPerfil() {
        limparDetalhesCache()
        telaAtual = "EDITAR_PERFIL"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "PERFIL")

        ProfileScreen.construirTelaPerfil(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onEditarPerfil = { mostrarEditarPerfil() },
            onVerEventosCriados = { mostrarEventosCriados() },
            onDeslogar = { deslogarUsuario() }
        )
    }
}
