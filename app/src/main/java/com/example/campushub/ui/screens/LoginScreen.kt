package com.example.campushub.ui.screens

import android.content.Context
import android.text.InputType
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import com.example.campushub.data.repository.AuthRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents

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
                    userRepo.salvarUsuario(
                        nome = user.displayName ?: userRepo.getUsuarioNome(),
                        matricula = userRepo.getUsuarioMatricula().ifEmpty { (10000000..99999999).random().toString() },
                        email = user.email ?: emailDigitado,
                        senha = senhaDigitada,
                        role = userRepo.getUsuarioRole()
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

        val spinnerRole = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Aluno", "Organizador")
            )
            setPadding(UIComponents.dpToPx(context, 16), UIComponents.dpToPx(context, 14), UIComponents.dpToPx(context, 16), UIComponents.dpToPx(context, 14))
        }
        layout.addView(spinnerRole)

        layout.addView(UIComponents.criarBotao(context, "FINALIZAR CADASTRO") {
            val nome = inputNome.text.toString().trim()
            val matricula = (10000000..99999999).random().toString()
            val email = inputEmail.text.toString().trim()
            val senha = inputSenha.text.toString()
            val confirmarSenha = inputConfirmarSenha.text.toString()
            val selectedRole = if (spinnerRole.selectedItem.toString() == "Organizador") "ORGANIZADOR" else "ALUNO"

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

            AuthRepository.cadastrarUsuario(
                email = email,
                senha = senha,
                nome = nome,
                onSuccess = { _ ->
                    userRepo.salvarUsuario(nome, matricula, email, senha, selectedRole)
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
