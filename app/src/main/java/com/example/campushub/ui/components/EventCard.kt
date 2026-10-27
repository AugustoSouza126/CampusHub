package com.example.campushub.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.theme.AppColors
import java.util.Locale

/**
 * Componente responsável por construir o Card de Evento reutilizável nas telas
 */
object EventCard {

    /**
     * Constrói e adiciona um [CardView] representando um evento no layout pai fornecido.
     */
    fun adicionarCardEvento(
        context: Context,
        parent: LinearLayout,
        evento: Event,
        userRepo: UserPreferencesRepository,
        onVerDetalhes: (Event) -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        val card = CardView(context).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutInterno = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Cabeçalho do Card (Categoria + Badge Data + Heart Favorito)
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
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
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
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
        }
        rowTop.addView(tvDataBadge)

        val tvHeart = TextView(context).apply {
            text = if (userRepo.isFavorito(evento.id)) " ❤️" else " 🤍"
            textSize = 18f
            setPadding(dpToPx(8), 0, 0, 0)
            setOnClickListener {
                if (userRepo.isFavorito(evento.id)) {
                    userRepo.removerFavorito(evento.id)
                    text = " 🤍"
                    Toast.makeText(context, "Removido dos favoritos.", Toast.LENGTH_SHORT).show()
                } else {
                    userRepo.salvarFavorito(evento.id)
                    text = " ❤️"
                    Toast.makeText(context, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rowTop.addView(tvHeart)

        layoutInterno.addView(rowTop)

        // Título do Evento
        val tvTitulo = TextView(context).apply {
            text = evento.titulo
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, dpToPx(10), 0, dpToPx(4))
        }
        layoutInterno.addView(tvTitulo)

        // Badges do evento (Encerrado, Inscrito e Nota/Rating)
        val rowBadges = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (evento.encerrado) {
            val tvEncerrado = TextView(context).apply {
                text = "🏁 ENCERRADO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#475569"))
                background = UIComponents.criarShape(context, Color.parseColor("#E2E8F0"), Color.TRANSPARENT, 0, 6f)
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
            val tvBadge = TextView(context).apply {
                text = "✅ INSCRITO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_GREEN_TEXT)
                background = UIComponents.criarShape(context, AppColors.COLOR_GREEN_BG, Color.TRANSPARENT, 0, 6f)
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
            val tvMedia = TextView(context).apply {
                text = String.format(Locale.getDefault(), "⭐ %.1f (%d)", media, total)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                background = UIComponents.criarShape(context, Color.parseColor("#FEF3C7"), Color.TRANSPARENT, 0, 6f)
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

        // Local, Horário e Endereço
        val tvInfo = TextView(context).apply {
            val localTexto = if (evento.endereco.isNotEmpty()) {
                "🕒 ${evento.horario}  •  📍 ${evento.local}\n🏠 ${evento.endereco}"
            } else {
                "🕒 ${evento.horario}  •  📍 ${evento.local}"
            }
            text = localTexto
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, dpToPx(2), 0, dpToPx(8))
        }
        layoutInterno.addView(tvInfo)

        // Descrição resumida
        val tvDesc = TextView(context).apply {
            text = evento.descricao
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(12))
        }
        layoutInterno.addView(tvDesc)

        // Botão Ver Detalhes
        val btnDetalhes = UIComponents.criarBotao(context, "VER DETALHES", primario = true) {
            onVerDetalhes(evento)
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
}
