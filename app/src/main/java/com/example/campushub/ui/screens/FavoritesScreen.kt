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
 * Responsável por construir a Tela de Favoritos no CampusHub.
 * Filtra e exibe somente os eventos marcados pelo usuário com coração (❤️).
 */
object FavoritesScreen {

    /**
     * Constrói a interface visual da Tela de Favoritos.
     * Busca os IDs favoritados salvos nas preferências locais e carrega os dados atualizados do Firebase.
     */
    fun construirTelaFavoritos(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        onVerDetalhes: (Event) -> Unit,
        onExplorarEventos: () -> Unit
    ) {
        fun dpToPx(dp: Int): Int = UIComponents.dpToPx(context, dp)

        layout.addView(UIComponents.criarHeaderBar(context, "Meus Favoritos", "Eventos que você salvou para acompanhar."))

        val containerLista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(containerLista)

        val favoritosIds = userRepo.getFavoritos()

        EventRepository.getEventos(
            onSuccess = { lista ->
                containerLista.removeAllViews()
                val favoritosLista = lista.filter { favoritosIds.contains(it.id.toString()) }

                if (favoritosLista.isEmpty()) {
                    val tvVazio = TextView(context).apply {
                        text = "Você ainda não favoritou nenhum evento.\nClique no ❤️ dos eventos para salvar aqui!"
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
                    favoritosLista.forEach { evento ->
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
