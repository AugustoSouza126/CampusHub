package com.example.campushub.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.campushub.data.model.Avaliacao
import com.example.campushub.data.model.Comentario
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("campushub_prefs", Context.MODE_PRIVATE)

    // =========================================================
    // USUÁRIO & AUTENTICAÇÃO
    // =========================================================

    fun salvarUsuario(nome: String, matricula: String, email: String, senha: String) {
        prefs.edit()
            .putString("user_name", nome)
            .putString("user_matricula", matricula)
            .putString("user_email", email)
            .putString("user_password", senha)
            .apply()
    }

    fun getUsuarioNome(): String = prefs.getString("user_name", "") ?: ""
    fun getUsuarioMatricula(): String = prefs.getString("user_matricula", "") ?: ""
    fun getUsuarioEmail(): String = prefs.getString("user_email", "") ?: ""
    fun getUsuarioSenha(): String = prefs.getString("user_password", "") ?: ""

    // =========================================================
    // INSCRIÇÕES EM EVENTOS
    // =========================================================

    fun getInscricoes(): MutableSet<String> {
        val set = prefs.getStringSet("user_inscricoes", null)
        return set?.toMutableSet() ?: mutableSetOf()
    }

    fun salvarInscricao(eventoId: Int) {
        val inscricoes = getInscricoes()
        inscricoes.add(eventoId.toString())
        prefs.edit().putStringSet("user_inscricoes", inscricoes).apply()
    }

    fun removerInscricao(eventoId: Int) {
        val inscricoes = getInscricoes()
        inscricoes.remove(eventoId.toString())
        prefs.edit().putStringSet("user_inscricoes", inscricoes).apply()
    }

    fun isInscrito(eventoId: Int): Boolean {
        return getInscricoes().contains(eventoId.toString())
    }

    // =========================================================
    // FAVORITOS
    // =========================================================

    fun getFavoritos(): MutableSet<String> {
        val set = prefs.getStringSet("user_favoritos", null)
        return set?.toMutableSet() ?: mutableSetOf()
    }

    fun salvarFavorito(eventoId: Int) {
        val favs = getFavoritos()
        favs.add(eventoId.toString())
        prefs.edit().putStringSet("user_favoritos", favs).apply()
    }

    fun removerFavorito(eventoId: Int) {
        val favs = getFavoritos()
        favs.remove(eventoId.toString())
        prefs.edit().putStringSet("user_favoritos", favs).apply()
    }

    fun isFavorito(eventoId: Int): Boolean {
        return getFavoritos().contains(eventoId.toString())
    }

    // =========================================================
    // COMENTÁRIOS
    // =========================================================

    fun getTodosComentarios(): List<Comentario> {
        val jsonStr = prefs.getString("user_comentarios", "[]") ?: "[]"
        val lista = mutableListOf<Comentario>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    Comentario(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        eventoId = obj.optInt("eventoId"),
                        autorNome = obj.optString("autorNome"),
                        autorEmail = obj.optString("autorEmail"),
                        texto = obj.optString("texto"),
                        dataPublicacao = obj.optString("dataPublicacao")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun salvarTodosComentarios(lista: List<Comentario>) {
        val jsonArray = JSONArray()
        for (c in lista) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("eventoId", c.eventoId)
                put("autorNome", c.autorNome)
                put("autorEmail", c.autorEmail)
                put("texto", c.texto)
                put("dataPublicacao", c.dataPublicacao)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_comentarios", jsonArray.toString()).apply()
    }

    fun getComentariosDoEvento(eventoId: Int): List<Comentario> {
        return getTodosComentarios().filter { it.eventoId == eventoId }
    }

    fun adicionarComentario(eventoId: Int, texto: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = sdf.format(Date())
        val novo = Comentario(
            eventoId = eventoId,
            autorNome = getUsuarioNome().ifEmpty { "Usuário" },
            autorEmail = getUsuarioEmail(),
            texto = texto,
            dataPublicacao = dataFormatada
        )
        val lista = getTodosComentarios().toMutableList()
        lista.add(novo)
        salvarTodosComentarios(lista)
    }

    fun atualizarComentario(comentarioId: String, novoTexto: String) {
        val lista = getTodosComentarios().toMutableList()
        val index = lista.indexOfFirst { it.id == comentarioId }
        if (index != -1) {
            val c = lista[index]
            lista[index] = c.copy(texto = novoTexto)
            salvarTodosComentarios(lista)
        }
    }

    fun deletarComentario(comentarioId: String) {
        val lista = getTodosComentarios().filter { it.id != comentarioId }
        salvarTodosComentarios(lista)
    }

    // =========================================================
    // AVALIAÇÕES (NOTAS DE 1 A 5 ESTRELAS)
    // =========================================================

    fun getTodasAvaliacoes(): List<Avaliacao> {
        val jsonStr = prefs.getString("user_avaliacoes", "[]") ?: "[]"
        val lista = mutableListOf<Avaliacao>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    Avaliacao(
                        eventoId = obj.optInt("eventoId"),
                        autorEmail = obj.optString("autorEmail"),
                        nota = obj.optInt("nota")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun salvarTodasAvaliacoes(lista: List<Avaliacao>) {
        val jsonArray = JSONArray()
        for (a in lista) {
            val obj = JSONObject().apply {
                put("eventoId", a.eventoId)
                put("autorEmail", a.autorEmail)
                put("nota", a.nota)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_avaliacoes", jsonArray.toString()).apply()
    }

    fun getAvaliacaoDoUsuario(eventoId: Int): Int? {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return null
        return getTodasAvaliacoes().find { it.eventoId == eventoId && it.autorEmail.equals(email, ignoreCase = true) }?.nota
    }

    fun salvarOuAtualizarAvaliacao(eventoId: Int, nota: Int) {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return
        val lista = getTodasAvaliacoes().toMutableList()
        val index = lista.indexOfFirst { it.eventoId == eventoId && it.autorEmail.equals(email, ignoreCase = true) }
        if (index != -1) {
            lista[index] = Avaliacao(eventoId, email, nota)
        } else {
            lista.add(Avaliacao(eventoId, email, nota))
        }
        salvarTodasAvaliacoes(lista)
    }

    fun getMediaAvaliacoes(eventoId: Int): Pair<Float, Int> {
        val avaliacoes = getTodasAvaliacoes().filter { it.eventoId == eventoId }
        if (avaliacoes.isEmpty()) return Pair(0f, 0)
        val soma = avaliacoes.sumOf { it.nota }
        val media = soma.toFloat() / avaliacoes.size
        return Pair(media, avaliacoes.size)
    }
}
