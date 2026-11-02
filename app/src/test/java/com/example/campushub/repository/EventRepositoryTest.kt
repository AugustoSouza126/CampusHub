package com.example.campushub.repository

import com.example.campushub.data.repository.EventRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para o repositório [EventRepository].
 * Gerencia a lista padrão de eventos e integração Firestore.
 */
class EventRepositoryTest {

    /**
     * Verifica se a lista inicial de eventos pré-cadastrados não está vazia e possui a quantidade correta.
     */
    @Test
    fun testInitialEventListIsNotEmpty() {
        val initialEventsList = EventRepository.listaEventosIniciais
        assertNotNull(initialEventsList)
        assertTrue(initialEventsList.isNotEmpty())
        assertEquals(7, initialEventsList.size)
    }

    /**
     * Valida que todos os eventos da lista pré-cadastrada contêm informações obrigatórias preenchidas.
     */
    @Test
    fun testInitialEventsContainCompleteInformation() {
        val initialEventsList = EventRepository.listaEventosIniciais
        for (eventItem in initialEventsList) {
            assertTrue(eventItem.titulo.isNotEmpty())
            assertTrue(eventItem.categoria.isNotEmpty())
            assertTrue(eventItem.data.isNotEmpty())
            assertTrue(eventItem.dataBadge.isNotEmpty())
            assertTrue(eventItem.horario.isNotEmpty())
            assertTrue(eventItem.local.isNotEmpty())
            assertTrue(eventItem.endereco.isNotEmpty())
            assertTrue(eventItem.vagas > 0)
            assertTrue(eventItem.latitude != 0.0)
            assertTrue(eventItem.longitude != 0.0)
        }
    }

    /**
     * Testa o filtro de eventos por categoria.
     */
    @Test
    fun testFilterEventsByCategory() {
        val initialEventsList = EventRepository.listaEventosIniciais

        val academicEvents = initialEventsList.filter { it.categoria.equals("Acadêmico", ignoreCase = true) }
        assertEquals(2, academicEvents.size)

        val workshopEvents = initialEventsList.filter { it.categoria.equals("Workshop", ignoreCase = true) }
        assertEquals(1, workshopEvents.size)

        val hackathonEvents = initialEventsList.filter { it.categoria.equals("Hackathon", ignoreCase = true) }
        assertEquals(1, hackathonEvents.size)

        val lectureEvents = initialEventsList.filter { it.categoria.equals("Palestra", ignoreCase = true) }
        assertEquals(1, lectureEvents.size)

        val careerEvents = initialEventsList.filter { it.categoria.equals("Carreiras", ignoreCase = true) }
        assertEquals(1, careerEvents.size)

        val securityEvents = initialEventsList.filter { it.categoria.equals("Segurança", ignoreCase = true) }
        assertEquals(1, securityEvents.size)
    }

    /**
     * Testa a filtragem por situação do evento (Encerrado vs. Próximos).
     */
    @Test
    fun testFilterEventsByStatus() {
        val initialEventsList = EventRepository.listaEventosIniciais

        val endedEvents = initialEventsList.filter { it.encerrado }
        assertEquals(1, endedEvents.size)
        assertEquals(0, endedEvents.first().id)

        val upcomingEvents = initialEventsList.filter { !it.encerrado }
        assertEquals(6, upcomingEvents.size)
    }

    /**
     * Testa a busca de eventos por palavra-chave no título.
     */
    @Test
    fun testSearchEventsByTitle() {
        val initialEventsList = EventRepository.listaEventosIniciais

        val seminarSearchResults = initialEventsList.filter { it.titulo.contains("Seminário", ignoreCase = true) }
        assertEquals(1, seminarSearchResults.size)

        val hackathonSearchResults = initialEventsList.filter { it.titulo.contains("Hackathon", ignoreCase = true) }
        assertEquals(1, hackathonSearchResults.size)

        val nonExistingResults = initialEventsList.filter { it.titulo.contains("Inexistente 123", ignoreCase = true) }
        assertTrue(nonExistingResults.isEmpty())
    }

    /**
     * Testa a busca de evento por identificador único (ID).
     */
    @Test
    fun testFindEventById() {
        val initialEventsList = EventRepository.listaEventosIniciais
        val targetEvent = initialEventsList.find { it.id == 1 }
        assertNotNull(targetEvent)
        assertEquals("Semana Acadêmica 2026", targetEvent?.titulo)
    }
}
