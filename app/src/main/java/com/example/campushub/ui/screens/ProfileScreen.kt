package com.example.campushub.ui.screens

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.util.Patterns
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors

/**
 * Responsável por construir as telas do Perfil do Usuário e Edição de Dados Acadêmicos.
 */
object ProfileScreen {

    /**
     * Constrói a tela de Perfil do Usuário, exibindo os dados salvos
     */
    fun construirTelaPerfil(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onEditarPerfil: () -> Unit,
        onVerEventosCriados: () -> Unit,
        onDeslogar: () -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        layout.addView(UIComponents.criarHeaderBar(context, "Meu Perfil", "Gerencie os dados da sua conta acadêmica."))

        val cardPerfil = CardView(context).apply {
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setContentPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val innerPerfil = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun criarCampoPerfilView(icone: String, rotulo: String, valor: String) {
            val tvRotulo = TextView(context).apply {
                text = "$icone  $rotulo"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, dpToPx(8), 0, dpToPx(2))
            }
            innerPerfil.addView(tvRotulo)

            val tvValor = TextView(context).apply {
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

        layout.addView(UIComponents.criarBotao(context, "EDITAR PERFIL") {
            onEditarPerfil()
        })

        layout.addView(UIComponents.criarBotao(context, "📢 EVENTOS CRIADOS POR MIM", primario = false) {
            onVerEventosCriados()
        })

        layout.addView(UIComponents.criarBotao(context, "SAIR DA CONTA", primario = false) {
            onDeslogar()
        })
    }

    /**
     * Constrói a tela de Edição de Perfil, permitindo alterar Nome, Matrícula e E-mail,
     * persistindo as alterações no [UserPreferencesRepository].
     */
    fun construirTelaEditarPerfil(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onPerfilAtualizado: () -> Unit,
        onCancelar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Editar Perfil", "Atualize seus dados cadastrais."))

        layout.addView(UIComponents.criarRotulo(context, "Nome completo"))
        val inputNome = UIComponents.criarCampoTexto(context, "Nome completo", valorInicial = userRepo.getUsuarioNome())
        layout.addView(inputNome)

        layout.addView(UIComponents.criarRotulo(context, "Matrícula"))
        val inputMatricula = UIComponents.criarCampoTexto(
            context,
            "Matrícula",
            inputType = InputType.TYPE_CLASS_NUMBER,
            valorInicial = userRepo.getUsuarioMatricula()
        )
        layout.addView(inputMatricula)

        layout.addView(UIComponents.criarRotulo(context, "E-mail"))
        val inputEmail = UIComponents.criarCampoTexto(
            context,
            "E-mail",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            valorInicial = userRepo.getUsuarioEmail()
        )
        layout.addView(inputEmail)

        layout.addView(UIComponents.criarBotao(context, "SALVAR ALTERAÇÕES") {
            val novoNome = inputNome.text.toString().trim()
            val novaMatricula = inputMatricula.text.toString().trim()
            val novoEmail = inputEmail.text.toString().trim()

            if (novoNome.isEmpty() || novaMatricula.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(context, "Todos os campos devem ser preenchidos.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(novoEmail).matches()) {
                Toast.makeText(context, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            userRepo.salvarUsuario(novoNome, novaMatricula, novoEmail, userRepo.getUsuarioSenha())
            Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            onPerfilAtualizado()
        })

        layout.addView(UIComponents.criarBotao(context, "CANCELAR", primario = false) {
            onCancelar()
        })
    }
}
