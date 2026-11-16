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

object EventCard {

    fun adicionarCardEvento(
        context: Context,
        parent: LinearLayout,
        evento: Event,
        userRepo: UserPreferencesRepository,
        onVerDetalhes: (Event) -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        val card = UIComponents.criarCardView(context, 10f, 0f, 16, AppColors.COLOR_CARD_BG)
        (card as? CardView)?.let {
            it.cardElevation = dpToPx(1).toFloat()
            it.background = UIComponents.criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 10f)
        }

        val layoutInterno = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

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
            background = UIComponents.criarShape(context, AppColors.COLOR_CHIP_BG, Color.TRANSPARENT, 0, 6f)
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
        }
        rowTop.addView(tvCategoria)

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }
        rowTop.addView(spacer)

        val ivCalendar = android.widget.ImageView(context).apply {
            setImageResource(context.resources.getIdentifier("ic_calendar", "drawable", context.packageName))
            setColorFilter(AppColors.COLOR_PRIMARY)
            layoutParams = LinearLayout.LayoutParams(dpToPx(14), dpToPx(14)).apply {
                setMargins(0, 0, dpToPx(4), 0)
            }
        }
        val rowDate = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY_LIGHT, Color.TRANSPARENT, 0, 6f)
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
        }
        rowDate.addView(ivCalendar)
        
        val tvDataBadge = TextView(context).apply {
            text = evento.dataBadge
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_PRIMARY)
        }
        rowDate.addView(tvDataBadge)
        rowTop.addView(rowDate)

        val ivHeart = android.widget.ImageView(context).apply {
            val resName = if (userRepo.isFavorito(evento.id)) "ic_favorite" else "ic_favorite_border"
            setImageResource(context.resources.getIdentifier(resName, "drawable", context.packageName))
            setColorFilter(if (userRepo.isFavorito(evento.id)) Color.parseColor("#E11D48") else AppColors.COLOR_TEXT_MUTED)
            layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                setMargins(dpToPx(12), 0, 0, 0)
            }
            setOnClickListener {
                animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
                    animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                }.start()
                if (userRepo.isFavorito(evento.id)) {
                    userRepo.removerFavorito(evento.id)
                    setImageResource(context.resources.getIdentifier("ic_favorite_border", "drawable", context.packageName))
                    setColorFilter(AppColors.COLOR_TEXT_MUTED)
                    Toast.makeText(context, "Removido dos favoritos.", Toast.LENGTH_SHORT).show()
                } else {
                    userRepo.salvarFavorito(evento.id)
                    setImageResource(context.resources.getIdentifier("ic_favorite", "drawable", context.packageName))
                    setColorFilter(Color.parseColor("#E11D48"))
                    Toast.makeText(context, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rowTop.addView(ivHeart)

        layoutInterno.addView(rowTop)

        if (evento.imagemUrl.isNotEmpty()) {
            val ivCapa = android.widget.ImageView(context).apply {
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                visibility = View.GONE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(140)
                ).apply {
                    setMargins(0, dpToPx(8), 0, dpToPx(4))
                }
            }
            MediaHelper.carregarImagemParaImageView(ivCapa, evento.imagemUrl)
            layoutInterno.addView(ivCapa)
        }

        val tvTitulo = TextView(context).apply {
            text = evento.titulo
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, dpToPx(10), 0, dpToPx(4))
        }
        layoutInterno.addView(tvTitulo)

        val rowBadges = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (evento.encerrado) {
            val tvEncerrado = TextView(context).apply {
                text = "ENCERRADO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_ON_SURFACE_VARIANT)
                background = UIComponents.criarShape(context, AppColors.COLOR_SURFACE_VARIANT, Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), dpToPx(8), dpToPx(8))
                }
            }
            rowBadges.addView(tvEncerrado)
        }

        if (userRepo.isInscrito(evento.id)) {
            val tvBadge = TextView(context).apply {
                text = "INSCRITO"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_GREEN_TEXT)
                background = UIComponents.criarShape(context, AppColors.COLOR_GREEN_BG, Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), dpToPx(8), dpToPx(8))
                }
            }
            rowBadges.addView(tvBadge)
        } else if (userRepo.isNaListaEspera(evento.id)) {
            val posicao = userRepo.getPosicaoListaEspera(evento.id)
            val tvBadgeEspera = TextView(context).apply {
                text = "LISTA DE ESPERA (#$posicao)"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#B45309"))
                background = UIComponents.criarShape(context, Color.parseColor("#FEF3C7"), Color.TRANSPARENT, 0, 6f)
                setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), dpToPx(8), dpToPx(8))
                }
            }
            rowBadges.addView(tvBadgeEspera)
        } else {
            val totalInscritos = userRepo.getTotalInscritos(evento.id)
            if (totalInscritos >= evento.vagas && evento.vagas > 0) {
                val tvBadgeEsgotado = TextView(context).apply {
                    text = "VAGAS ESGOTADAS"
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#B91C1C"))
                    background = UIComponents.criarShape(context, Color.parseColor("#FEE2E2"), Color.TRANSPARENT, 0, 6f)
                    setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, dpToPx(4), dpToPx(8), dpToPx(8))
                    }
                }
                rowBadges.addView(tvBadgeEsgotado)
            }
        }

        val (media, total) = userRepo.getMediaAvaliacoes(evento.id)
        if (total > 0) {
            val rowRating = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = UIComponents.criarShape(context, Color.parseColor("#FEF3C7"), Color.parseColor("#FDE68A"), 1, 6f)
                setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), 0, dpToPx(8))
                }
            }
            val ivStar = android.widget.ImageView(context).apply {
                setImageResource(context.resources.getIdentifier("ic_star", "drawable", context.packageName))
                setColorFilter(Color.parseColor("#D97706"))
                layoutParams = LinearLayout.LayoutParams(dpToPx(14), dpToPx(14)).apply {
                    setMargins(0, 0, dpToPx(4), 0)
                }
            }
            val tvMedia = TextView(context).apply {
                text = String.format(Locale.getDefault(), "%.1f (%d)", media, total)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
            }
            rowRating.addView(ivStar)
            rowRating.addView(tvMedia)
            rowBadges.addView(rowRating)
        }

        layoutInterno.addView(rowBadges)

        val rowInfo1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dpToPx(4))
        }
        val ivTime = android.widget.ImageView(context).apply {
            setImageResource(context.resources.getIdentifier("ic_time", "drawable", context.packageName))
            setColorFilter(AppColors.COLOR_TEXT_MUTED)
            layoutParams = LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)).apply {
                setMargins(0, 0, dpToPx(6), 0)
            }
        }
        val tvHorario = TextView(context).apply {
            text = evento.horario
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
        }
        rowInfo1.addView(ivTime)
        rowInfo1.addView(tvHorario)
        
        val ivLocation = android.widget.ImageView(context).apply {
            setImageResource(context.resources.getIdentifier("ic_location", "drawable", context.packageName))
            setColorFilter(AppColors.COLOR_TEXT_MUTED)
            layoutParams = LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)).apply {
                setMargins(dpToPx(16), 0, dpToPx(6), 0)
            }
        }
        val tvLocal = TextView(context).apply {
            text = evento.local
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
        }
        rowInfo1.addView(ivLocation)
        rowInfo1.addView(tvLocal)
        
        layoutInterno.addView(rowInfo1)

        if (evento.endereco.isNotEmpty()) {
            val tvEndereco = TextView(context).apply {
                text = evento.endereco
                textSize = 13f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(dpToPx(22), 0, 0, dpToPx(8))
            }
            layoutInterno.addView(tvEndereco)
        }

        val tvDesc = TextView(context).apply {
            text = evento.descricao
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, dpToPx(12))
        }
        layoutInterno.addView(tvDesc)

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

        card.alpha = 0f
        card.translationY = 40f
        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(280)
            .start()
    }
}
