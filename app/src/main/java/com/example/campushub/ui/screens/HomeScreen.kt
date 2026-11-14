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
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.EventCard
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton

object HomeScreen {

    fun construirTelaHome(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onCriarNovoEvento: () -> Unit,
        onVerEventosCriados: () -> Unit,
        onVerDetalhes: (Event) -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        val nome = userRepo.getUsuarioNome()
        val saudacao = if (nome.isNotEmpty()) "Olá, $nome!" else "Olá!"

        layout.addView(UIComponents.criarHeaderBar(context, saudacao, "Confira os eventos da universidade e garanta sua vaga."))

        if (userRepo.isOrganizador()) {
            layout.addView(UIComponents.criarBotao(context, "CRIAR NOVO EVENTO", primario = true) {
                onCriarNovoEvento()
            })

            layout.addView(UIComponents.criarBotao(context, "EVENTOS CRIADOS POR MIM", primario = false) {
                onVerEventosCriados()
            })
        }

        var termoBusca = ""
        var situacaoSelecionada = "Todos"
        var categoriaSelecionada = "Todas"
        var ordenacaoSelecionada = "Data"

        val containerEventos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

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

                    val ordenados = when (ordenacaoSelecionada) {
                        "Por título" -> filtrados.sortedBy { it.titulo }
                        "Por nota/avaliação" -> filtrados.sortedByDescending { userRepo.getMediaAvaliacoes(it.id).first }
                        else -> filtrados
                    }

                    if (ordenados.isEmpty()) {
                        val tvVazio = TextView(context).apply {
                            text = "Nenhum evento encontrado com os filtros selecionados."
                            textSize = 15f
                            setTextColor(AppColors.COLOR_TEXT_MUTED)
                            setPadding(0, dpToPx(20), 0, dpToPx(20))
                            gravity = Gravity.CENTER
                        }
                        containerEventos.addView(tvVazio)
                    } else {
                        ordenados.forEach { evento ->
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

        val rowBuscaFiltro = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val inputBusca = UIComponents.criarCampoTexto(context, "Digite o título do evento...").apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, dpToPx(8), 0)
            }
        }
        rowBuscaFiltro.addView(inputBusca)

        val btnAbrirFiltros = android.widget.ImageView(context).apply {
            setImageResource(context.resources.getIdentifier("ic_filter", "drawable", context.packageName))
            setColorFilter(Color.WHITE)
            background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 8f)
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            layoutParams = LinearLayout.LayoutParams(dpToPx(42), dpToPx(42))
            setOnClickListener {
                exibirBottomSheetFiltros(
                    context = context,
                    categoriaAtual = categoriaSelecionada,
                    situacaoAtual = situacaoSelecionada,
                    ordenacaoAtual = ordenacaoSelecionada,
                    onAplicar = { cat, sit, ord ->
                        categoriaSelecionada = cat
                        situacaoSelecionada = sit
                        ordenacaoSelecionada = ord
                        aplicarFiltros()
                    }
                )
            }
        }
        rowBuscaFiltro.addView(btnAbrirFiltros)

        layout.addView(UIComponents.criarRotulo(context, "Pesquisar e Filtrar"))
        layout.addView(rowBuscaFiltro)

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

    private fun exibirBottomSheetFiltros(
        context: Context,
        categoriaAtual: String,
        situacaoAtual: String,
        ordenacaoAtual: String,
        onAplicar: (String, String, String) -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(context)
        val view = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(24))
            setBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val tvTitulo = TextView(context).apply {
            text = "Filtros e Ordenação"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(16))
        }
        view.addView(tvTitulo)

        var tempCat = categoriaAtual
        var tempSit = situacaoAtual
        var tempOrd = ordenacaoAtual

        view.addView(UIComponents.criarRotulo(context, "Categoria"))
        val spinnerCat = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Todas", "Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança")
            )
            val index = listOf("Todas", "Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança").indexOf(tempCat)
            if (index >= 0) setSelection(index)
        }
        view.addView(spinnerCat)

        view.addView(UIComponents.criarRotulo(context, "Situação"))
        val spinnerSit = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Todos", "Próximos", "Encerrados")
            )
            val index = listOf("Todos", "Próximos", "Encerrados").indexOf(tempSit)
            if (index >= 0) setSelection(index)
        }
        view.addView(spinnerSit)

        view.addView(UIComponents.criarRotulo(context, "Ordenar por"))
        val spinnerOrd = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Data", "Por título", "Por nota/avaliação")
            )
            val index = listOf("Data", "Por título", "Por nota/avaliação").indexOf(tempOrd)
            if (index >= 0) setSelection(index)
        }
        view.addView(spinnerOrd)

        val rowBotoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(20), 0, 0)
        }

        val btnLimpar = UIComponents.criarBotao(context, "Limpar Filtros", primario = false) {
            onAplicar("Todas", "Todos", "Data")
            bottomSheet.dismiss()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(42), 1f).apply {
                setMargins(0, 0, dpToPx(4), 0)
            }
        }
        rowBotoes.addView(btnLimpar)

        val btnAplicar = UIComponents.criarBotao(context, "Aplicar Filtros", primario = true) {
            onAplicar(
                spinnerCat.selectedItem.toString(),
                spinnerSit.selectedItem.toString(),
                spinnerOrd.selectedItem.toString()
            )
            bottomSheet.dismiss()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(42), 1f).apply {
                setMargins(dpToPx(4), 0, 0, 0)
            }
        }
        rowBotoes.addView(btnAplicar)

        view.addView(rowBotoes)

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }
}
