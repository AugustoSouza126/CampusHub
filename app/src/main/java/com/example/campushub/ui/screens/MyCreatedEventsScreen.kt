package com.example.campushub.ui.screens

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import java.util.Locale

/**
 * Responsável por construir a Tela de Eventos Criados pelo Usuário.
 * Filtra no Firebase e exibe os eventos publicados com o e-mail do usuário logado,
 */
object MyCreatedEventsScreen {

    /**
     * Constrói e exibe a lista de eventos criados pelo usuário atual.
     */
    fun construirTelaEventosCriados(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onEditarEvento: (Evento) -> Unit,
        onCriarNovoEvento: () -> Unit,
        onVerDetalhes: (Evento) -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Eventos Criados por Mim", "Gerencie os eventos que você publicou no campus."))

        val containerLista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(containerLista)

        // Carrega a lista de eventos filtrados por e-mail do criador
        fun carregarLista() {
            containerLista.removeAllViews()

            val tvCarregando = TextView(context).apply {
                text = "Carregando seus eventos..."
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 20), 0, UIComponents.dpToPx(context, 20))
                gravity = Gravity.CENTER
            }
            containerLista.addView(tvCarregando)

            val meuEmail = userRepo.getUsuarioEmail()

            EventRepository.getEventos(
                onSuccess = { lista ->
                    containerLista.removeAllViews()
                    val meusEventosCriados = lista.filter {
                        it.criadorEmail.equals(meuEmail, ignoreCase = true) && meuEmail.isNotEmpty()
                    }

                    if (meusEventosCriados.isEmpty()) {
                        val tvVazio = TextView(context).apply {
                            text = "Você ainda não criou nenhum evento no aplicativo."
                            textSize = 15f
                            setTextColor(AppColors.COLOR_TEXT_MUTED)
                            setPadding(0, UIComponents.dpToPx(context, 20), 0, UIComponents.dpToPx(context, 20))
                            gravity = Gravity.CENTER
                        }
                        containerLista.addView(tvVazio)

                        containerLista.addView(UIComponents.criarBotao(context, "➕ CRIAR NOVO EVENTO", primario = true) {
                            onCriarNovoEvento()
                        })
                    } else {
                        containerLista.addView(UIComponents.criarBotao(context, "➕ CRIAR NOVO EVENTO", primario = true) {
                            onCriarNovoEvento()
                        })

                        meusEventosCriados.forEach { evento ->
                            adicionarCardCriador(context, containerLista, evento, userRepo, onEditarEvento, onVerDetalhes) {
                                carregarLista()
                            }
                        }
                    }
                },
                onError = { error ->
                    containerLista.removeAllViews()
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        carregarLista()
    }

    /**
     * Constrói o Card específico com botões de gerenciamento (Editar, Encerrar, Excluir)
     */
    private fun adicionarCardCriador(
        context: Context,
        parent: LinearLayout,
        evento: Evento,
        userRepo: UserPreferencesRepository,
        onEditarEvento: (Evento) -> Unit,
        onVerDetalhes: (Evento) -> Unit,
        onAtualizarLista: () -> Unit
    ) {
        val card = CardView(context).apply {
            radius = UIComponents.dpToPx(context, 16).toFloat()
            cardElevation = UIComponents.dpToPx(context, 4).toFloat()
            setContentPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutInterno = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Cabeçalho
        val rowTop = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            weightSum = 1f
        }

        val tvCategoria = TextView(context).apply {
            text = evento.categoria.uppercase(Locale.getDefault())
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_CHIP_TEXT)
            background = UIComponents.criarShape(context, AppColors.COLOR_CHIP_BG, Color.TRANSPARENT, 0, 8f)
            setPadding(UIComponents.dpToPx(context, 10), UIComponents.dpToPx(context, 4), UIComponents.dpToPx(context, 10), UIComponents.dpToPx(context, 4))
        }
        rowTop.addView(tvCategoria)

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }
        rowTop.addView(spacer)

        val tvDataBadge = TextView(context).apply {
            text = "📅 ${evento.dataBadge}"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 8f)
            setPadding(UIComponents.dpToPx(context, 10), UIComponents.dpToPx(context, 4), UIComponents.dpToPx(context, 10), UIComponents.dpToPx(context, 4))
        }
        rowTop.addView(tvDataBadge)

        layoutInterno.addView(rowTop)

        // Título
        val tvTitulo = TextView(context).apply {
            text = evento.titulo
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, UIComponents.dpToPx(context, 10), 0, UIComponents.dpToPx(context, 4))
        }
        layoutInterno.addView(tvTitulo)

        // Local e Horário
        val tvInfo = TextView(context).apply {
            text = "🕒 ${evento.horario}  •  📍 ${evento.local}"
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 8))
        }
        layoutInterno.addView(tvInfo)

        // Botoes de Ação Rápida (Ver, Editar, Encerrar, Excluir)
        val rowBotoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
        }

        val btnVer = UIComponents.criarBotao(context, "👁️", primario = true) {
            onVerDetalhes(evento)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                setMargins(0, 0, UIComponents.dpToPx(context, 2), 0)
            }
        }
        rowBotoes.addView(btnVer)

        val btnEditar = UIComponents.criarBotao(context, "✏️", primario = false) {
            onEditarEvento(evento)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                setMargins(UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 2), 0)
            }
        }
        rowBotoes.addView(btnEditar)

        if (!evento.encerrado) {
            val btnEncerrar = UIComponents.criarBotao(context, "🏁", primario = false) {
                EventRepository.encerrarEvento(
                    evento.id,
                    onSuccess = {
                        Toast.makeText(context, "Evento encerrado com sucesso!", Toast.LENGTH_SHORT).show()
                        onAtualizarLista()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                    setMargins(UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 2), 0)
                }
            }
            rowBotoes.addView(btnEncerrar)
        }

        val btnExcluir = UIComponents.criarBotao(context, "🗑️", primario = false, perigo = true) {
            EventRepository.deletarEvento(
                evento.id,
                onSuccess = {
                    Toast.makeText(context, "Evento excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    onAtualizarLista()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                setMargins(UIComponents.dpToPx(context, 2), 0, 0, 0)
            }
        }
        rowBotoes.addView(btnExcluir)

        layoutInterno.addView(rowBotoes)
        card.addView(layoutInterno)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 10))
        }

        parent.addView(card, params)
    }
}
