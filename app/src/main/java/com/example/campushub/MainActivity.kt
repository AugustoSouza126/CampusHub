package com.example.campushub

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var userRepo: UserPreferencesRepository
    private var telaAtual = "HOME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(this)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
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

    // =========================================================
    // NAVEGAÇÃO & CRIAÇÃO DE TELA BASE
    // =========================================================

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

    private fun dpToPx(dp: Int): Int = UIComponents.dpToPx(this, dp)

    private fun criarHeaderBar(titulo: String, subtitulo: String? = null): View {
        return UIComponents.criarHeaderBar(this, titulo, subtitulo)
    }

    private fun criarRotulo(texto: String): TextView {
        return UIComponents.criarRotulo(this, texto)
    }

    private fun criarCampoTexto(
        hint: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        valorInicial: String = ""
    ): EditText {
        return UIComponents.criarCampoTexto(this, hint, inputType, valorInicial)
    }

    private fun criarBotao(
        texto: String,
        primario: Boolean = true,
        perigo: Boolean = false,
        acao: () -> Unit
    ): Button {
        return UIComponents.criarBotao(this, texto, primario, perigo, acao)
    }

    private fun deslogarUsuario() {
        AuthRepository.deslogar()
        mostrarLogin()
    }

    // =========================================================
    // TELA DE LOGIN
    // =========================================================

    private fun mostrarLogin() {
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        layout.addView(criarHeaderBar("Bem-vindo ao CampusHub", "Acesse sua conta para ver os eventos da universidade."))

        layout.addView(criarRotulo("E-mail institucional"))
        val inputEmail = criarCampoTexto("exemplo@universidade.edu.br", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        layout.addView(inputEmail)

        layout.addView(criarRotulo("Senha"))
        val inputSenha = criarCampoTexto(
            "Digite sua senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputSenha)

        layout.addView(criarBotao("ENTRAR NO SISTEMA") {
            val emailDigitado = inputEmail.text.toString().trim()
            val senhaDigitada = inputSenha.text.toString()

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha para entrar.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            AuthRepository.fazerLogin(
                email = emailDigitado,
                senha = senhaDigitada,
                onSuccess = { user ->
                    userRepo.salvarUsuario(
                        nome = user.displayName ?: userRepo.getUsuarioNome(),
                        matricula = userRepo.getUsuarioMatricula(),
                        email = user.email ?: emailDigitado,
                        senha = senhaDigitada
                    )
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    mostrarHome()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(criarBotao("CRIAR NOVA CONTA", primario = false) {
            mostrarCadastro()
        })

        layout.addView(criarBotao("ESQUECI MINHA SENHA", primario = false) {
            mostrarRecuperacao()
        })
    }

    // =========================================================
    // TELA DE CADASTRO DE CONTA
    // =========================================================

    private fun mostrarCadastro() {
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        layout.addView(criarHeaderBar("Criar Cadastro", "Preencha suas informações para criar a conta."))

        layout.addView(criarRotulo("Nome completo"))
        val inputNome = criarCampoTexto("Ex: João da Silva")
        layout.addView(inputNome)

        layout.addView(criarRotulo("Matrícula"))
        val inputMatricula = criarCampoTexto("Ex: 2026100123", InputType.TYPE_CLASS_NUMBER)
        layout.addView(inputMatricula)

        layout.addView(criarRotulo("E-mail"))
        val inputEmail = criarCampoTexto(
            "exemplo@universidade.edu.br",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        layout.addView(inputEmail)

        layout.addView(criarRotulo("Senha"))
        val inputSenha = criarCampoTexto(
            "Senha (mínimo 6 caracteres)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputSenha)

        layout.addView(criarRotulo("Confirmar senha"))
        val inputConfirmarSenha = criarCampoTexto(
            "Repita a sua senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputConfirmarSenha)

        layout.addView(criarBotao("FINALIZAR CADASTRO") {
            val nome = inputNome.text.toString().trim()
            val matricula = inputMatricula.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val senha = inputSenha.text.toString()
            val confirmarSenha = inputConfirmarSenha.text.toString()

            if (nome.isEmpty()) {
                Toast.makeText(this, "Digite seu nome completo.", Toast.LENGTH_SHORT).show()
                inputNome.requestFocus()
                return@criarBotao
            }

            if (matricula.isEmpty()) {
                Toast.makeText(this, "Digite sua matrícula.", Toast.LENGTH_SHORT).show()
                inputMatricula.requestFocus()
                return@criarBotao
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                inputEmail.requestFocus()
                return@criarBotao
            }

            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve conter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                inputSenha.requestFocus()
                return@criarBotao
            }

            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                inputConfirmarSenha.requestFocus()
                return@criarBotao
            }

            AuthRepository.cadastrarUsuario(
                email = email,
                senha = senha,
                nome = nome,
                onSuccess = { _ ->
                    userRepo.salvarUsuario(nome, matricula, email, senha)
                    Toast.makeText(this, "Conta criada com sucesso no Firebase!", Toast.LENGTH_SHORT).show()
                    mostrarHome()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(criarBotao("VOLTAR AO LOGIN", primario = false) {
            mostrarLogin()
        })
    }

    // =========================================================
    // TELA DE RECUPERAÇÃO DE SENHA
    // =========================================================

    private fun mostrarRecuperacao() {
        val (_, layout) = criarTelaBase(exibirBottomNav = false)

        layout.addView(criarHeaderBar("Recuperar Senha", "Informe seu e-mail cadastrado para redefinir a senha."))

        layout.addView(criarRotulo("E-mail cadastrado"))
        val inputEmail = criarCampoTexto(
            "exemplo@universidade.edu.br",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        layout.addView(inputEmail)

        layout.addView(criarBotao("ENVIAR CÓDIGO DE RECUPERAÇÃO") {
            val email = inputEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            AuthRepository.enviarEmailRecuperacao(
                email = email,
                onSuccess = {
                    Toast.makeText(this, "E-mail de redefinição enviado para $email!", Toast.LENGTH_LONG).show()
                    mostrarLogin()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(criarBotao("VOLTAR", primario = false) {
            mostrarLogin()
        })
    }

    // =========================================================
    // TELA PRINCIPAL (HOME)
    // =========================================================

    private fun mostrarHome() {
        telaAtual = "HOME"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        val nome = userRepo.getUsuarioNome()
        val saudacao = if (nome.isNotEmpty()) "Olá, $nome! 👋" else "Olá! 👋"

        layout.addView(criarHeaderBar(saudacao, "Confira os eventos da universidade e garanta sua vaga."))

        layout.addView(criarBotao("➕ CRIAR NOVO EVENTO", primario = true) {
            mostrarCriarEvento()
        })

        layout.addView(criarBotao("📢 EVENTOS CRIADOS POR MIM", primario = false) {
            mostrarEventosCriados()
        })

        var termoBusca = ""
        var situacaoSelecionada = "Todos"
        var categoriaSelecionada = "Todas"

        // 1. BUSCA POR TÍTULO
        layout.addView(criarRotulo("Buscar por título"))
        val inputBusca = criarCampoTexto("🔎 Digite o título do evento...")
        layout.addView(inputBusca)

        // 2. FILTRO DE SITUAÇÃO (Todos / Próximos / Encerrados)
        layout.addView(criarRotulo("Situação do evento"))
        val rowSituacao = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val opcoesSituacao = listOf("Todos", "Próximos", "Encerrados")
        val botoesSituacao = mutableListOf<Button>()

        val containerEventos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun aplicarFiltros() {
            containerEventos.removeAllViews()

            val tvCarregando = TextView(this).apply {
                text = "Carregando eventos do Firebase..."
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(20), 0, dpToPx(20))
                gravity = Gravity.CENTER
            }
            containerEventos.addView(tvCarregando)

            EventRepository.getEventos(
                onSuccess = { lista ->
                    containerEventos.removeAllViews()
                    val filtrados = lista.filter { evento ->
                        val bateTitulo = termoBusca.isBlank() || evento.titulo.contains(termoBusca, ignoreCase = true)
                        val bateCategoria = categoriaSelecionada == "Todas" || evento.categoria.equals(categoriaSelecionada, ignoreCase = true)
                        val bateSituacao = when (situacaoSelecionada) {
                            "Próximos" -> !evento.encerrado
                            "Encerrados" -> evento.encerrado
                            else -> true
                        }
                        bateTitulo && bateCategoria && bateSituacao
                    }

                    if (filtrados.isEmpty()) {
                        val tvVazio = TextView(this).apply {
                            text = "Nenhum evento encontrado com os filtros selecionados."
                            textSize = 15f
                            setTextColor(AppColors.COLOR_TEXT_MUTED)
                            setPadding(0, dpToPx(20), 0, dpToPx(20))
                            gravity = Gravity.CENTER
                        }
                        containerEventos.addView(tvVazio)
                    } else {
                        filtrados.forEach { evento ->
                            adicionarCardEvento(containerEventos, evento)
                        }
                    }
                },
                onError = { error ->
                    containerEventos.removeAllViews()
                    val tvErro = TextView(this).apply {
                        text = "Erro ao carregar eventos do Firebase: $error"
                        textSize = 14f
                        setTextColor(Color.parseColor("#DC2626"))
                        setPadding(0, dpToPx(20), 0, dpToPx(20))
                        gravity = Gravity.CENTER
                    }
                    containerEventos.addView(tvErro)
                }
            )
        }

        fun atualizarEstiloSituacao() {
            botoesSituacao.forEachIndexed { index, btn ->
                val opcao = opcoesSituacao[index]
                val selecionado = (opcao == situacaoSelecionada)
                if (selecionado) {
                    btn.background = UIComponents.criarShape(this, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 10f)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.background = UIComponents.criarShape(this, Color.WHITE, AppColors.COLOR_STROKE, 1, 10f)
                    btn.setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
            }
        }

        opcoesSituacao.forEach { opcao ->
            val btn = MaterialButton(this).apply {
                text = opcao
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                isAllCaps = false
                cornerRadius = dpToPx(10)
                setOnClickListener {
                    situacaoSelecionada = opcao
                    atualizarEstiloSituacao()
                    aplicarFiltros()
                }
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(42), 1f).apply {
                    setMargins(dpToPx(2), 0, dpToPx(2), 0)
                }
            }
            botoesSituacao.add(btn)
            rowSituacao.addView(btn)
        }
        atualizarEstiloSituacao()
        layout.addView(rowSituacao)

        // 3. FILTRO DE CATEGORIA
        layout.addView(criarRotulo("Categoria"))
        val scrollCategorias = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(16))
            }
        }

        val rowCategorias = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val categoriasOpcoes = listOf("Todas", "Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança")
        val botoesCategorias = mutableListOf<Button>()

        fun atualizarEstiloCategorias() {
            botoesCategorias.forEachIndexed { index, btn ->
                val cat = categoriasOpcoes[index]
                val selecionada = (cat == categoriaSelecionada)
                if (selecionada) {
                    btn.background = UIComponents.criarShape(this, AppColors.COLOR_PRIMARY_LIGHT, Color.TRANSPARENT, 0, 20f)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.background = UIComponents.criarShape(this, Color.WHITE, AppColors.COLOR_STROKE, 1, 20f)
                    btn.setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
            }
        }

        categoriasOpcoes.forEach { cat ->
            val btnCat = MaterialButton(this).apply {
                text = cat
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                isAllCaps = false
                cornerRadius = dpToPx(20)
                setOnClickListener {
                    categoriaSelecionada = cat
                    atualizarEstiloCategorias()
                    aplicarFiltros()
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(38)
                ).apply {
                    setMargins(0, 0, dpToPx(8), 0)
                }
            }
            botoesCategorias.add(btnCat)
            rowCategorias.addView(btnCat)
        }
        atualizarEstiloCategorias()
        scrollCategorias.addView(rowCategorias)
        layout.addView(scrollCategorias)

        inputBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                termoBusca = s?.toString() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        aplicarFiltros()
        layout.addView(containerEventos)
    }

    // =========================================================
    // CARD DO EVENTO
    // =========================================================

    private fun adicionarCardEvento(parent: LinearLayout, evento: Evento) {
        val card = CardView(this).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutInterno = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Cabeçalho do Card (Categoria + Badge Data + Heart)
        val rowTop = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            weightSum = 1f
        }

        val tvCategoria = TextView(this).apply {
            text = evento.categoria.uppercase(Locale.getDefault())
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_CHIP_TEXT)
            background = UIComponents.criarShape(this@MainActivity, AppColors.COLOR_CHIP_BG, Color.TRANSPARENT, 0, 8f)
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
        }
        rowTop.addView(tvCategoria)

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }
        rowTop.addView(spacer)

        val tvDataBadge = TextView(this).apply {
            text = "📅 ${evento.dataBadge}"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_PRIMARY_LIGHT)
        }
        rowTop.addView(tvDataBadge)

        val tvHeart = TextView(this).apply {
            text = if (userRepo.isFavorito(evento.id)) " ❤️" else " 🤍"
            textSize = 18f
            setPadding(dpToPx(8), 0, 0, 0)
            setOnClickListener {
                if (userRepo.isFavorito(evento.id)) {
                    userRepo.removerFavorito(evento.id)
                    text = " 🤍"
                    Toast.makeText(this@MainActivity, "Removido dos favoritos.", Toast.LENGTH_SHORT).show()
                } else {
                    userRepo.salvarFavorito(evento.id)
                    text = " ❤️"
                    Toast.makeText(this@MainActivity, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rowTop.addView(tvHeart)

        layoutInterno.addView(rowTop)

        // Título
        val tvTitulo = TextView(this).apply {
            text = evento.titulo
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, dpToPx(10), 0, dpToPx(4))
        }
        layoutInterno.addView(tvTitulo)

        // Status, Encerrado e Rating Badges
        val rowBadges = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (evento.encerrado) {
            val tvEncerrado = TextView(this).apply {
                text = "🏁 ENCERRADO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#475569"))
                background = UIComponents.criarShape(this@MainActivity, Color.parseColor("#E2E8F0"), Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(2), dpToPx(6), dpToPx(6))
                }
            }
            rowBadges.addView(tvEncerrado)
        }

        if (userRepo.isInscrito(evento.id)) {
            val tvBadge = TextView(this).apply {
                text = "✅ INSCRITO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_GREEN_TEXT)
                background = UIComponents.criarShape(this@MainActivity, AppColors.COLOR_GREEN_BG, Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(2), dpToPx(6), dpToPx(6))
                }
            }
            rowBadges.addView(tvBadge)
        }

        val (media, total) = userRepo.getMediaAvaliacoes(evento.id)
        if (total > 0) {
            val tvMedia = TextView(this).apply {
                text = String.format(Locale.getDefault(), "⭐ %.1f (%d)", media, total)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                background = UIComponents.criarShape(this@MainActivity, Color.parseColor("#FEF3C7"), Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(2), 0, dpToPx(6))
                }
            }
            rowBadges.addView(tvMedia)
        }

        layoutInterno.addView(rowBadges)

        // Local e Horário
        val tvInfo = TextView(this).apply {
            text = "🕒 ${evento.horario}  •  📍 ${evento.local}"
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, dpToPx(2), 0, dpToPx(8))
        }
        layoutInterno.addView(tvInfo)

        // Descrição
        val tvDesc = TextView(this).apply {
            text = evento.descricao
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(12))
        }
        layoutInterno.addView(tvDesc)

        // Botão Ver Detalhes
        val btnDetalhes = criarBotao("VER DETALHES", primario = true) {
            mostrarDetalhes(evento)
        }
        layoutInterno.addView(btnDetalhes)

        card.addView(layoutInterno)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dpToPx(4), 0, dpToPx(10))
        }

        parent.addView(card, params)
    }

    // =========================================================
    // TELA CRIAR / EDITAR EVENTO
    // =========================================================

    private fun mostrarCriarEvento(eventoParaEditar: Evento? = null) {
        telaAtual = "CRIAR_EVENTO"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        com.example.campushub.ui.screens.CreateEventScreen.construirTelaCriarEvento(
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
    // TELA EVENTOS CRIADOS POR MIM
    // =========================================================

    private fun mostrarEventosCriados() {
        telaAtual = "EVENTOS_CRIADOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        com.example.campushub.ui.screens.MyCreatedEventsScreen.construirTelaEventosCriados(
            context = this,
            layout = layout,
            userRepo = userRepo,
            onEditarEvento = { evento -> mostrarEditarEvento(evento) },
            onCriarNovoEvento = { mostrarCriarEvento() },
            onVerDetalhes = { evento -> mostrarDetalhes(evento) }
        )
    }

    // =========================================================
    // TELA DE DETALHES DO EVENTO
    // =========================================================

    private fun mostrarDetalhes(evento: Evento) {
        telaAtual = "DETALHES"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        com.example.campushub.ui.screens.EventDetail.construirTelaDetalhes(
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
    // TELA MEUS FAVORITOS
    // =========================================================

    private fun mostrarFavoritos() {
        telaAtual = "FAVORITOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "FAVORITOS")

        layout.addView(criarHeaderBar("Meus Favoritos", "Eventos que você salvou para acompanhar."))

        val containerLista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(containerLista)

        val favoritosIds = userRepo.getFavoritos()

        EventRepository.getEventos(
            onSuccess = { lista ->
                containerLista.removeAllViews()
                val favoritosLista = lista.filter { favoritosIds.contains(it.id.toString()) }

                if (favoritosLista.isEmpty()) {
                    val tvVazio = TextView(this).apply {
                        text = "Você ainda não favoritou nenhum evento.\nClique no ❤️ dos eventos para salvar aqui!"
                        textSize = 15f
                        setTextColor(AppColors.COLOR_TEXT_MUTED)
                        setPadding(0, dpToPx(30), 0, dpToPx(20))
                        gravity = Gravity.CENTER
                    }
                    containerLista.addView(tvVazio)

                    containerLista.addView(criarBotao("EXPLORAR EVENTOS DISPONÍVEIS") {
                        mostrarHome()
                    })
                } else {
                    favoritosLista.forEach { evento ->
                        adicionarCardEvento(containerLista, evento)
                    }
                }
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // =========================================================
    // TELA MEUS EVENTOS
    // =========================================================

    private fun mostrarMeusEventos() {
        telaAtual = "MEUS_EVENTOS"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "MEUS_EVENTOS")

        layout.addView(criarHeaderBar("Minhas Inscrições", "Acompanhe todos os eventos em que você se inscreveu."))

        val containerLista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(containerLista)

        val inscritosIds = userRepo.getInscricoes()

        EventRepository.getEventos(
            onSuccess = { lista ->
                containerLista.removeAllViews()
                val meusEventosLista = lista.filter { inscritosIds.contains(it.id.toString()) }

                if (meusEventosLista.isEmpty()) {
                    val tvVazio = TextView(this).apply {
                        text = "Você ainda não está inscrito em nenhum evento."
                        textSize = 15f
                        setTextColor(AppColors.COLOR_TEXT_MUTED)
                        setPadding(0, dpToPx(30), 0, dpToPx(20))
                        gravity = Gravity.CENTER
                    }
                    containerLista.addView(tvVazio)

                    containerLista.addView(criarBotao("EXPLORAR EVENTOS DISPONÍVEIS") {
                        mostrarHome()
                    })
                } else {
                    meusEventosLista.forEach { evento ->
                        adicionarCardEvento(containerLista, evento)
                    }
                }
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // =========================================================
    // TELA DE PERFIL
    // =========================================================

    private fun mostrarPerfil() {
        telaAtual = "PERFIL"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "PERFIL")

        layout.addView(criarHeaderBar("Meu Perfil", "Gerencie os dados da sua conta acadêmica."))

        val cardPerfil = CardView(this).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val innerPerfil = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun criarCampoPerfilView(icone: String, rotulo: String, valor: String) {
            val tvRotulo = TextView(this).apply {
                text = "$icone  $rotulo"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(8), 0, dpToPx(2))
            }
            innerPerfil.addView(tvRotulo)

            val tvValor = TextView(this).apply {
                text = valor.ifEmpty { "Não informado" }
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_TEXT_MAIN)
                setPadding(0, 0, 0, dpToPx(8))
            }
            innerPerfil.addView(tvValor)
        }

        criarCampoPerfilView("👤", "Nome Completo", userRepo.getUsuarioNome())
        criarCampoPerfilView("📄", "Matrícula", userRepo.getUsuarioMatricula())
        criarCampoPerfilView("✉️", "E-mail Institucional", userRepo.getUsuarioEmail())

        cardPerfil.addView(innerPerfil)

        val paramsCard = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dpToPx(4), 0, dpToPx(16))
        }

        layout.addView(cardPerfil, paramsCard)

        layout.addView(criarBotao("EDITAR PERFIL") {
            mostrarEditarPerfil()
        })

        layout.addView(criarBotao("📢 EVENTOS CRIADOS POR MIM", primario = false) {
            mostrarEventosCriados()
        })

        layout.addView(criarBotao("SAIR DA CONTA", primario = false) {
            deslogarUsuario()
        })
    }

    // =========================================================
    // TELA DE EDITAR PERFIL
    // =========================================================

    private fun mostrarEditarPerfil() {
        telaAtual = "EDITAR_PERFIL"
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "PERFIL")

        layout.addView(criarHeaderBar("Editar Perfil", "Atualize seus dados cadastrais."))

        layout.addView(criarRotulo("Nome completo"))
        val inputNome = criarCampoTexto("Nome completo", valorInicial = userRepo.getUsuarioNome())
        layout.addView(inputNome)

        layout.addView(criarRotulo("Matrícula"))
        val inputMatricula = criarCampoTexto(
            "Matrícula",
            inputType = InputType.TYPE_CLASS_NUMBER,
            valorInicial = userRepo.getUsuarioMatricula()
        )
        layout.addView(inputMatricula)

        layout.addView(criarRotulo("E-mail"))
        val inputEmail = criarCampoTexto(
            "E-mail",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            valorInicial = userRepo.getUsuarioEmail()
        )
        layout.addView(inputEmail)

        layout.addView(criarBotao("SALVAR ALTERAÇÕES") {
            val novoNome = inputNome.text.toString().trim()
            val novaMatricula = inputMatricula.text.toString().trim()
            val novoEmail = inputEmail.text.toString().trim()

            if (novoNome.isEmpty() || novaMatricula.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(this, "Todos os campos devem ser preenchidos.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(novoEmail).matches()) {
                Toast.makeText(this, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            userRepo.salvarUsuario(novoNome, novaMatricula, novoEmail, userRepo.getUsuarioSenha())
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            mostrarPerfil()
        })

        layout.addView(criarBotao("CANCELAR", primario = false) {
            mostrarPerfil()
        })
    }
}
