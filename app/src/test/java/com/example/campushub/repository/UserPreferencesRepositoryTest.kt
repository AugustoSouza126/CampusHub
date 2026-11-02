package com.example.campushub.repository

import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para [UserPreferencesRepository] executando em ambiente de host JVM via Robolectric.
 * Gerencia preferências locais de usuário, inscrições, favoritos, comentários, avaliações e mídias.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserPreferencesRepositoryTest {

    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        // Inicializa o repositório utilizando o contexto da aplicação provido pelo Robolectric
        repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())
    }

    /**
     * Verifica os valores padrão de um usuário quando o armazenamento local está vazio.
     */
    @Test
    fun testDefaultUserValuesWhenEmpty() {
        assertEquals("", repository.getUsuarioNome())
        assertEquals("", repository.getUsuarioMatricula())
        assertEquals("", repository.getUsuarioEmail())
        assertEquals("", repository.getUsuarioSenha())
        assertTrue(repository.getInscricoes().isEmpty())
        assertTrue(repository.getFavoritos().isEmpty())
    }

    /**
     * Testa a gravação e recuperação dos dados de perfil do usuário.
     */
    @Test
    fun testSaveAndGetUserProfile() {
        repository.salvarUsuario("Carlos Eduardo", "20261099", "carlos@campus.edu.br", "senha123")

        assertEquals("Carlos Eduardo", repository.getUsuarioNome())
        assertEquals("20261099", repository.getUsuarioMatricula())
        assertEquals("carlos@campus.edu.br", repository.getUsuarioEmail())
        assertEquals("senha123", repository.getUsuarioSenha())
    }

    /**
     * Testa a confirmação, listagem e cancelamento de inscrição em eventos.
     */
    @Test
    fun testEventSubscriptions() {
        val eventId = 101

        assertFalse(repository.isInscrito(eventId))

        repository.salvarInscricao(eventId)
        assertTrue(repository.isInscrito(eventId))
        assertTrue(repository.getInscricoes().contains("101"))

        repository.removerInscricao(eventId)
        assertFalse(repository.isInscrito(eventId))
    }

    /**
     * Testa a adição e remoção de eventos na lista de favoritos.
     */
    @Test
    fun testFavoriteEvents() {
        val eventId = 202

        assertFalse(repository.isFavorito(eventId))

        repository.salvarFavorito(eventId)
        assertTrue(repository.isFavorito(eventId))

        repository.removerFavorito(eventId)
        assertFalse(repository.isFavorito(eventId))
    }

    /**
     * Testa o ciclo de vida de um comentário: adição, listagem, atualização e exclusão.
     */
    @Test
    fun testEventCommentsLifecycle() {
        repository.salvarUsuario("Ana Paula", "20261111", "ana@campus.edu.br", "123456")
        val eventId = 303

        assertTrue(repository.getComentariosDoEvento(eventId).isEmpty())

        repository.adicionarComentario(eventId, "Ótima iniciativa!")
        val commentsList = repository.getComentariosDoEvento(eventId)
        assertEquals(1, commentsList.size)
        assertEquals("Ótima iniciativa!", commentsList.first().texto)
        assertEquals("Ana Paula", commentsList.first().autorNome)

        val commentId = commentsList.first().id
        repository.atualizarComentario(commentId, "Comentário atualizado!")
        val updatedList = repository.getComentariosDoEvento(eventId)
        assertEquals("Comentário atualizado!", updatedList.first().texto)

        repository.deletarComentario(commentId)
        assertTrue(repository.getComentariosDoEvento(eventId).isEmpty())
    }

    /**
     * Testa atribuição de notas e cálculo automático de média e total de avaliações.
     */
    @Test
    fun testEventRatingsAndAverageCalculation() {
        repository.salvarUsuario("Lucas Lima", "20262222", "lucas@campus.edu.br", "123456")
        val eventId = 404

        assertNull(repository.getAvaliacaoDoUsuario(eventId))
        val (initialAverage, initialTotal) = repository.getMediaAvaliacoes(eventId)
        assertEquals(0f, initialAverage, 0.01f)
        assertEquals(0, initialTotal)

        repository.salvarOuAtualizarAvaliacao(eventId, 5)
        assertEquals(Integer.valueOf(5), repository.getAvaliacaoDoUsuario(eventId))

        val (finalAverage, finalTotal) = repository.getMediaAvaliacoes(eventId)
        assertEquals(5f, finalAverage, 0.01f)
        assertEquals(1, finalTotal)
    }

    /**
     * Testa adição, consulta e remoção de fotos de eventos.
     */
    @Test
    fun testEventPhotosManagement() {
        repository.salvarUsuario("Fernanda", "20263333", "nanda@campus.edu.br", "123456")
        val eventId = 505

        assertTrue(repository.getFotosDoEvento(eventId).isEmpty())

        repository.adicionarFoto(eventId, "/path/local/foto.jpg")
        val photosList = repository.getFotosDoEvento(eventId)
        assertEquals(1, photosList.size)
        assertEquals("/path/local/foto.jpg", photosList.first().caminhoArquivo)

        repository.deletarFoto(photosList.first().id)
        assertTrue(repository.getFotosDoEvento(eventId).isEmpty())
    }

    /**
     * Testa adição, consulta e remoção de áudios de eventos.
     */
    @Test
    fun testEventAudiosManagement() {
        repository.salvarUsuario("Gabriel", "20264444", "gabriel@campus.edu.br", "123456")
        val eventId = 606

        assertTrue(repository.getAudiosDoEvento(eventId).isEmpty())

        repository.adicionarAudio(eventId, "/path/local/audio.m4a", "00:15")
        val audiosList = repository.getAudiosDoEvento(eventId)
        assertEquals(1, audiosList.size)
        assertEquals("/path/local/audio.m4a", audiosList.first().caminhoArquivo)

        repository.deletarAudio(audiosList.first().id)
        assertTrue(repository.getAudiosDoEvento(eventId).isEmpty())
    }

    /**
     * Testa adição, consulta e remoção de vídeos de eventos.
     */
    @Test
    fun testEventVideosManagement() {
        repository.salvarUsuario("Juliana", "20265555", "juliana@campus.edu.br", "123456")
        val eventId = 707

        assertTrue(repository.getVideosDoEvento(eventId).isEmpty())

        repository.adicionarVideo(eventId, "/path/local/video.mp4")
        val videosList = repository.getVideosDoEvento(eventId)
        assertEquals(1, videosList.size)
        assertEquals("/path/local/video.mp4", videosList.first().caminhoArquivo)

        repository.deletarVideo(videosList.first().id)
        assertTrue(repository.getVideosDoEvento(eventId).isEmpty())
    }
}
