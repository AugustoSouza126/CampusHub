package com.example.campushub.ui.screens

import android.content.Context
import android.text.InputType
import android.util.Patterns
import android.widget.LinearLayout
import android.widget.Toast
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents

/**
 * Responsável por construir as telas do fluxo de autenticação de usuários:
 * Login, Cadastro de Conta e Recuperação de Senha.
 */
object LoginScreen {

    /**
     * Constrói e exibe a tela de Login com campos para e-mail e senha,
     * botão de autenticação via Firebase e atalhos para cadastro/recuperação.
     */
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
                    userRepo.salvarUsuario(
                        nome = user.displayName ?: userRepo.getUsuarioNome(),
                        matricula = userRepo.getUsuarioMatricula(),
                        email = user.email ?: emailDigitado,
                        senha = senhaDigitada
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

    /**
     * Constrói e exibe a tela de Cadastro de Nova Conta Acadêmica,
     * realizando as validações de nome, matrícula, formato de e-mail e confirmação de senha.
     */
    fun construirTelaCadastro(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onCadastroSucesso: () -> Unit,
        onVoltar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, "Criar Cadastro", "Preencha suas informações para criar a conta."))

        layout.addView(UIComponents.criarRotulo(context, "Nome completo"))
        val inputNome = UIComponents.criarCampoTexto(context, "Ex: João da Silva")
        layout.addView(inputNome)

        layout.addView(UIComponents.criarRotulo(context, "Matrícula"))
        val inputMatricula = UIComponents.criarCampoTexto(context, "Ex: 2026100123", InputType.TYPE_CLASS_NUMBER)
        layout.addView(inputMatricula)

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
            val matricula = inputMatricula.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val senha = inputSenha.text.toString()
            val confirmarSenha = inputConfirmarSenha.text.toString()

            if (nome.isEmpty()) {
                Toast.makeText(context, "Digite seu nome completo.", Toast.LENGTH_SHORT).show()
                inputNome.requestFocus()
                return@criarBotao
            }

            if (matricula.isEmpty()) {
                Toast.makeText(context, "Digite sua matrícula.", Toast.LENGTH_SHORT).show()
                inputMatricula.requestFocus()
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

            AuthRepository.cadastrarUsuario(
                email = email,
                senha = senha,
                nome = nome,
                onSuccess = { _ ->
                    userRepo.salvarUsuario(nome, matricula, email, senha)
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

    /**
     * Constrói e exibe a tela de Recuperação de Senha, enviando um e-mail de redefinição
     * através das APIs de autenticação do Firebase.
     */
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
