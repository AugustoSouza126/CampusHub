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

/**
 * Atividade Principal do CampusHub.
 * Atua como o orquestrador de navegação do aplicativo, gerenciando as telas,
 * permissões e lançadores de câmera e galeria para publicação de fotos e áudios.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var userRepo: UserPreferencesRepository
    private var telaAtual = "HOME"

    // Cache e referências da Tela de Detalhes para manter posição de rolagem em tempo real
    private var currentDetailRoot: FrameLayout? = null
    private var currentDetailLayout: LinearLayout? = null
    private var currentDetailScrollView: ScrollView? = null

    // Estados e lançadores de mídia (Câmera e Galeria)
    private var eventoIdAtualParaMidia: Int = 0
    private var photoFileCamera: File? = null
    private var onMidiaAtualizadaCallback: (() -> Unit)? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoFileCamera != null && photoFileCamera!!.exists()) {
            userRepo.adicionarFoto(eventoIdAtualParaMidia, photoFileCamera!!.absolutePath)
            Toast.makeText(this, "Foto capturada e publicada no evento!", Toast.LENGTH_SHORT).show()
            onMidiaAtualizadaCallback?.invoke()
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

        // Configuração do botão voltar nativo do Android
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (telaAtual != "HOME" && telaAtual != "LOGIN") {
                    mostrarHome()
                } else {
                    finish()
                }
            }
        })

        // Redireciona para Home se logado ou Login se deslogado
        if (AuthRepository.isUsuarioLogado()) {
            mostrarHome()
        } else {
            mostrarLogin()
        }
    }

    // =========================================================
    // NAVEGAÇÃO & CRIAÇÃO DE TELA BASE
    // =========================================================

    /**
     * Cria e define a estrutura base do layout da tela na Activity.
     */
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

    /**
     * Realiza o logout do usuário através do [AuthRepository] e redireciona para a tela de login.
     */
    private fun deslogarUsuario() {
        limparDetalhesCache()
        AuthRepository.deslogar()
        mostrarLogin()
    }

    // =========================================================
    // LANÇADORES DE CÂMERA, GALERIA E ÁUDIO
    // =========================================================

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

    private fun solicitarPermissaoAudio(onConcluido: () -> Unit) {
        onMidiaAtualizadaCallback = onConcluido
        requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
    }

    // =========================================================
    // FLUXO DE AUTENTICAÇÃO (LOGIN, CADASTRO, RECUPERAÇÃO)
    // =========================================================

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

    // =========================================================
    // TELA PRINCIPAL (HOME)
    // =========================================================

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

    // =========================================================
    // CRIAR / EDITAR EVENTO
    // =========================================================

    private fun mostrarCriarEvento(eventoParaEditar: Event? = null) {
        limparDetalhesCache()
        telaAtual = "CRIAR_EVENTO"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        CreateEventScreen.construirTelaCriarEvento(
            context = this,
            layout = layout,
            userRepo = userRepo,
            eventoParaEditar = eventoParaEditar,
            onSucesso = { mostrarHome() },
            onVoltar = { mostrarHome() }
        )
    }

    private fun mostrarEditarEvento(evento: Event) {
        mostrarCriarEvento(evento)
    }

    // =========================================================
    // EVENTOS CRIADOS POR MIM
    // =========================================================

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

    // =========================================================
    // DETALHES DO EVENTO (ATUALIZAÇÃO EM TEMPO REAL SEM DUPLICAR HEADER)
    // =========================================================

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

    // =========================================================
    // FAVORITOS
    // =========================================================

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

    // =========================================================
    // MINHAS INSCRIÇÕES / MEUS EVENTOS
    // =========================================================

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

    // =========================================================
    // PERFIL & EDITAR PERFIL
    // =========================================================

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
