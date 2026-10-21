package com.example.campushub

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.screens.CreateEventScreen
import com.example.campushub.ui.screens.EventDetail
import com.example.campushub.ui.screens.FavoritesScreen
import com.example.campushub.ui.screens.HomeScreen
import com.example.campushub.ui.screens.LoginScreen
import com.example.campushub.ui.screens.MyCreatedEventsScreen
import com.example.campushub.ui.screens.ProfileScreen
import com.example.campushub.ui.screens.SubscriptionsScreen

/**
 * Atividade Principal do CampusHub.
 * Atua como o orquestrador de navegação do aplicativo, gerenciando as telas,
 * o manipulador de botão voltar (OnBackPressedDispatcher) e delegando a construção
 * das telas para seus respectivos componentes no pacote `ui.screens`.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var userRepo: UserPreferencesRepository
    private var telaAtual = "HOME"

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
     * Associa o callback da barra de navegação inferior às funções de mudança de tela.
     */
    private fun criarTelaBase(
        exibirBottomNav: Boolean = true,
        abaAtiva: String = "HOME"
    ): Pair<FrameLayout, LinearLayout> {
        val (root, layout) = UIComponents.criarTelaBase(
            context = this,
            exibirBottomNav = exibirBottomNav,
            abaAtiva = abaAtiva,
            onNavigate = { aba ->
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
        AuthRepository.deslogar()
        mostrarLogin()
    }

    // =========================================================
    // FLUXO DE AUTENTICAÇÃO (LOGIN, CADASTRO, RECUPERAÇÃO)
    // =========================================================

    /**
     * Exibe a tela de login para o usuário.
     */
    private fun mostrarLogin() {
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

    /**
     * Exibe a tela de cadastro de nova conta.
     */
    private fun mostrarCadastro() {
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

    /**
     * Exibe a tela de recuperação de senha por e-mail.
     */
    private fun mostrarRecuperacao() {
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

    /**
     * Exibe a tela inicial com busca, filtros de evento e lista principal.
     */
    private fun mostrarHome() {
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

    /**
     * Exibe o formulário para criar um novo evento ou editar um evento existente.
     */
    private fun mostrarCriarEvento(eventoParaEditar: Evento? = null) {
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

    private fun mostrarEditarEvento(evento: Evento) {
        mostrarCriarEvento(evento)
    }

    // =========================================================
    // EVENTOS CRIADOS POR MIM
    // =========================================================

    /**
     * Exibe a lista de eventos publicados pelo usuário logado.
     */
    private fun mostrarEventosCriados() {
        telaAtual = "EVENTOS_CRIADOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        MyCreatedEventsScreen.construirTelaEventosCriados(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onEditarEvento = { evento -> mostrarEditarEvento(evento) },
            onCriarNovoEvento = { mostrarCriarEvento() },
            onVerDetalhes = { evento -> mostrarDetalhes(evento) }
        )
    }

    // =========================================================
    // DETALHES DO EVENTO
    // =========================================================

    /**
     * Exibe a tela de detalhes com informações, avaliações e comentários do evento.
     */
    private fun mostrarDetalhes(evento: Evento) {
        telaAtual = "DETALHES"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        EventDetail.construirTelaDetalhes(
            context = this,
            layout = layout,
            evento = evento,
            userRepo = userRepo,
            onEditarEvento = { eventoParaEditar -> mostrarEditarEvento(eventoParaEditar) },
            onAtualizarTela = { mostrarDetalhes(evento) },
            onVoltar = { mostrarHome() }
        )
    }

    // =========================================================
    // FAVORITOS
    // =========================================================

    /**
     * Exibe os eventos marcados como favoritos pelo usuário.
     */
    private fun mostrarFavoritos() {
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

    /**
     * Exibe os eventos nos quais o usuário está inscrito.
     */
    private fun mostrarMeusEventos() {
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

    /**
     * Exibe os dados do perfil acadêmico do usuário.
     */
    private fun mostrarPerfil() {
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

    /**
     * Exibe a tela para alterar dados cadastrais do perfil.
     */
    private fun mostrarEditarPerfil() {
        telaAtual = "EDITAR_PERFIL"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "PERFIL")

        ProfileScreen.construirTelaEditarPerfil(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onPerfilAtualizado = { mostrarPerfil() },
            onCancelar = { mostrarPerfil() }
        )
    }
}
