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

object MyCreatedEventsScreen {

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

        // Categoria & Badges
        val rowTop = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
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

        if (evento.encerrado) {
            val tvEncerrado = TextView(context).apply {
                text = "🏁 ENCERRADO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#475569"))
                background = UIComponents.criarShape(context, Color.parseColor("#E2E8F0"), Color.TRANSPARENT, 0, 6f)
                setPadding(UIComponents.dpToPx(context, 8), UIComponents.dpToPx(context, 3), UIComponents.dpToPx(context, 8), UIComponents.dpToPx(context, 3))
            }
            rowTop.addView(tvEncerrado)
        }

        layoutInterno.addView(rowTop)

        // Título
        val tvTitulo = TextView(context).apply {
            text = evento.titulo
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, UIComponents.dpToPx(context, 8), 0, UIComponents.dpToPx(context, 4))
        }
        layoutInterno.addView(tvTitulo)

        // Info
        val tvInfo = TextView(context).apply {
            text = "📅 ${evento.data} • 🕒 ${evento.horario}\n📍 ${evento.local} • 👥 ${evento.vagas} vagas"
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 12))
        }
        layoutInterno.addView(tvInfo)

        // PAINEL DE AÇÕES DO CRIADOR (EDITAR, ENCERRAR, EXCLUIR)
        val rowAcoes = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }

        val btnVerDetalhes = UIComponents.criarBotao(context, "🔍 VER", primario = false) {
            onVerDetalhes(evento)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                setMargins(0, 0, UIComponents.dpToPx(context, 2), 0)
            }
        }
        rowAcoes.addView(btnVerDetalhes)

        val btnEditar = UIComponents.criarBotao(context, "✏️ EDITAR", primario = true) {
            onEditarEvento(evento)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                setMargins(UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 2), 0)
            }
        }
        rowAcoes.addView(btnEditar)

        if (!evento.encerrado) {
            val btnEncerrar = UIComponents.criarBotao(context, "🏁 ENCERRAR", primario = false) {
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
                    setMargins(UIComponents.dpToPx(context, 2), 0, 0, 0)
                }
            }
            rowAcoes.addView(btnEncerrar)
        } else {
            val btnExcluir = UIComponents.criarBotao(context, "🗑️ EXCLUIR", primario = false, perigo = true) {
                EventRepository.deletarEvento(
                    evento.id,
                    onSuccess = {
                        Toast.makeText(context, "Evento excluído do Firebase!", Toast.LENGTH_SHORT).show()
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
            rowAcoes.addView(btnExcluir)
        }

        layoutInterno.addView(rowAcoes)

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
