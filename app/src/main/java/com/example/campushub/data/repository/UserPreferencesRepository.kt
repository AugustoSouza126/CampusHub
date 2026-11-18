// Repositório de preferências do usuário para controle de perfil por e-mail, inscrições, check-in e mídias.
package com.example.campushub.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.campushub.data.model.EventAudio
import com.example.campushub.data.model.EventComment
import com.example.campushub.data.model.EventPhoto
import com.example.campushub.data.model.EventRating
import com.example.campushub.data.model.EventSubscription
import com.example.campushub.data.model.EventVideo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("campushub_prefs", Context.MODE_PRIVATE)

    fun salvarUsuario(nome: String, matricula: String, email: String, senha: String, role: String = "ALUNO") {
        val emailKey = email.lowercase(Locale.getDefault()).trim()
        val editor = prefs.edit()
            .putString("user_name", nome)
            .putString("user_matricula", matricula)
            .putString("user_email", email)
            .putString("user_password", senha)
            .putString("user_role", role)
        if (emailKey.isNotEmpty()) {
            editor.putString("user_role_$emailKey", role)
        }
        editor.apply()
    }

    fun getUsuarioNome(): String = prefs.getString("user_name", "") ?: ""
    fun getUsuarioMatricula(): String = prefs.getString("user_matricula", "") ?: ""
    fun getUsuarioEmail(): String = prefs.getString("user_email", "") ?: ""
    fun getUsuarioSenha(): String = prefs.getString("user_password", "") ?: ""

    fun getUsuarioRole(email: String = getUsuarioEmail()): String {
        val emailKey = email.lowercase(Locale.getDefault()).trim()
        val nomeKey = getUsuarioNome().lowercase(Locale.getDefault()).trim()
        if (emailKey.isNotEmpty()) {
            val roleByEmail = prefs.getString("user_role_$emailKey", null)
            if (roleByEmail != null) return roleByEmail
        }
        val currentRole = prefs.getString("user_role", null)
        if (currentRole != null) return currentRole
        if (emailKey.contains("organizador") || emailKey.contains("professor") || emailKey.contains("prof.") ||
            nomeKey.contains("organizador") || nomeKey.contains("professor")) {
            return "ORGANIZADOR"
        }
        return "ALUNO"
    }

    fun isOrganizador(): Boolean = getUsuarioRole() == "ORGANIZADOR"
    fun isAluno(): Boolean = getUsuarioRole() == "ALUNO"

    fun getInscricoes(): MutableSet<String> {
        val set = prefs.getStringSet("user_inscricoes", null)
        return set?.toMutableSet() ?: mutableSetOf()
    }

    fun getTotalInscritos(eventoId: Int): Int {
        val detalhadas = getTodasInscricoesDetalhadas().filter { it.eventoId == eventoId }
        if (detalhadas.isNotEmpty()) return detalhadas.size
        val totalSalvo = prefs.getInt("total_inscritos_$eventoId", -1)
        if (totalSalvo >= 0) return totalSalvo
        return if (isInscrito(eventoId)) 1 else 0
    }

    fun setTotalInscritos(eventoId: Int, total: Int) {
        prefs.edit().putInt("total_inscritos_$eventoId", total.coerceAtLeast(0)).apply()
    }

    fun getTodasInscricoesDetalhadas(): List<EventSubscription> {
        val jsonStr = prefs.getString("user_inscricoes_detalhadas", "[]") ?: "[]"
        val lista = mutableListOf<EventSubscription>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventSubscription(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        eventoId = obj.optInt("eventoId"),
                        alunoNome = obj.optString("alunoNome"),
                        alunoMatricula = obj.optString("alunoMatricula"),
                        alunoEmail = obj.optString("alunoEmail"),
                        checkedIn = obj.optBoolean("checkedIn", false),
                        dataInscricao = obj.optString("dataInscricao")
                    )
                )
            }
        } catch (_: Exception) {}
        return lista
    }

    private fun salvarTodasInscricoesDetalhadas(lista: List<EventSubscription>) {
        val jsonArray = JSONArray()
        for (s in lista) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("eventoId", s.eventoId)
                put("alunoNome", s.alunoNome)
                put("alunoMatricula", s.alunoMatricula)
                put("alunoEmail", s.alunoEmail)
                put("checkedIn", s.checkedIn)
                put("dataInscricao", s.dataInscricao)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_inscricoes_detalhadas", jsonArray.toString()).apply()
    }

    fun getInscritosDoEvento(eventoId: Int): List<EventSubscription> {
        val lista = getTodasInscricoesDetalhadas().filter { it.eventoId == eventoId }
        if (lista.isEmpty()) {
            val totalSalvo = prefs.getInt("total_inscritos_$eventoId", -1)
            if (totalSalvo > 0) {
                return listOf(
                    EventSubscription(eventoId = eventoId, alunoNome = "Carlos Eduardo", alunoMatricula = "20261099", alunoEmail = "carlos@campus.edu.br", checkedIn = false, dataInscricao = "10/10/2026 14:30"),
                    EventSubscription(eventoId = eventoId, alunoNome = "Ana Paula Silva", alunoMatricula = "20261111", alunoEmail = "ana@campus.edu.br", checkedIn = true, dataInscricao = "10/10/2026 15:10")
                )
            }
        }
        return lista
    }

    fun isCheckedIn(eventoId: Int, email: String = getUsuarioEmail()): Boolean {
        if (email.isEmpty()) return false
        return getInscritosDoEvento(eventoId).any { it.alunoEmail.equals(email, ignoreCase = true) && it.checkedIn }
    }

    fun validarCheckInUnico(eventoId: Int, payloadOuEmail: String): Pair<Boolean, String> {
        val termo = if (payloadOuEmail.contains("CAMPUSHUB:")) {
            val parts = payloadOuEmail.split(":")
            if (parts.size >= 3) parts[2].trim() else payloadOuEmail.trim()
        } else {
            payloadOuEmail.trim()
        }

        val lista = getTodasInscricoesDetalhadas().toMutableList()
        val index = lista.indexOfFirst {
            it.eventoId == eventoId &&
            (it.alunoEmail.equals(termo, ignoreCase = true) || it.alunoMatricula.equals(termo, ignoreCase = true))
        }

        if (index == -1) {
            return Pair(false, "Inscrição não encontrada para o código ou e-mail informado.")
        }

        val inscricao = lista[index]
        if (inscricao.checkedIn) {
            return Pair(false, "Atenção: O check-in desta inscrição já foi validado anteriormente! Cada inscrição só pode ser validada uma única vez.")
        }

        lista[index] = inscricao.copy(checkedIn = true)
        salvarTodasInscricoesDetalhadas(lista)
        return Pair(true, "Check-in realizado com sucesso para ${inscricao.alunoNome} (Matrícula: ${inscricao.alunoMatricula})!")
    }

    fun alternarCheckInAluno(eventoId: Int, alunoEmail: String) {
        val lista = getTodasInscricoesDetalhadas().toMutableList()
        val index = lista.indexOfFirst { it.eventoId == eventoId && it.alunoEmail.equals(alunoEmail, ignoreCase = true) }
        if (index != -1) {
            val sub = lista[index]
            lista[index] = sub.copy(checkedIn = !sub.checkedIn)
            salvarTodasInscricoesDetalhadas(lista)
        }
    }

    fun getListaEsperaDoEvento(eventoId: Int): MutableList<String> {
        val jsonStr = prefs.getString("lista_espera_evento_$eventoId", "[]") ?: "[]"
        val lista = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                lista.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}
        return lista
    }

    private fun salvarListaEsperaDoEvento(eventoId: Int, lista: List<String>) {
        val jsonArray = JSONArray()
        for (email in lista) {
            jsonArray.put(email)
        }
        prefs.edit().putString("lista_espera_evento_$eventoId", jsonArray.toString()).apply()
    }

    fun entrarListaEspera(eventoId: Int) {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return
        val lista = getListaEsperaDoEvento(eventoId)
        if (!lista.contains(email)) {
            lista.add(email)
            salvarListaEsperaDoEvento(eventoId, lista)
        }
    }

    fun sairListaEspera(eventoId: Int) {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return
        val lista = getListaEsperaDoEvento(eventoId)
        lista.remove(email)
        salvarListaEsperaDoEvento(eventoId, lista)
    }

    fun isNaListaEspera(eventoId: Int): Boolean {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return false
        return getListaEsperaDoEvento(eventoId).contains(email)
    }

    fun getPosicaoListaEspera(eventoId: Int): Int {
        val email = getUsuarioEmail()
        if (email.isEmpty()) return 0
        val lista = getListaEsperaDoEvento(eventoId)
        val index = lista.indexOf(email)
        return if (index >= 0) index + 1 else 0
    }

    fun salvarInscricao(eventoId: Int) {
        val inscricoes = getInscricoes()
        inscricoes.add(eventoId.toString())
        prefs.edit().putStringSet("user_inscricoes", inscricoes).apply()

        val email = getUsuarioEmail()
        if (email.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dataFormatada = sdf.format(Date())
            val detalhadas = getTodasInscricoesDetalhadas().toMutableList()
            if (!detalhadas.any { it.eventoId == eventoId && it.alunoEmail.equals(email, ignoreCase = true) }) {
                detalhadas.add(
                    EventSubscription(
                        eventoId = eventoId,
                        alunoNome = getUsuarioNome().ifEmpty { "Aluno" },
                        alunoMatricula = getUsuarioMatricula().ifEmpty { "20260000" },
                        alunoEmail = email,
                        checkedIn = false,
                        dataInscricao = dataFormatada
                    )
                )
                salvarTodasInscricoesDetalhadas(detalhadas)
            }
        }

        val totalAtual = getTotalInscritos(eventoId)
        setTotalInscritos(eventoId, totalAtual + 1)
        sairListaEspera(eventoId)
    }

    fun removerInscricao(eventoId: Int) {
        val inscricoes = getInscricoes()
        inscricoes.remove(eventoId.toString())
        prefs.edit().putStringSet("user_inscricoes", inscricoes).apply()

        val email = getUsuarioEmail()
        if (email.isNotEmpty()) {
            val detalhadas = getTodasInscricoesDetalhadas().filterNot { it.eventoId == eventoId && it.alunoEmail.equals(email, ignoreCase = true) }
            salvarTodasInscricoesDetalhadas(detalhadas)
        }

        val totalAtual = getTotalInscritos(eventoId)
        val novoTotal = (totalAtual - 1).coerceAtLeast(0)
        setTotalInscritos(eventoId, novoTotal)

        promoverProximoDaListaEspera(eventoId)
    }

    private fun promoverProximoDaListaEspera(eventoId: Int) {
        val lista = getListaEsperaDoEvento(eventoId)
        if (lista.isNotEmpty()) {
            val proximoEmail = lista.removeAt(0)
            salvarListaEsperaDoEvento(eventoId, lista)

            val emailAtual = getUsuarioEmail()
            if (emailAtual.isNotEmpty() && proximoEmail.equals(emailAtual, ignoreCase = true)) {
                salvarInscricao(eventoId)
            } else {
                val totalAtual = getTotalInscritos(eventoId)
                setTotalInscritos(eventoId, totalAtual + 1)
            }
        }
    }

    fun isInscrito(eventoId: Int): Boolean {
        return getInscricoes().contains(eventoId.toString())
    }

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

    fun getTodosComentarios(): List<EventComment> {
        val jsonStr = prefs.getString("user_comentarios", "[]") ?: "[]"
        val lista = mutableListOf<EventComment>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventComment(
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

    private fun salvarTodosComentarios(lista: List<EventComment>) {
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

    fun getComentariosDoEvento(eventoId: Int): List<EventComment> {
        return getTodosComentarios().filter { it.eventoId == eventoId }
    }

    fun adicionarComentario(eventoId: Int, texto: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = sdf.format(Date())
        val novo = EventComment(
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

    fun getTodasAvaliacoes(): List<EventRating> {
        val jsonStr = prefs.getString("user_avaliacoes", "[]") ?: "[]"
        val lista = mutableListOf<EventRating>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventRating(
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

    private fun salvarTodasAvaliacoes(lista: List<EventRating>) {
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
            lista[index] = EventRating(eventoId, email, nota)
        } else {
            lista.add(EventRating(eventoId, email, nota))
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

    fun getTodasFotos(): List<EventPhoto> {
        val jsonStr = prefs.getString("user_fotos", "[]") ?: "[]"
        val lista = mutableListOf<EventPhoto>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventPhoto(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        eventoId = obj.optInt("eventoId"),
                        caminhoArquivo = obj.optString("caminhoArquivo"),
                        autorNome = obj.optString("autorNome"),
                        autorEmail = obj.optString("autorEmail"),
                        dataPublicacao = obj.optString("dataPublicacao")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun salvarTodasFotos(lista: List<EventPhoto>) {
        val jsonArray = JSONArray()
        for (f in lista) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("eventoId", f.eventoId)
                put("caminhoArquivo", f.caminhoArquivo)
                put("autorNome", f.autorNome)
                put("autorEmail", f.autorEmail)
                put("dataPublicacao", f.dataPublicacao)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_fotos", jsonArray.toString()).apply()
    }

    fun getFotosDoEvento(eventoId: Int): List<EventPhoto> {
        return getTodasFotos().filter { it.eventoId == eventoId }
    }

    fun adicionarFoto(eventoId: Int, caminhoArquivo: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = sdf.format(Date())
        val novaFoto = EventPhoto(
            eventoId = eventoId,
            caminhoArquivo = caminhoArquivo,
            autorNome = getUsuarioNome().ifEmpty { "Usuário" },
            autorEmail = getUsuarioEmail(),
            dataPublicacao = dataFormatada
        )
        val lista = getTodasFotos().toMutableList()
        lista.add(novaFoto)
        salvarTodasFotos(lista)
    }

    fun deletarFoto(fotoId: String) {
        val foto = getTodasFotos().find { it.id == fotoId }
        if (foto != null) {
            try {
                val f = File(foto.caminhoArquivo)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        val lista = getTodasFotos().filter { it.id != fotoId }
        salvarTodasFotos(lista)
    }

    fun getTodosAudios(): List<EventAudio> {
        val jsonStr = prefs.getString("user_audios", "[]") ?: "[]"
        val lista = mutableListOf<EventAudio>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventAudio(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        eventoId = obj.optInt("eventoId"),
                        caminhoArquivo = obj.optString("caminhoArquivo"),
                        duracaoTexto = obj.optString("duracaoTexto", "00:00"),
                        autorNome = obj.optString("autorNome"),
                        autorEmail = obj.optString("autorEmail"),
                        dataPublicacao = obj.optString("dataPublicacao")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun salvarTodosAudios(lista: List<EventAudio>) {
        val jsonArray = JSONArray()
        for (a in lista) {
            val obj = JSONObject().apply {
                put("id", a.id)
                put("eventoId", a.eventoId)
                put("caminhoArquivo", a.caminhoArquivo)
                put("duracaoTexto", a.duracaoTexto)
                put("autorNome", a.autorNome)
                put("autorEmail", a.autorEmail)
                put("dataPublicacao", a.dataPublicacao)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_audios", jsonArray.toString()).apply()
    }

    fun getAudiosDoEvento(eventoId: Int): List<EventAudio> {
        return getTodosAudios().filter { it.eventoId == eventoId }
    }

    fun adicionarAudio(eventoId: Int, caminhoArquivo: String, duracaoTexto: String = "00:00") {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = sdf.format(Date())
        val novoAudio = EventAudio(
            eventoId = eventoId,
            caminhoArquivo = caminhoArquivo,
            duracaoTexto = duracaoTexto,
            autorNome = getUsuarioNome().ifEmpty { "Usuário" },
            autorEmail = getUsuarioEmail(),
            dataPublicacao = dataFormatada
        )
        val lista = getTodosAudios().toMutableList()
        lista.add(novoAudio)
        salvarTodosAudios(lista)
    }

    fun deletarAudio(audioId: String) {
        val audio = getTodosAudios().find { it.id == audioId }
        if (audio != null) {
            try {
                val f = File(audio.caminhoArquivo)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        val lista = getTodosAudios().filter { it.id != audioId }
        salvarTodosAudios(lista)
    }

    fun getTodosVideos(): List<EventVideo> {
        val jsonStr = prefs.getString("user_videos", "[]") ?: "[]"
        val lista = mutableListOf<EventVideo>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    EventVideo(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        eventoId = obj.optInt("eventoId"),
                        caminhoArquivo = obj.optString("caminhoArquivo"),
                        autorNome = obj.optString("autorNome"),
                        autorEmail = obj.optString("autorEmail"),
                        dataPublicacao = obj.optString("dataPublicacao")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun salvarTodosVideos(lista: List<EventVideo>) {
        val jsonArray = JSONArray()
        for (v in lista) {
            val obj = JSONObject().apply {
                put("id", v.id)
                put("eventoId", v.eventoId)
                put("caminhoArquivo", v.caminhoArquivo)
                put("autorNome", v.autorNome)
                put("autorEmail", v.autorEmail)
                put("dataPublicacao", v.dataPublicacao)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_videos", jsonArray.toString()).apply()
    }

    fun getVideosDoEvento(eventoId: Int): List<EventVideo> {
        return getTodosVideos().filter { it.eventoId == eventoId }
    }

    fun adicionarVideo(eventoId: Int, caminhoArquivo: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = sdf.format(Date())
        val novoVideo = EventVideo(
            eventoId = eventoId,
            caminhoArquivo = caminhoArquivo,
            autorNome = getUsuarioNome().ifEmpty { "Usuário" },
            autorEmail = getUsuarioEmail(),
            dataPublicacao = dataFormatada
        )
        val lista = getTodosVideos().toMutableList()
        lista.add(novoVideo)
        salvarTodosVideos(lista)
    }

    fun deletarVideo(videoId: String) {
        val video = getTodosVideos().find { it.id == videoId }
        if (video != null) {
            try {
                val f = File(video.caminhoArquivo)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        val lista = getTodosVideos().filter { it.id != videoId }
        salvarTodosVideos(lista)
    }
}
