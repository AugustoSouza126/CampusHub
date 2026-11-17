// Tela de autenticação e cadastro de usuários com geração de matrícula em segundo plano.
package com.example.campushub.ui.screens

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.util.Patterns
import android.widget.LinearLayout
import android.widget.Toast
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.util.Locale

object LoginScreen {

    fun construirTelaLogin(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onLoginSucesso: () -> Unit,
        onIrParaCadastro: () -> Unit,
        onIrParaRecuperacao: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Bem-vindo ao CampusHub", "Acesse sua conta para ver os eventos da universidade."))

        layout.addView(UIComponents.criarRotulo(context, "E-mail institucional"))
        val inputEmail = UIComponents.criarCampoTexto(context, "exemplo@universidade.edu.br", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        layout.addView(inputEmail)

        layout.addView(UIComponents.criarRotulo(context, "Senha"))
        val inputSenha = UIComponents.criarCampoTexto(
            context,
            "Digite sua senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputSenha)

        layout.addView(UIComponents.criarBotao(context, "ENTRAR NO SISTEMA") {
            val emailDigitado = inputEmail.text.toString().trim()
            val senhaDigitada = inputSenha.text.toString()

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(context, "Preencha e-mail e senha para entrar.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            AuthRepository.fazerLogin(
                email = emailDigitado,
                senha = senhaDigitada,
                onSuccess = { user ->
                    val emailLogado = user.email ?: emailDigitado
                    val roleSalva = userRepo.getUsuarioRole(emailLogado)
                    val matriculaSalva = userRepo.getUsuarioMatricula().ifEmpty {
                        "2026" + String.format(Locale.getDefault(), "%04d", (1000..9999).random())
                    }
                    userRepo.salvarUsuario(
                        nome = user.displayName ?: userRepo.getUsuarioNome(),
                        matricula = matriculaSalva,
                        email = emailLogado,
                        senha = senhaDigitada,
                        role = roleSalva
                    )
                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    onLoginSucesso()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(UIComponents.criarBotao(context, "CRIAR NOVA CONTA", primario = false) {
            onIrParaCadastro()
        })

        layout.addView(UIComponents.criarBotao(context, "ESQUECI MINHA SENHA", primario = false) {
            onIrParaRecuperacao()
        })
    }

    fun construirTelaCadastro(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onCadastroSucesso: () -> Unit,
        onVoltar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Criar Cadastro", "Preencha suas informações para criar a conta."))

        var perfilSelecionado = "ALUNO"

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
        val inputNome = UIComponents.criarCampoTexto(context, "Ex: João da Silva")
        layout.addView(inputNome)

        layout.addView(UIComponents.criarRotulo(context, "E-mail"))
        val inputEmail = UIComponents.criarCampoTexto(
            context,
            "exemplo@universidade.edu.br",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        layout.addView(inputEmail)

        layout.addView(UIComponents.criarRotulo(context, "Senha"))
        val inputSenha = UIComponents.criarCampoTexto(
            context,
            "Senha (mínimo 6 caracteres)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputSenha)

        layout.addView(UIComponents.criarRotulo(context, "Confirmar senha"))
        val inputConfirmarSenha = UIComponents.criarCampoTexto(
            context,
            "Repita a sua senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        layout.addView(inputConfirmarSenha)

        layout.addView(UIComponents.criarBotao(context, "FINALIZAR CADASTRO") {
            val nome = inputNome.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val senha = inputSenha.text.toString()
            val confirmarSenha = inputConfirmarSenha.text.toString()

            if (nome.isEmpty()) {
                Toast.makeText(context, "Digite seu nome completo.", Toast.LENGTH_SHORT).show()
                inputNome.requestFocus()
                return@criarBotao
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                inputEmail.requestFocus()
                return@criarBotao
            }

            if (senha.length < 6) {
                Toast.makeText(context, "A senha deve conter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                inputSenha.requestFocus()
                return@criarBotao
            }

            if (senha != confirmarSenha) {
                Toast.makeText(context, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                inputConfirmarSenha.requestFocus()
                return@criarBotao
            }

            val matriculaAutomatica = "2026" + String.format(Locale.getDefault(), "%04d", (1000..9999).random())

            AuthRepository.cadastrarUsuario(
                email = email,
                senha = senha,
                nome = nome,
                onSuccess = { _ ->
                    userRepo.salvarUsuario(nome, matriculaAutomatica, email, senha, perfilSelecionado)
                    Toast.makeText(context, "Conta criada com sucesso no Firebase!", Toast.LENGTH_SHORT).show()
                    onCadastroSucesso()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(UIComponents.criarBotao(context, "VOLTAR AO LOGIN", primario = false) {
            onVoltar()
        })
    }

    fun construirTelaRecuperacao(
        context: Context,
        layout: LinearLayout,
        onVoltar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Recuperar Senha", "Informe seu e-mail cadastrado para redefinir a senha."))

        layout.addView(UIComponents.criarRotulo(context, "E-mail cadastrado"))
        val inputEmail = UIComponents.criarCampoTexto(
            context,
            "exemplo@universidade.edu.br",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        layout.addView(inputEmail)

        layout.addView(UIComponents.criarBotao(context, "ENVIAR CÓDIGO DE RECUPERAÇÃO") {
            val email = inputEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            AuthRepository.enviarEmailRecuperacao(
                email = email,
                onSuccess = {
                    Toast.makeText(context, "E-mail de redefinição enviado para $email!", Toast.LENGTH_LONG).show()
                    onVoltar()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        })

        layout.addView(UIComponents.criarBotao(context, "VOLTAR", primario = false) {
            onVoltar()
        })
    }
}
