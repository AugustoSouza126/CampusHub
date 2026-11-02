package com.example.campushub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

object AuthRepository {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (_: Throwable) {
            null
        }

    fun getUsuarioAtual(): FirebaseUser? {
        return auth?.currentUser
    }

    fun isUsuarioLogado(): Boolean {
        return auth?.currentUser != null
    }

    fun fazerLogin(
        email: String,
        senha: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Serviço de autenticação do Firebase indisponível.")
            return
        }
        firebaseAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError("Usuário não encontrado após login.")
                    }
                } else {
                    val msg = task.exception?.localizedMessage ?: "Erro ao realizar login no Firebase."
                    onError(msg)
                }
            }
    }

    fun cadastrarUsuario(
        email: String,
        senha: String,
        nome: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Serviço de autenticação do Firebase indisponível.")
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nome)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            onSuccess(user)
                        }
                    } else {
                        onError("Erro ao registrar usuário.")
                    }
                } else {
                    val msg = task.exception?.localizedMessage ?: "Erro ao criar conta no Firebase."
                    onError(msg)
                }
            }
    }

    fun enviarEmailRecuperacao(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Serviço de autenticação do Firebase indisponível.")
            return
        }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Erro ao enviar e-mail de recuperação."
                    onError(msg)
                }
            }
    }

    fun deslogar() {
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
    }
}
