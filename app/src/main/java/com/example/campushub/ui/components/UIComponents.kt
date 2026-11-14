package com.example.campushub.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton

object UIComponents {

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun criarShape(
        context: Context,
        bgColor: Int,
        strokeColor: Int = Color.TRANSPARENT,
        strokeWidthDp: Int = 0,
        cornerRadiusDp: Float = 0f
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(bgColor)
            if (strokeWidthDp > 0) {
                setStroke(dpToPx(context, strokeWidthDp), strokeColor)
            }
            if (cornerRadiusDp > 0f) {
                cornerRadius = dpToPx(context, cornerRadiusDp.toInt()).toFloat()
            }
        }
    }

    fun criarRotulo(context: Context, texto: String): TextView {
        return TextView(context).apply {
            this.text = texto
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(dpToPx(context, 2), dpToPx(context, 12), 0, dpToPx(context, 6))
        }
    }

    fun criarCampoTexto(
        context: Context,
        hint: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        valorInicial: String = ""
    ): EditText {
        return EditText(context).apply {
            this.hint = hint
            this.textSize = 15f
            this.inputType = inputType
            if (valorInicial.isNotEmpty()) {
                setText(valorInicial)
            }
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setHintTextColor(Color.parseColor("#94A3B8"))
            setPadding(dpToPx(context, 16), dpToPx(context, 14), dpToPx(context, 16), dpToPx(context, 14))
            background = criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 8f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(context, 8))
            }
        }
    }

    fun criarButtonInstance(context: Context): Button {
        return try {
            MaterialButton(context)
        } catch (_: Throwable) {
            Button(context)
        }
    }

    fun criarCardView(
        context: Context,
        radiusDp: Float = 10f,
        elevationDp: Float = 1f,
        paddingDp: Int = 16,
        bgColor: Int = AppColors.COLOR_CARD_BG
    ): FrameLayout {
        return try {
            CardView(context).apply {
                radius = dpToPx(context, radiusDp.toInt()).toFloat()
                cardElevation = dpToPx(context, elevationDp.toInt()).toFloat()
                setContentPadding(
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp)
                )
                useCompatPadding = true
                setCardBackgroundColor(bgColor)
            }
        } catch (_: Throwable) {
            FrameLayout(context).apply {
                background = criarShape(context, bgColor, AppColors.COLOR_STROKE, 1, radiusDp)
                setPadding(
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp),
                    dpToPx(context, paddingDp)
                )
            }
        }
    }

    fun criarBotao(
        context: Context,
        texto: String,
        primario: Boolean = true,
        perigo: Boolean = false,
        acao: () -> Unit
    ): Button {
        return criarButtonInstance(context).apply {
            this.text = texto
            setOnClickListener { acao() }
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            backgroundTintList = null

            if (perigo) {
                background = criarShape(context, Color.parseColor("#FEF2F2"), Color.parseColor("#FCA5A5"), 1, 8f)
                setTextColor(Color.parseColor("#DC2626"))
            } else if (!primario) {
                background = criarShape(context, Color.TRANSPARENT, AppColors.COLOR_STROKE, 1, 8f)
                setTextColor(AppColors.COLOR_TEXT_MAIN)
            } else {
                background = criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 8f)
                setTextColor(Color.WHITE)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 42)
            ).apply {
                setMargins(0, dpToPx(context, 4), 0, dpToPx(context, 4))
            }
        }
    }

    fun criarHeaderBar(
        context: Context,
        titulo: String,
        subtitulo: String? = null,
        onVoltar: (() -> Unit)? = null
    ): View {
        val header = LinearLayout(context).apply {
            tag = "HEADER_BAR"
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppColors.COLOR_CARD_BG)
            setPadding(dpToPx(context, 16), dpToPx(context, 44), dpToPx(context, 16), dpToPx(context, 16))
            elevation = dpToPx(context, 2).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(context, 8))
            }
        }

        val rowTop = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (onVoltar != null) {
            val btnVoltar = ImageView(context).apply {
                setImageResource(context.resources.getIdentifier("ic_arrow_back", "drawable", context.packageName))
                setColorFilter(AppColors.COLOR_TEXT_MAIN)
                setPadding(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8))
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(context, 40),
                    dpToPx(context, 40)
                ).apply {
                    setMargins(0, 0, dpToPx(context, 8), 0)
                }
                background = criarShape(context, Color.TRANSPARENT, Color.TRANSPARENT, 0, 20f)
                setOnClickListener { onVoltar() }
            }
            rowTop.addView(btnVoltar)
        }

        val tvLogo = TextView(context).apply {
            text = "CampusHub"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_PRIMARY)
        }
        rowTop.addView(tvLogo)

        header.addView(rowTop)

        val tvTitulo = TextView(context).apply {
            text = titulo
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, dpToPx(context, 12), 0, 0)
        }
        header.addView(tvTitulo)

        if (subtitulo != null) {
            val tvSub = TextView(context).apply {
                text = subtitulo
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(context, 4), 0, 0)
            }
            header.addView(tvSub)
        }

        return header
    }

    fun criarBottomNav(
        context: Context,
        abaAtiva: String,
        onNavigate: (String) -> Unit
    ): View {
        val navContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(AppColors.COLOR_CARD_BG)
            elevation = dpToPx(context, 12).toFloat()
            background = criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 0f)
            setPadding(0, dpToPx(context, 6), 0, dpToPx(context, 22))
            weightSum = 4f
        }

        fun criarItemNav(iconeRes: String, rotulo: String, abaId: String) {
            val ehAtivo = (abaId == abaAtiva)

            val item = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(context, 4), 0, dpToPx(context, 2))
                setOnClickListener {
                    if (abaId != abaAtiva) onNavigate(abaId)
                }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
            }

            val iconContainer = FrameLayout(context).apply {
                if (ehAtivo) {
                    background = criarShape(context, AppColors.COLOR_PRIMARY_LIGHT, Color.TRANSPARENT, 0, 12f)
                }
                setPadding(dpToPx(context, 12), dpToPx(context, 4), dpToPx(context, 12), dpToPx(context, 4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(context, 2))
                }
            }

            val ivIcone = ImageView(context).apply {
                val resId = context.resources.getIdentifier(iconeRes, "drawable", context.packageName)
                setImageResource(resId)
                val iconColor = if (ehAtivo) AppColors.COLOR_PRIMARY else AppColors.COLOR_TEXT_MUTED
                setColorFilter(iconColor)
                layoutParams = FrameLayout.LayoutParams(dpToPx(context, 22), dpToPx(context, 22), Gravity.CENTER)
            }
            iconContainer.addView(ivIcone)
            item.addView(iconContainer)

            val tvTexto = TextView(context).apply {
                text = rotulo
                textSize = 11f
                includeFontPadding = false
                typeface = if (ehAtivo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (ehAtivo) AppColors.COLOR_TEXT_MAIN else AppColors.COLOR_TEXT_MUTED)
                gravity = Gravity.CENTER
            }
            item.addView(tvTexto)

            navContainer.addView(item)
        }

        criarItemNav("ic_home", "Início", "HOME")
        criarItemNav("ic_favorite", "Favoritos", "FAVORITOS")
        criarItemNav("ic_calendar", "Inscrições", "MEUS_EVENTOS")
        criarItemNav("ic_person", "Perfil", "PERFIL")

        return navContainer
    }

    fun criarTelaBase(
        context: Context,
        exibirBottomNav: Boolean = true,
        abaAtiva: String = "HOME",
        restoreScrollY: Int = 0,
        onNavigate: (String) -> Unit = {}
    ): Pair<FrameLayout, LinearLayout> {
        val root = FrameLayout(context).apply {
            setBackgroundColor(AppColors.COLOR_BG)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                if (exibirBottomNav) {
                    setMargins(0, 0, 0, dpToPx(context, 90))
                }
            }
        }

        val containerRoot = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val bodyLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(context, 20), dpToPx(context, 8), dpToPx(context, 20), dpToPx(context, 50))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val layoutDelegado = object : LinearLayout(context) {
            private fun removerHeaderAnterior() {
                for (i in containerRoot.childCount - 1 downTo 0) {
                    val child = containerRoot.getChildAt(i)
                    if (child?.tag == "HEADER_BAR") {
                        containerRoot.removeViewAt(i)
                    }
                }
            }

            override fun addView(child: View?) {
                if (child?.tag == "HEADER_BAR") {
                    removerHeaderAnterior()
                    containerRoot.addView(child, 0)
                } else {
                    bodyLayout.addView(child)
                }
            }

            override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
                if (child?.tag == "HEADER_BAR") {
                    removerHeaderAnterior()
                    containerRoot.addView(child, 0, params)
                } else {
                    bodyLayout.addView(child, params)
                }
            }

            override fun removeAllViews() {
                bodyLayout.removeAllViews()
                removerHeaderAnterior()
            }
        }

        containerRoot.addView(bodyLayout)
        scrollView.addView(containerRoot)
        root.addView(scrollView)

        if (restoreScrollY > 0) {
            scrollView.post {
                scrollView.scrollTo(0, restoreScrollY)
            }
        }

        if (exibirBottomNav) {
            val bottomNav = criarBottomNav(context, abaAtiva, onNavigate)
            val navParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 90)
            ).apply {
                gravity = Gravity.BOTTOM
            }
            root.addView(bottomNav, navParams)
        }

        return Pair(root, layoutDelegado)
    }
}
