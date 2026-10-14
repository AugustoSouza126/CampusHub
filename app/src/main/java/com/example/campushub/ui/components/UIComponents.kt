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
            background = criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 12f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(context, 8))
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
        return MaterialButton(context).apply {
            this.text = texto
            setOnClickListener { acao() }
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false

            if (perigo) {
                background = criarShape(context, Color.parseColor("#FEF2F2"), Color.parseColor("#EF4444"), 1, 12f)
                setTextColor(Color.parseColor("#DC2626"))
            } else if (primario) {
                background = criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 12f)
                setTextColor(Color.WHITE)
            } else {
                background = criarShape(context, Color.WHITE, AppColors.COLOR_PRIMARY_LIGHT, 1, 12f)
                setTextColor(AppColors.COLOR_PRIMARY_LIGHT)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 48)
            ).apply {
                setMargins(0, dpToPx(context, 6), 0, dpToPx(context, 6))
            }
        }
    }

    fun criarHeaderBar(context: Context, titulo: String, subtitulo: String? = null): View {
        val header = LinearLayout(context).apply {
            tag = "HEADER_BAR"
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppColors.COLOR_PRIMARY)
            setPadding(dpToPx(context, 20), dpToPx(context, 52), dpToPx(context, 20), dpToPx(context, 24))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(context, 16))
            }
        }

        val tvLogo = TextView(context).apply {
            text = "🎓 CampusHub"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#93C5FD"))
            setPadding(0, 0, 0, dpToPx(context, 4))
        }
        header.addView(tvLogo)

        val tvTitulo = TextView(context).apply {
            text = titulo
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }
        header.addView(tvTitulo)

        if (subtitulo != null) {
            val tvSub = TextView(context).apply {
                text = subtitulo
                textSize = 14f
                setTextColor(Color.parseColor("#E0E7FF"))
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
            weightSum = 4f
        }

        fun criarItemNav(icone: String, rotulo: String, abaId: String) {
            val ehAtivo = (abaId == abaAtiva)

            val item = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(context, 8), 0, dpToPx(context, 8))
                setOnClickListener {
                    if (abaId != abaAtiva) onNavigate(abaId)
                }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
            }

            val tvIcone = TextView(context).apply {
                text = icone
                textSize = 18f
                gravity = Gravity.CENTER
            }
            item.addView(tvIcone)

            val tvTexto = TextView(context).apply {
                text = rotulo
                textSize = 11f
                typeface = if (ehAtivo) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (ehAtivo) AppColors.COLOR_PRIMARY_LIGHT else AppColors.COLOR_TEXT_MUTED)
                gravity = Gravity.CENTER
            }
            item.addView(tvTexto)

            navContainer.addView(item)
        }

        criarItemNav("🏠", "Início", "HOME")
        criarItemNav("❤️", "Favoritos", "FAVORITOS")
        criarItemNav("📅", "Inscrições", "MEUS_EVENTOS")
        criarItemNav("👤", "Perfil", "PERFIL")

        return navContainer
    }

    fun criarTelaBase(
        context: Context,
        exibirBottomNav: Boolean = true,
        abaAtiva: String = "HOME",
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
                    setMargins(0, 0, 0, dpToPx(context, 65))
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
            setPadding(dpToPx(context, 20), dpToPx(context, 8), dpToPx(context, 20), dpToPx(context, 30))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val layoutDelegado = object : LinearLayout(context) {
            override fun addView(child: View?) {
                if (child?.tag == "HEADER_BAR") {
                    containerRoot.addView(child, 0)
                } else {
                    bodyLayout.addView(child)
                }
            }

            override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
                if (child?.tag == "HEADER_BAR") {
                    containerRoot.addView(child, 0, params)
                } else {
                    bodyLayout.addView(child, params)
                }
            }
        }

        containerRoot.addView(bodyLayout)
        scrollView.addView(containerRoot)
        root.addView(scrollView)

        if (exibirBottomNav) {
            val bottomNav = criarBottomNav(context, abaAtiva, onNavigate)
            val navParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 65)
            ).apply {
                gravity = Gravity.BOTTOM
            }
            root.addView(bottomNav, navParams)
        }

        return Pair(root, layoutDelegado)
    }
}
