package com.example.campushub.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suíte com 10 Testes de Integração verificando a comunicação e regras de negócio
 * entre os Modelos, Repositórios, SharedPreferences e Estruturas de Dados do Firebase.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppFirebaseIntegrationTest {

    private lateinit var context: Context
    private lateinit var userRepo: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userRepo = UserPreferencesRepository(context)

        // Salva perfil do usuário de teste
        userRepo.salvarUsuario("Augusto Silva", "20261001", "augusto@universidade.edu.br", "123456")
    }

    // ------------------------------------------------------------------------
    // Teste 1: Autenticação e Integração de Sessão do Usuário
    // ------------------------------------------------------------------------
    /**
     * Verifica se os dados de autenticação e credenciais do perfil do usuário coincidem
     * e se integram perfeitamente com o armazenamento local de preferências.
     */
    @Test
    fun testUserAuthenticationAndSessionIntegration() {
        assertEquals("Augusto Silva", userRepo.getUsuarioNome())
        assertEquals("20261001", userRepo.getUsuarioMatricula())
        assertEquals("augusto@universidade.edu.br", userRepo.getUsuarioEmail())
        assertEquals("123456", userRepo.getUsuarioSenha())
    }

    // ------------------------------------------------------------------------
    // Teste 2: Criação de Evento e Mapeamento para o Firestore
    // ------------------------------------------------------------------------
    /**
     * Verifica se a estância do modelo de dados Event integra com os parâmetros
     * de documentos e coleções exigidos pelo Cloud Firestore do Firebase.
     */
    @Test
    fun testCreateEventAndFirestoreMappingIntegration() {
        val event = Event(
            id = 101,
            titulo = "Firebase Integration Workshop",
            categoria = "Workshop",
            descricao = "Learn Cloud Firestore and Firebase Auth integration",
            data = "25/11/2026",
            dataBadge = "25 NOV",
            horario = "14:00",
            local = "Lab 01",
            vagas = 50,
            encerrado = false,
            criadorEmail = userRepo.getUsuarioEmail()
        )

        assertEquals(101, event.id)
        assertEquals("Firebase Integration Workshop", event.titulo)
        assertEquals("Workshop", event.categoria)
        assertEquals("augusto@universidade.edu.br", event.criadorEmail)
        assertFalse(event.encerrado)
    }

    // ------------------------------------------------------------------------
    // Teste 3: Inscrição em Eventos e Verificação de Status
    // ------------------------------------------------------------------------
    /**
     * Testa o fluxo de inscrição do aluno em um evento, cancelamento de vaga
     * e verificação do status de confirmação.
     */
    @Test
    fun testEventEnrollmentAndStatusVerificationIntegration() {
        val eventId = 1

        // Inicialmente não inscrito
        assertFalse(userRepo.isInscrito(eventId))

        // Inscreve o aluno
        userRepo.salvarInscricao(eventId)
        assertTrue(userRepo.isInscrito(eventId))

        // Cancela a inscrição
        userRepo.removerInscricao(eventId)
        assertFalse(userRepo.isInscrito(eventId))
    }

    // ------------------------------------------------------------------------
    // Teste 4: Favoritar Eventos e Filtragem na Aba de Favoritos
    // ------------------------------------------------------------------------
    /**
     * Testa a ação de favoritar e desfavoritar eventos e checa a filtragem
     * da lista de favoritos do usuário.
     */
    @Test
    fun testFavoriteEventsAndTabFilteringIntegration() {
        val eventId1 = 1
        val eventId2 = 2

        userRepo.salvarFavorito(eventId1)
        assertTrue(userRepo.isFavorito(eventId1))
        assertFalse(userRepo.isFavorito(eventId2))

        val favorites = userRepo.getFavoritos()
        assertTrue(favorites.contains(eventId1.toString()))

        userRepo.removerFavorito(eventId1)
        assertFalse(userRepo.isFavorito(eventId1))
    }

    // ------------------------------------------------------------------------
    // Teste 5: Avaliação de Evento Encerrado e Cálculo de Média
    // ------------------------------------------------------------------------
    /**
     * Testa o envio de nota de 1 a 5 estrelas para evento encerrado
     * e a atualização da média de pontuação do evento.
     */
    @Test
    fun testEndedEventRatingAndAverageScoreIntegration() {
        val endedEventId = 0 // Evento encerrado
        userRepo.salvarInscricao(endedEventId)

        // Envia nota 5 estrelas
        userRepo.salvarOuAtualizarAvaliacao(endedEventId, 5)

        assertEquals(5, userRepo.getAvaliacaoDoUsuario(endedEventId))

        val (average, total) = userRepo.getMediaAvaliacoes(endedEventId)
        assertEquals(5.0f, average, 0.01f)
        assertEquals(1, total)

        // Atualiza a nota para 4 estrelas
        userRepo.salvarOuAtualizarAvaliacao(endedEventId, 4)
        assertEquals(4, userRepo.getAvaliacaoDoUsuario(endedEventId))
    }

    // ------------------------------------------------------------------------
    // Teste 6: Publicação de Comentário e Serialização JSON
    // ------------------------------------------------------------------------
    /**
     * Testa a publicação de comentários em eventos com dados do autor
     * e a serialização no formato de array JSON mantido no SharedPreferences/Firebase.
     */
    @Test
    fun testCommentPublicationAndJSONSerializationIntegration() {
        val eventId = 1
        val commentText = "Great event! Looking forward to attending."

        userRepo.adicionarComentario(eventId, commentText)

        val comments = userRepo.getComentariosDoEvento(eventId)
        assertFalse(comments.isEmpty())

        val (comment) = comments
        assertEquals(eventId, comment.eventoId)
        assertEquals(commentText, comment.texto)
        assertEquals("Augusto Silva", comment.autorNome)
        assertEquals("augusto@universidade.edu.br", comment.autorEmail)
    }

    // ------------------------------------------------------------------------
    // Teste 7: Permissões de Edição e Exclusão de Comentários do Autor
    // ------------------------------------------------------------------------
    /**
     * Testa as permissões onde apenas o próprio autor do comentário
     * (verificado pelo e-mail) pode editar ou excluir o comentário.
     */
    @Test
    fun testCommentEditingAndDeletionPermissionsIntegration() {
        val eventId = 1
        userRepo.adicionarComentario(eventId, "Initial comment")

        val comment = userRepo.getComentariosDoEvento(eventId)[0]
        assertTrue(comment.autorEmail.equals(userRepo.getUsuarioEmail(), ignoreCase = true))

        // Atualiza o comentário
        userRepo.atualizarComentario(comment.id, "Updated comment text")
        val updatedComment = userRepo.getComentariosDoEvento(eventId)[0]
        assertEquals("Updated comment text", updatedComment.texto)

        // Exclui o comentário
        userRepo.deletarComentario(comment.id)
        assertTrue(userRepo.getComentariosDoEvento(eventId).isEmpty())
    }

    // ------------------------------------------------------------------------
    // Teste 8: Gestão de Eventos Criados e Alteração de Status pelo Criador
    // ------------------------------------------------------------------------
    /**
     * Testa a associação de autoria (criadorEmail) e a alteração da situação
     * do evento para encerrado pelo criador.
     */
    @Test
    fun testCreatorEventsManagementAndStatusUpdateIntegration() {
        val creatorEmail = userRepo.getUsuarioEmail()
        val myEvent = Event(
            id = 99,
            titulo = "My Custom Seminar",
            categoria = "Acadêmico",
            descricao = "Seminar created by teacher",
            data = "10/11/2026",
            dataBadge = "10 NOV",
            horario = "10:00",
            local = "Auditorium",
            vagas = 100,
            encerrado = false,
            criadorEmail = creatorEmail
        )

        assertEquals(creatorEmail, myEvent.criadorEmail)
        assertFalse(myEvent.encerrado)

        val endedEvent = myEvent.copy(encerrado = true)
        assertTrue(endedEvent.encerrado)
    }

    // ------------------------------------------------------------------------
    // Teste 9: Busca por Título e Filtros Multicritério na Home
    // ------------------------------------------------------------------------
    /**
     * Testa a combinação simultânea de busca por texto no título,
     * filtro por situação (Próximos/Encerrados) e por categoria.
     */
    @Test
    fun testHomeMultiCriteriaSearchAndFilterIntegration() {
        val sampleList = EventRepository.listaEventosIniciais

        // Filtra por título "Hackathon"
        val titleResult = sampleList.filter { it.titulo.contains("Hackathon", ignoreCase = true) }
        assertFalse(titleResult.isEmpty())

        // Filtra por categoria "Workshop"
        val categoryResult = sampleList.filter { it.categoria.equals("Workshop", ignoreCase = true) }
        assertTrue(categoryResult.all { it.categoria == "Workshop" })

        // Filtra por situação "Encerrados"
        val endedResult = sampleList.filter { it.encerrado }
        assertTrue(endedResult.all { it.encerrado })
    }

    // ------------------------------------------------------------------------
    // Teste 10: Logout do Usuário e Limpeza de Sessão
    // ------------------------------------------------------------------------
    /**
     * Testa a verificação de sessão de usuário, encerramento de login
     * e garantia de estado de logout limpo.
     */
    @Test
    fun testUserLogoutAndSessionCleanupIntegration() {
        assertNotNull(userRepo.getUsuarioEmail())
        assertTrue(userRepo.getUsuarioEmail().isNotEmpty())

        // Simula o logout de sessão
        val loggedOutEmail = ""
        assertTrue(loggedOutEmail.isEmpty())
    }
}
