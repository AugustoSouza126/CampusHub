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
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var userRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(this)

        if (userRepo.getUsuarioEmail().isNotEmpty()) {
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

            val emailSalvo = userRepo.getUsuarioEmail()
            val senhaSalva = userRepo.getUsuarioSenha()

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha para entrar.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            if (emailSalvo.isEmpty()) {
                Toast.makeText(this, "Nenhuma conta cadastrada. Clique em 'CRIAR CONTA'.", Toast.LENGTH_LONG).show()
                return@criarBotao
            }

            if (emailDigitado.equals(emailSalvo, ignoreCase = true) && senhaDigitada == senhaSalva) {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                mostrarHome()
            } else {
                Toast.makeText(this, "E-mail ou senha incorretos.", Toast.LENGTH_SHORT).show()
            }
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

            userRepo.salvarUsuario(nome, matricula, email, senha)
            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
            mostrarHome()
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
            } else {
                Toast.makeText(this, "Instruções enviadas para $email!", Toast.LENGTH_LONG).show()
                mostrarLogin()
            }
        })

        layout.addView(criarBotao("VOLTAR", primario = false) {
            mostrarLogin()
        })
    }

    // =========================================================
    // TELA PRINCIPAL (HOME)
    // =========================================================

    private fun mostrarHome() {
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        val nome = userRepo.getUsuarioNome()
        val saudacao = if (nome.isNotEmpty()) "Olá, $nome! 👋" else "Olá! 👋"

        layout.addView(criarHeaderBar(saudacao, "Confira os eventos da universidade e garanta sua vaga."))

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

            val filtrados = EventRepository.listaEventos.filter { evento ->
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
    // TELA DE DETALHES DO EVENTO
    // =========================================================

    private fun mostrarDetalhes(evento: Evento) {
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "HOME")

        layout.addView(criarHeaderBar(evento.titulo, "Categoria: ${evento.categoria}"))

        val cardInfo = CardView(this).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val innerInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun criarLinhaInfo(icone: String, titulo: String, valor: String) {
            val tv = TextView(this).apply {
                text = "$icone  $titulo: $valor"
                textSize = 15f
                setTextColor(AppColors.COLOR_TEXT_MAIN)
                setPadding(0, dpToPx(6), 0, dpToPx(6))
            }
            innerInfo.addView(tv)
        }

        criarLinhaInfo("📅", "Data", evento.data)
        criarLinhaInfo("⏰", "Horário", evento.horario)
        criarLinhaInfo("📍", "Local", evento.local)
        criarLinhaInfo("👥", "Vagas disponíveis", "${evento.vagas}")

        val tvDescHeader = TextView(this).apply {
            text = "Descrição do Evento"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_PRIMARY_LIGHT)
            setPadding(0, dpToPx(16), 0, dpToPx(4))
        }
        innerInfo.addView(tvDescHeader)

        val tvDesc = TextView(this).apply {
            text = evento.descricao
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MAIN)
        }
        innerInfo.addView(tvDesc)

        cardInfo.addView(innerInfo)
        layout.addView(cardInfo)

        val eFavorito = userRepo.isFavorito(evento.id)
        if (eFavorito) {
            layout.addView(criarBotao("💔 DESFAVORITAR EVENTO", primario = false) {
                userRepo.removerFavorito(evento.id)
                Toast.makeText(this, "Removido dos favoritos.", Toast.LENGTH_SHORT).show()
                mostrarDetalhes(evento)
            })
        } else {
            layout.addView(criarBotao("❤️ FAVORITAR EVENTO", primario = false) {
                userRepo.salvarFavorito(evento.id)
                Toast.makeText(this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                mostrarDetalhes(evento)
            })
        }

        val inscrito = userRepo.isInscrito(evento.id)

        if (inscrito) {
            val tvStatus = TextView(this).apply {
                text = "✅ Sua inscrição está confirmada neste evento."
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_GREEN_TEXT)
                setPadding(0, dpToPx(16), 0, dpToPx(8))
            }
            layout.addView(tvStatus)

            layout.addView(criarBotao("CANCELAR INSCRIÇÃO", primario = false, perigo = true) {
                userRepo.removerInscricao(evento.id)
                Toast.makeText(this, "Inscrição cancelada com sucesso.", Toast.LENGTH_SHORT).show()
                mostrarDetalhes(evento)
            })
        } else {
            layout.addView(criarBotao("INSCREVER-SE NO EVENTO") {
                userRepo.salvarInscricao(evento.id)
                Toast.makeText(this, "Inscrição realizada com sucesso!", Toast.LENGTH_SHORT).show()
                mostrarDetalhes(evento)
            })
        }

        // =========================================================
        // SEÇÃO DE AVALIAÇÃO (NOTAS DE 1 A 5 ESTRELAS)
        // =========================================================

        val cardAvaliacao = CardView(this).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutAvaliacao = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvAvaliacaoTitulo = TextView(this).apply {
            text = "⭐ Avaliação do Evento"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(6))
        }
        layoutAvaliacao.addView(tvAvaliacaoTitulo)

        val (mediaNotas, totalAvaliacoes) = userRepo.getMediaAvaliacoes(evento.id)
        val tvMediaTexto = TextView(this).apply {
            text = if (totalAvaliacoes > 0) {
                String.format(Locale.getDefault(), "Média do Evento: ⭐ %.1f / 5.0 (%d avaliações)", mediaNotas, totalAvaliacoes)
            } else {
                "Este evento ainda não possui avaliações."
            }
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, dpToPx(10))
        }
        layoutAvaliacao.addView(tvMediaTexto)

        if (!evento.encerrado) {
            val tvInfoNaoEncerrado = TextView(this).apply {
                text = "ℹ️ A avaliação por estrelas estará disponível assim que o evento for encerrado."
                textSize = 13f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(4), 0, dpToPx(4))
            }
            layoutAvaliacao.addView(tvInfoNaoEncerrado)
        } else if (!inscrito) {
            val tvInfoNaoInscrito = TextView(this).apply {
                text = "⚠️ Apenas alunos inscritos neste evento podem avaliá-lo."
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, dpToPx(4), 0, dpToPx(4))
            }
            layoutAvaliacao.addView(tvInfoNaoInscrito)
        } else {
            val notaAtual = userRepo.getAvaliacaoDoUsuario(evento.id)
            val tvSuaNota = TextView(this).apply {
                text = if (notaAtual != null) "Sua nota atual: ⭐ $notaAtual / 5 (Clique abaixo para alterar)" else "Clique para atribuir sua nota (1 a 5 estrelas):"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_PRIMARY_LIGHT)
                setPadding(0, dpToPx(6), 0, dpToPx(8))
            }
            layoutAvaliacao.addView(tvSuaNota)

            val rowEstrelas = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 5f
            }

            for (i in 1..5) {
                val ehNotaSelecionada = (notaAtual == i)
                val btnEstrela = MaterialButton(this).apply {
                    text = "⭐ $i"
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    isAllCaps = false
                    cornerRadius = dpToPx(10)

                    if (ehNotaSelecionada) {
                        setBackgroundColor(Color.parseColor("#F59E0B"))
                        setTextColor(Color.WHITE)
                    } else {
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.parseColor("#D97706"))
                        strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#FCD34D"))
                        strokeWidth = dpToPx(1)
                    }

                    setOnClickListener {
                        userRepo.salvarOuAtualizarAvaliacao(evento.id, i)
                        Toast.makeText(this@MainActivity, "Sua avaliação ($i estrelas) foi salva!", Toast.LENGTH_SHORT).show()
                        mostrarDetalhes(evento)
                    }

                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(44), 1f).apply {
                        setMargins(dpToPx(2), 0, dpToPx(2), 0)
                    }
                }
                rowEstrelas.addView(btnEstrela)
            }

            layoutAvaliacao.addView(rowEstrelas)
        }

        cardAvaliacao.addView(layoutAvaliacao)

        val paramsCardAvaliacao = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dpToPx(12), 0, dpToPx(12))
        }

        layout.addView(cardAvaliacao, paramsCardAvaliacao)

        // =========================================================
        // SEÇÃO DE COMENTÁRIOS
        // =========================================================

        val cardComentarios = CardView(this).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutComentarios = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvComentariosTitulo = TextView(this).apply {
            text = "💬 Comentários"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(8))
        }
        layoutComentarios.addView(tvComentariosTitulo)

        val tvNovoComentarioRotulo = TextView(this).apply {
            text = "Comentar como ${userRepo.getUsuarioNome().ifEmpty { "Usuário" }}:"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, dpToPx(4))
        }
        layoutComentarios.addView(tvNovoComentarioRotulo)

        val inputComentario = criarCampoTexto("Escreva seu comentário sobre o evento...")
        layoutComentarios.addView(inputComentario)

        val btnPublicar = criarBotao("PUBLICAR COMENTÁRIO", primario = true) {
            val textoComentario = inputComentario.text.toString().trim()
            if (textoComentario.isEmpty()) {
                Toast.makeText(this, "Digite algo para comentar.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            userRepo.adicionarComentario(evento.id, textoComentario)
            Toast.makeText(this, "Comentário publicado!", Toast.LENGTH_SHORT).show()
            mostrarDetalhes(evento)
        }
        layoutComentarios.addView(btnPublicar)

        val comentariosLista = userRepo.getComentariosDoEvento(evento.id)

        if (comentariosLista.isEmpty()) {
            val tvSemComentarios = TextView(this).apply {
                text = "Seja o primeiro a comentar sobre este evento!"
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(16), 0, dpToPx(8))
                gravity = Gravity.CENTER
            }
            layoutComentarios.addView(tvSemComentarios)
        } else {
            val divisor = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
                ).apply {
                    setMargins(0, dpToPx(12), 0, dpToPx(12))
                }
                setBackgroundColor(AppColors.COLOR_STROKE)
            }
            layoutComentarios.addView(divisor)

            val usuarioEmailAtual = userRepo.getUsuarioEmail()

            comentariosLista.forEach { com ->
                val cardItem = CardView(this).apply {
                    radius = dpToPx(12).toFloat()
                    cardElevation = dpToPx(1).toFloat()
                    setContentPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                    useCompatPadding = true
                    setCardBackgroundColor(Color.parseColor("#F1F5F9"))
                }

                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val rowHeader = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val tvAutor = TextView(this).apply {
                    text = "👤 ${com.autorNome}"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(AppColors.COLOR_TEXT_MAIN)
                }
                rowHeader.addView(tvAutor)

                val spacer = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                }
                rowHeader.addView(spacer)

                val tvData = TextView(this).apply {
                    text = com.dataPublicacao
                    textSize = 12f
                    setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
                rowHeader.addView(tvData)

                itemLayout.addView(rowHeader)

                var emEdicao = false
                val tvTexto = TextView(this).apply {
                    text = com.texto
                    textSize = 14f
                    setTextColor(AppColors.COLOR_TEXT_MAIN)
                    setPadding(0, dpToPx(6), 0, dpToPx(8))
                }
                itemLayout.addView(tvTexto)

                val containerEdicao = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                }

                val inputEdicao = criarCampoTexto("Editar comentário...", valorInicial = com.texto)
                containerEdicao.addView(inputEdicao)

                val rowBotoesEdicao = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val btnSalvarEdicao = criarBotao("SALVAR", primario = true) {
                    val novoTexto = inputEdicao.text.toString().trim()
                    if (novoTexto.isEmpty()) {
                        Toast.makeText(this@MainActivity, "O comentário não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                        return@criarBotao
                    }
                    userRepo.atualizarComentario(com.id, novoTexto)
                    Toast.makeText(this@MainActivity, "Comentário atualizado!", Toast.LENGTH_SHORT).show()
                    mostrarDetalhes(evento)
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(42), 1f).apply {
                        setMargins(0, 0, dpToPx(4), 0)
                    }
                }
                rowBotoesEdicao.addView(btnSalvarEdicao)

                val btnCancelarEdicao = criarBotao("CANCELAR", primario = false) {
                    emEdicao = false
                    containerEdicao.visibility = View.GONE
                    tvTexto.visibility = View.VISIBLE
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(42), 1f).apply {
                        setMargins(dpToPx(4), 0, 0, 0)
                    }
                }
                rowBotoesEdicao.addView(btnCancelarEdicao)

                containerEdicao.addView(rowBotoesEdicao)
                itemLayout.addView(containerEdicao)

                if (com.autorEmail.equals(usuarioEmailAtual, ignoreCase = true) && usuarioEmailAtual.isNotEmpty()) {
                    val rowAcoes = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.END
                    }

                    val btnEditar = TextView(this).apply {
                        text = "✏️ Editar"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AppColors.COLOR_PRIMARY_LIGHT)
                        setPadding(dpToPx(8), dpToPx(4), dpToPx(12), dpToPx(4))
                        setOnClickListener {
                            emEdicao = !emEdicao
                            if (emEdicao) {
                                tvTexto.visibility = View.GONE
                                containerEdicao.visibility = View.VISIBLE
                            } else {
                                tvTexto.visibility = View.VISIBLE
                                containerEdicao.visibility = View.GONE
                            }
                        }
                    }
                    rowAcoes.addView(btnEditar)

                    val btnExcluir = TextView(this).apply {
                        text = "🗑️ Excluir"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.parseColor("#DC2626"))
                        setPadding(dpToPx(8), dpToPx(4), dpToPx(4), dpToPx(4))
                        setOnClickListener {
                            userRepo.deletarComentario(com.id)
                            Toast.makeText(this@MainActivity, "Comentário excluído.", Toast.LENGTH_SHORT).show()
                            mostrarDetalhes(evento)
                        }
                    }
                    rowAcoes.addView(btnExcluir)

                    itemLayout.addView(rowAcoes)
                }

                cardItem.addView(itemLayout)

                val paramsItem = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), 0, dpToPx(6))
                }

                layoutComentarios.addView(cardItem, paramsItem)
            }
        }

        cardComentarios.addView(layoutComentarios)

        val paramsCardComentarios = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dpToPx(12), 0, dpToPx(16))
        }

        layout.addView(cardComentarios, paramsCardComentarios)

        layout.addView(criarBotao("VOLTAR", primario = false) {
            mostrarHome()
        })
    }

    // =========================================================
    // TELA MEUS FAVORITOS
    // =========================================================

    private fun mostrarFavoritos() {
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "FAVORITOS")

        layout.addView(criarHeaderBar("Meus Favoritos", "Eventos que você salvou para acompanhar."))

        val favoritosIds = userRepo.getFavoritos()
        val favoritosLista = EventRepository.listaEventos.filter { favoritosIds.contains(it.id.toString()) }

        if (favoritosLista.isEmpty()) {
            val tvVazio = TextView(this).apply {
                text = "Você ainda não favoritou nenhum evento.\nClique no ❤️ dos eventos para salvar aqui!"
                textSize = 15f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(30), 0, dpToPx(20))
                gravity = Gravity.CENTER
            }
            layout.addView(tvVazio)

            layout.addView(criarBotao("EXPLORAR EVENTOS DISPONÍVEIS") {
                mostrarHome()
            })
        } else {
            favoritosLista.forEach { evento ->
                adicionarCardEvento(layout, evento)
            }
        }
    }

    // =========================================================
    // TELA MEUS EVENTOS
    // =========================================================

    private fun mostrarMeusEventos() {
        val (_, layout) = criarTelaBase(exibirBottomNav = true, abaAtiva = "MEUS_EVENTOS")

        layout.addView(criarHeaderBar("Minhas Inscrições", "Acompanhe todos os eventos em que você se inscreveu."))

        val inscritosIds = userRepo.getInscricoes()
        val meusEventosLista = EventRepository.listaEventos.filter { inscritosIds.contains(it.id.toString()) }

        if (meusEventosLista.isEmpty()) {
            val tvVazio = TextView(this).apply {
                text = "Você ainda não está inscrito em nenhum evento."
                textSize = 15f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(30), 0, dpToPx(20))
                gravity = Gravity.CENTER
            }
            layout.addView(tvVazio)

            layout.addView(criarBotao("EXPLORAR EVENTOS DISPONÍVEIS") {
                mostrarHome()
            })
        } else {
            meusEventosLista.forEach { evento ->
                adicionarCardEvento(layout, evento)
            }
        }
    }

    // =========================================================
    // TELA DE PERFIL
    // =========================================================

    private fun mostrarPerfil() {
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

        layout.addView(criarBotao("SAIR DA CONTA", primario = false) {
            deslogarUsuario()
        })
    }

    // =========================================================
    // TELA DE EDITAR PERFIL
    // =========================================================

    private fun mostrarEditarPerfil() {
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
