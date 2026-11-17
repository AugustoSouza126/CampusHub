// Tela de exibição e edição do perfil acadêmico e permissão do usuário.
package com.example.campushub.ui.screens

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.util.Patterns
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.util.Locale

object ProfileScreen {

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

        val cardPerfil = UIComponents.criarCardView(context, 16f, 4f, 20, AppColors.COLOR_CARD_BG)

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

        val perfilRotulo = if (userRepo.isOrganizador()) "📋 Organizador" else "🎓 Aluno"
        criarCampoPerfilView("🎭", "Perfil de Acesso", perfilRotulo)
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

        if (userRepo.isOrganizador()) {
            layout.addView(UIComponents.criarBotao(context, "📢 EVENTOS CRIADOS POR MIM", primario = false) {
                onVerEventosCriados()
            })
        }

        layout.addView(UIComponents.criarBotao(context, "SAIR DA CONTA", primario = false) {
            onDeslogar()
        })
    }

    fun construirTelaEditarPerfil(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onPerfilAtualizado: () -> Unit,
        onCancelar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Editar Perfil", "Atualize seus dados cadastrais."))

        var perfilSelecionado = userRepo.getUsuarioRole()

        layout.addView(UIComponents.criarRotulo(context, "Perfil de Acesso"))
        val rowPerfil = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UIComponents.dpToPx(context, 44)
            ).apply {
                setMargins(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
        }

        val btnAluno = UIComponents.criarButtonInstance(context).apply {
            text = "🎓 ALUNO"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            if (this is MaterialButton) cornerRadius = UIComponents.dpToPx(context, 10)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(0, 0, UIComponents.dpToPx(context, 4), 0)
            }
        }

        val btnOrganizador = UIComponents.criarButtonInstance(context).apply {
            text = "📋 ORGANIZADOR"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            if (this is MaterialButton) cornerRadius = UIComponents.dpToPx(context, 10)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(UIComponents.dpToPx(context, 4), 0, 0, 0)
            }
        }

        fun atualizarBotoesPerfil() {
            if (perfilSelecionado == "ALUNO") {
                btnAluno.background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 10f)
                btnAluno.setTextColor(Color.WHITE)
                btnOrganizador.background = UIComponents.criarShape(context, Color.WHITE, AppColors.COLOR_STROKE, 1, 10f)
                btnOrganizador.setTextColor(AppColors.COLOR_TEXT_MUTED)
            } else {
                btnOrganizador.background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY, Color.TRANSPARENT, 0, 10f)
                btnOrganizador.setTextColor(Color.WHITE)
                btnAluno.background = UIComponents.criarShape(context, Color.WHITE, AppColors.COLOR_STROKE, 1, 10f)
                btnAluno.setTextColor(AppColors.COLOR_TEXT_MUTED)
            }
        }

        btnAluno.setOnClickListener {
            perfilSelecionado = "ALUNO"
            atualizarBotoesPerfil()
        }

        btnOrganizador.setOnClickListener {
            perfilSelecionado = "ORGANIZADOR"
            atualizarBotoesPerfil()
        }

        atualizarBotoesPerfil()
        rowPerfil.addView(btnAluno)
        rowPerfil.addView(btnOrganizador)
        layout.addView(rowPerfil)

        layout.addView(UIComponents.criarRotulo(context, "Nome completo"))
        val inputNome = UIComponents.criarCampoTexto(context, "Nome completo", valorInicial = userRepo.getUsuarioNome())
        layout.addView(inputNome)

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
            val novoEmail = inputEmail.text.toString().trim()
            val matricula = userRepo.getUsuarioMatricula().ifEmpty {
                "2026" + String.format(Locale.getDefault(), "%04d", (1000..9999).random())
            }

            if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(context, "Todos os campos devem ser preenchidos.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(novoEmail).matches()) {
                Toast.makeText(context, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            userRepo.salvarUsuario(novoNome, matricula, novoEmail, userRepo.getUsuarioSenha(), perfilSelecionado)
            Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            onPerfilAtualizado()
        })

        layout.addView(UIComponents.criarBotao(context, "CANCELAR", primario = false) {
            onCancelar()
        })
    }
}
