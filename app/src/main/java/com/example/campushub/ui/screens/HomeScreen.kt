package com.example.campushub.ui.screens

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.EventCard
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton

/**
 * Responsável por construir a Tela Inicial (Home) do CampusHub.
 * Apresenta opções de navegação rápida, campo de busca por título,
 * filtros por situação (Todos/Próximos/Encerrados) e por categoria,
 * além de listar dinamicamente os eventos cadastrados no Firebase.
 */
object HomeScreen {

    /**
     * Constrói a interface visual completa da Tela Home.
     */
    fun construirTelaHome(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onCriarNovoEvento: () -> Unit,
        onVerEventosCriados: () -> Unit,
        onVerDetalhes: (Evento) -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        val nome = userRepo.getUsuarioNome()
        val saudacao = if (nome.isNotEmpty()) "Olá, $nome! 👋" else "Olá! 👋"

        layout.addView(UIComponents.criarHeaderBar(context, saudacao, "Confira os eventos da universidade e garanta sua vaga."))

        // Botões de Ação Rápida
        layout.addView(UIComponents.criarBotao(context, "➕ CRIAR NOVO EVENTO", primario = true) {
            onCriarNovoEvento()
        })

        layout.addView(UIComponents.criarBotao(context, "📢 EVENTOS CRIADOS POR MIM", primario = false) {
            onVerEventosCriados()
        })

        var termoBusca = ""
        var situacaoSelecionada = "Todos"
        var categoriaSelecionada = "Todas"

        // 1. Campo de Busca por Título
        layout.addView(UIComponents.criarRotulo(context, "Buscar por título"))
        val inputBusca = UIComponents.criarCampoTexto(context, "🔎 Digite o título do evento...")
        layout.addView(inputBusca)

        // 2. Filtro de Situação (Todos / Próximos / Encerrados)
        layout.addView(UIComponents.criarRotulo(context, "Situação do evento"))
        val rowSituacao = LinearLayout(context).apply {
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

        val containerEventos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Aplica os filtros de busca, categoria e situação à lista de eventos do Firebase
        fun aplicarFiltros() {
            containerEventos.removeAllViews()

            val tvCarregando = TextView(context).apply {
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
                        val tvVazio = TextView(context).apply {
                            text = "Nenhum evento encontrado com os filtros selecionados."
                            textSize = 15f
                            setTextColor(AppColors.COLOR_TEXT_MUTED)
                            setPadding(0, dpToPx(20), 0, dpToPx(20))
                            gravity = Gravity.CENTER
                        }
                        containerEventos.addView(tvVazio)
                    } else {
                        filtrados.forEach { evento ->
                            EventCard.adicionarCardEvento(
                                context = context,
                                parent = containerEventos,
                                evento = evento,
                                userRepo = userRepo,
                                onVerDetalhes = onVerDetalhes
                            )
                        }
                    }
                },
                onError = { error ->
                    containerEventos.removeAllViews()
                    val tvErro = TextView(context).apply {
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
                    btn.background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 10f)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.background = UIComponents.criarShape(context, Color.WHITE, AppColors.COLOR_STROKE, 1, 10f)
                    btn.setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
            }
        }

        opcoesSituacao.forEach { opcao ->
            val btn = MaterialButton(context).apply {
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

        // 3. Filtro de Categoria (Carrossel Horizontal)
        layout.addView(UIComponents.criarRotulo(context, "Categoria"))
        val scrollCategorias = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(16))
            }
        }

        val rowCategorias = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val categoriasOpcoes = listOf("Todas", "Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança")
        val botoesCategorias = mutableListOf<Button>()

        fun atualizarEstiloCategorias() {
            botoesCategorias.forEachIndexed { index, btn ->
                val cat = categoriasOpcoes[index]
                val selecionada = (cat == categoriaSelecionada)
                if (selecionada) {
                    btn.background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY_LIGHT, Color.TRANSPARENT, 0, 20f)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.background = UIComponents.criarShape(context, Color.WHITE, AppColors.COLOR_STROKE, 1, 20f)
                    btn.setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
            }
        }

        categoriasOpcoes.forEach { cat ->
            val btnCat = MaterialButton(context).apply {
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

        // Escutador de alterações no campo de busca para filtragem em tempo real
        inputBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                termoBusca = s?.toString() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Executa a filtragem inicial
        aplicarFiltros()
        layout.addView(containerEventos)
    }
}
