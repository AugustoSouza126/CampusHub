package com.example.campushub.ui.screens

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.EventCard
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors

/**
 * Responsável por construir a Tela de Minhas Inscrições do CampusHub.
 * Filtra e exibe os eventos em que o usuário confirmou presença/inscricão.
 */
object SubscriptionsScreen {

    /**
     * Constrói a interface visual da Tela de Inscrições.
     * Busca as inscrições armazenadas no repositório do usuário e lista os eventos correspondentes.
     */
    fun construirTelaMeusEventos(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onVerDetalhes: (Event) -> Unit,
        onExplorarEventos: () -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        layout.addView(UIComponents.criarHeaderBar(context, "Minhas Inscrições", "Acompanhe todos os eventos em que você se inscreveu."))

        val containerLista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(containerLista)

        val inscritosIds = userRepo.getInscricoes()

        EventRepository.getEventos(
            onSuccess = { lista ->
                containerLista.removeAllViews()
                val meusEventosLista = lista.filter { inscritosIds.contains(it.id.toString()) }

                if (meusEventosLista.isEmpty()) {
                    val tvVazio = TextView(context).apply {
                        text = "Você ainda não está inscrito em nenhum evento."
                        textSize = 15f
                        setTextColor(AppColors.COLOR_TEXT_MUTED)
                        setPadding(0, dpToPx(30), 0, dpToPx(20))
                        gravity = Gravity.CENTER
                    }
                    containerLista.addView(tvVazio)

                    containerLista.addView(UIComponents.criarBotao(context, "EXPLORAR EVENTOS DISPONÍVEIS") {
                        onExplorarEventos()
                    })
                } else {
                    meusEventosLista.forEach { evento ->
                        EventCard.adicionarCardEvento(
                            context = context,
                            parent = containerLista,
                            evento = evento,
                            userRepo = userRepo,
                            onVerDetalhes = onVerDetalhes
                        )
                    }
                }
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
