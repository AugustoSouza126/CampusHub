package com.example.campushub.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.MapViewComponent
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.util.Locale

/**
 * Responsável por construir a Tela de Detalhes do Evento.
 * Exibe informações completas (data, horário, local, endereço, mapa MapLibre),
 * painel exclusivo para o criador do evento (editar/encerrar/excluir),
 * gerencia inscrição e favoritos, e oferece seções interativas de avaliação (1 a 5 estrelas)
 * e comentários dos alunos.
 */
object EventDetail {

    /**
     * Constrói a interface completa da Tela de Detalhes do Evento.
     */
    fun construirTelaDetalhes(
        context: Context,
        layout: LinearLayout,
        evento: Evento,
        userRepo: UserPreferencesRepository,
        onEditarEvento: (Evento) -> Unit = {},
        onAtualizarTela: () -> Unit,
        onVoltar: () -> Unit
    ) {
        layout.addView(UIComponents.criarHeaderBar(context, evento.titulo, "Categoria: ${evento.categoria}"))

        // 1. CARD DE INFORMAÇÕES BÁSICAS DO EVENTO
        val cardInfo = CardView(context).apply {
            radius = UIComponents.dpToPx(context, 16).toFloat()
            cardElevation = UIComponents.dpToPx(context, 4).toFloat()
            setContentPadding(
                UIComponents.dpToPx(context, 20),
                UIComponents.dpToPx(context, 20),
                UIComponents.dpToPx(context, 20),
                UIComponents.dpToPx(context, 20)
            )
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val innerInfo = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun criarLinhaInfo(icone: String, titulo: String, valor: String) {
            val tv = TextView(context).apply {
                text = "$icone  $titulo: $valor"
                textSize = 15f
                setTextColor(AppColors.COLOR_TEXT_MAIN)
                setPadding(0, UIComponents.dpToPx(context, 6), 0, UIComponents.dpToPx(context, 6))
            }
            innerInfo.addView(tv)
        }

        criarLinhaInfo("📅", "Data", evento.data)
        criarLinhaInfo("⏰", "Horário", evento.horario)
        criarLinhaInfo("📍", "Local", evento.local)
        if (evento.endereco.isNotEmpty()) {
            criarLinhaInfo("🏠", "Endereço", evento.endereco)
        }
        criarLinhaInfo("👥", "Vagas disponíveis", "${evento.vagas}")

        val tvDescHeader = TextView(context).apply {
            text = "Descrição do Evento"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 4))
        }
        innerInfo.addView(tvDescHeader)

        val tvDesc = TextView(context).apply {
            text = evento.descricao
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MAIN)
        }
        innerInfo.addView(tvDesc)

        cardInfo.addView(innerInfo)
        layout.addView(cardInfo)

        // 2. CARD DO MAPA (MAPLIBRE SDK)
        val cardMapa = CardView(context).apply {
            radius = UIComponents.dpToPx(context, 16).toFloat()
            cardElevation = UIComponents.dpToPx(context, 4).toFloat()
            setContentPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutMapa = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvMapaHeader = TextView(context).apply {
            text = "🗺️ Localização no Mapa"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 4))
        }
        layoutMapa.addView(tvMapaHeader)

        val tvEnderecoSub = TextView(context).apply {
            text = if (evento.endereco.isNotEmpty()) {
                "📍 ${evento.local}\n🏠 ${evento.endereco}"
            } else {
                "📍 ${evento.local}"
            }
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
        }
        layoutMapa.addView(tvEnderecoSub)

        // Renderiza o mapa interativo do MapLibre centralizado no ponto do evento
        val lat = if (evento.latitude != 0.0) evento.latitude else -23.55052
        val lng = if (evento.longitude != 0.0) evento.longitude else -46.633308

        val mapaView = MapViewComponent.criarMapaLocalizacao(
            context = context,
            latitude = lat,
            longitude = lng,
            titulo = evento.titulo,
            subtitulo = evento.local,
            alturaDp = 220
        )
        layoutMapa.addView(mapaView)

        // Botão para abrir a rota em aplicativo de mapa externo
        val btnAbrirExterno = UIComponents.criarBotao(context, "🧭 ABRIR ROTA NO MAPA EXTERNO", primario = false) {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(evento.titulo)})")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }
        layoutMapa.addView(btnAbrirExterno)

        cardMapa.addView(layoutMapa)

        val paramsCardMapa = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 10), 0, UIComponents.dpToPx(context, 10))
        }
        layout.addView(cardMapa, paramsCardMapa)

        // PAINEL DO CRIADOR DO EVENTO (Opções de Editar, Encerrar e Excluir)
        val ehCriador = evento.criadorEmail.equals(userRepo.getUsuarioEmail(), ignoreCase = true) && userRepo.getUsuarioEmail().isNotEmpty()
        if (ehCriador) {
            val cardCriador = CardView(context).apply {
                radius = UIComponents.dpToPx(context, 16).toFloat()
                cardElevation = UIComponents.dpToPx(context, 4).toFloat()
                setContentPadding(
                    UIComponents.dpToPx(context, 16),
                    UIComponents.dpToPx(context, 16),
                    UIComponents.dpToPx(context, 16),
                    UIComponents.dpToPx(context, 16)
                )
                useCompatPadding = true
                setCardBackgroundColor(Color.parseColor("#FEF3C7"))
            }

            val layoutCriador = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            val tvCriadorTitulo = TextView(context).apply {
                text = "🛠️ Painel do Criador do Evento"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
            layoutCriador.addView(tvCriadorTitulo)

            val rowBotoesCriador = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 3f
            }

            val btnEditar = UIComponents.criarBotao(context, "✏️ EDITAR", primario = true) {
                onEditarEvento(evento)
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                    setMargins(0, 0, UIComponents.dpToPx(context, 2), 0)
                }
            }
            rowBotoesCriador.addView(btnEditar)

            if (!evento.encerrado) {
                val btnEncerrar = UIComponents.criarBotao(context, "🏁 ENCERRAR", primario = false) {
                    EventRepository.encerrarEvento(
                        evento.id,
                        onSuccess = {
                            Toast.makeText(context, "Evento encerrado com sucesso!", Toast.LENGTH_SHORT).show()
                            onAtualizarTela()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                        setMargins(UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 2), 0)
                    }
                }
                rowBotoesCriador.addView(btnEncerrar)
            }

            val btnExcluir = UIComponents.criarBotao(context, "🗑️ EXCLUIR", primario = false, perigo = true) {
                EventRepository.deletarEvento(
                    evento.id,
                    onSuccess = {
                        Toast.makeText(context, "Evento excluído do Firebase!", Toast.LENGTH_SHORT).show()
                        onVoltar()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                    setMargins(UIComponents.dpToPx(context, 2), 0, 0, 0)
                }
            }
            rowBotoesCriador.addView(btnExcluir)

            layoutCriador.addView(rowBotoesCriador)
            cardCriador.addView(layoutCriador)

            val paramsCriador = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, UIComponents.dpToPx(context, 8), 0, UIComponents.dpToPx(context, 12))
            }

            layout.addView(cardCriador, paramsCriador)
        }

        // 3. BOTÃO DE FAVORITO
        val eFavorito = userRepo.isFavorito(evento.id)
        if (eFavorito) {
            layout.addView(UIComponents.criarBotao(context, "💔 DESFAVORITAR EVENTO", primario = false) {
                userRepo.removerFavorito(evento.id)
                Toast.makeText(context, "Removido dos favoritos.", Toast.LENGTH_SHORT).show()
                onAtualizarTela()
            })
        } else {
            layout.addView(UIComponents.criarBotao(context, "❤️ FAVORITAR EVENTO", primario = false) {
                userRepo.salvarFavorito(evento.id)
                Toast.makeText(context, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                onAtualizarTela()
            })
        }

        // 4. BOTÃO DE INSCRIÇÃO
        val inscrito = userRepo.isInscrito(evento.id)
        if (inscrito) {
            val tvStatus = TextView(context).apply {
                text = "✅ Sua inscrição está confirmada neste evento."
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_GREEN_TEXT)
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
            }
            layout.addView(tvStatus)

            layout.addView(UIComponents.criarBotao(context, "CANCELAR INSCRIÇÃO", primario = false, perigo = true) {
                userRepo.removerInscricao(evento.id)
                Toast.makeText(context, "Inscrição cancelada com sucesso.", Toast.LENGTH_SHORT).show()
                onAtualizarTela()
            })
        } else if (evento.encerrado) {
            val tvStatusEncerrado = TextView(context).apply {
                text = "🏁 Inscrições encerradas para este evento."
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
            }
            layout.addView(tvStatusEncerrado)
        } else {
            layout.addView(UIComponents.criarBotao(context, "INSCREVER-SE NO EVENTO") {
                userRepo.salvarInscricao(evento.id)
                Toast.makeText(context, "Inscrição realizada com sucesso!", Toast.LENGTH_SHORT).show()
                onAtualizarTela()
            })
        }

        // 5. SEÇÃO DE AVALIAÇÃO (De 1 a 5 Estrelas)
        construirSecaoAvaliacao(context, layout, evento, userRepo, inscrito, onAtualizarTela)

        // 6. SEÇÃO DE COMENTÁRIOS DOS ALUNOS
        construirSecaoComentarios(context, layout, evento, userRepo, onAtualizarTela)

        // 7. BOTÃO VOLTAR
        layout.addView(UIComponents.criarBotao(context, "VOLTAR", primario = false) {
            onVoltar()
        })
    }

    /**
     * Constrói o bloco visual de Avaliação por Estrelas (1 a 5), permitindo que
     * alunos inscritos atribuam ou alterem sua nota após o encerramento do evento.
     */
    private fun construirSecaoAvaliacao(
        context: Context,
        layout: LinearLayout,
        evento: Evento,
        userRepo: UserPreferencesRepository,
        inscrito: Boolean,
        onAtualizarTela: () -> Unit
    ) {
        val cardAvaliacao = CardView(context).apply {
            radius = UIComponents.dpToPx(context, 16).toFloat()
            cardElevation = UIComponents.dpToPx(context, 4).toFloat()
            setContentPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutAvaliacao = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvAvaliacaoTitulo = TextView(context).apply {
            text = "⭐ Avaliação do Evento"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 6))
        }
        layoutAvaliacao.addView(tvAvaliacaoTitulo)

        val (mediaNotas, totalAvaliacoes) = userRepo.getMediaAvaliacoes(evento.id)
        val tvMediaTexto = TextView(context).apply {
            text = if (totalAvaliacoes > 0) {
                String.format(Locale.getDefault(), "Média do Evento: ⭐ %.1f / 5.0 (%d avaliações)", mediaNotas, totalAvaliacoes)
            } else {
                "Este evento ainda não possui avaliações."
            }
            textSize = 14f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 10))
        }
        layoutAvaliacao.addView(tvMediaTexto)

        if (!evento.encerrado) {
            val tvInfoNaoEncerrado = TextView(context).apply {
                text = "ℹ️ A avaliação por estrelas estará disponível assim que o evento for encerrado."
                textSize = 13f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 4))
            }
            layoutAvaliacao.addView(tvInfoNaoEncerrado)
        } else if (!inscrito) {
            val tvInfoNaoInscrito = TextView(context).apply {
                text = "⚠️ Apenas alunos inscritos neste evento podem avaliá-lo."
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 4))
            }
            layoutAvaliacao.addView(tvInfoNaoInscrito)
        } else {
            val notaAtual = userRepo.getAvaliacaoDoUsuario(evento.id)
            val tvSuaNota = TextView(context).apply {
                text = if (notaAtual != null) "Sua nota atual: ⭐ $notaAtual / 5 (Clique abaixo para alterar)" else "Clique para atribuir sua nota (1 a 5 estrelas):"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppColors.COLOR_TEXT_MAIN)
                setPadding(0, UIComponents.dpToPx(context, 6), 0, UIComponents.dpToPx(context, 8))
            }
            layoutAvaliacao.addView(tvSuaNota)

            val rowEstrelas = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 5f
            }

            for (i in 1..5) {
                val ehNotaSelecionada = (notaAtual == i)
                val btnEstrela = MaterialButton(context).apply {
                    text = "⭐ $i"
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    isAllCaps = false
                    cornerRadius = UIComponents.dpToPx(context, 10)
                    backgroundTintList = null

                    if (ehNotaSelecionada) {
                        setBackgroundColor(Color.parseColor("#F59E0B"))
                        setTextColor(Color.WHITE)
                    } else {
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.parseColor("#D97706"))
                        strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#FCD34D"))
                        strokeWidth = UIComponents.dpToPx(context, 1)
                    }

                    setOnClickListener {
                        userRepo.salvarOuAtualizarAvaliacao(evento.id, i)
                        Toast.makeText(context, "Sua avaliação ($i estrelas) foi salva!", Toast.LENGTH_SHORT).show()
                        onAtualizarTela()
                    }

                    layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 44), 1f).apply {
                        setMargins(UIComponents.dpToPx(context, 2), 0, UIComponents.dpToPx(context, 2), 0)
                    }
                }
                rowEstrelas.addView(btnEstrela)
            }

            layoutAvaliacao.addView(rowEstrelas)
        }

        cardAvaliacao.addView(layoutAvaliacao)

        val paramsCardAvaliacao = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 12), 0, UIComponents.dpToPx(context, 12))
        }

        layout.addView(cardAvaliacao, paramsCardAvaliacao)
    }

    /**
     * Constrói a seção de Comentários, permitindo publicar novos comentários,
     * listar comentários existentes e dar permissão para editar/excluir comentários próprios.
     */
    private fun construirSecaoComentarios(
        context: Context,
        layout: LinearLayout,
        evento: Evento,
        userRepo: UserPreferencesRepository,
        onAtualizarTela: () -> Unit
    ) {
        val cardComentarios = CardView(context).apply {
            radius = UIComponents.dpToPx(context, 16).toFloat()
            cardElevation = UIComponents.dpToPx(context, 4).toFloat()
            setContentPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            useCompatPadding = true
            setCardBackgroundColor(AppColors.COLOR_CARD_BG)
        }

        val layoutComentarios = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvComentariosTitulo = TextView(context).apply {
            text = "💬 Comentários"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
        }
        layoutComentarios.addView(tvComentariosTitulo)

        val tvNovoComentarioRotulo = TextView(context).apply {
            text = "Comentar como ${userRepo.getUsuarioNome().ifEmpty { "Usuário" }}:"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 4))
        }
        layoutComentarios.addView(tvNovoComentarioRotulo)

        val inputComentario = UIComponents.criarCampoTexto(context, "Escreva seu comentário sobre o evento...")
        layoutComentarios.addView(inputComentario)

        val btnPublicar = UIComponents.criarBotao(context, "PUBLICAR COMENTÁRIO", primario = true) {
            val textoComentario = inputComentario.text.toString().trim()
            if (textoComentario.isEmpty()) {
                Toast.makeText(context, "Digite algo para comentar.", Toast.LENGTH_SHORT).show()
                return@criarBotao
            }

            userRepo.adicionarComentario(evento.id, textoComentario)
            Toast.makeText(context, "Comentário publicado!", Toast.LENGTH_SHORT).show()
            onAtualizarTela()
        }
        layoutComentarios.addView(btnPublicar)

        val comentariosLista = userRepo.getComentariosDoEvento(evento.id)

        if (comentariosLista.isEmpty()) {
            val tvSemComentarios = TextView(context).apply {
                text = "Seja o primeiro a comentar sobre este evento!"
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
                gravity = Gravity.CENTER
            }
            layoutComentarios.addView(tvSemComentarios)
        } else {
            val divisor = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    UIComponents.dpToPx(context, 1)
                ).apply {
                    setMargins(0, UIComponents.dpToPx(context, 12), 0, UIComponents.dpToPx(context, 12))
                }
                setBackgroundColor(AppColors.COLOR_STROKE)
            }
            layoutComentarios.addView(divisor)

            val usuarioEmailAtual = userRepo.getUsuarioEmail()

            comentariosLista.forEach { com ->
                val cardItem = CardView(context).apply {
                    radius = UIComponents.dpToPx(context, 12).toFloat()
                    cardElevation = UIComponents.dpToPx(context, 1).toFloat()
                    setContentPadding(
                        UIComponents.dpToPx(context, 12),
                        UIComponents.dpToPx(context, 12),
                        UIComponents.dpToPx(context, 12),
                        UIComponents.dpToPx(context, 12)
                    )
                    useCompatPadding = true
                    setCardBackgroundColor(Color.parseColor("#F1F5F9"))
                }

                val itemLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val rowHeader = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val tvAutor = TextView(context).apply {
                    text = "👤 ${com.autorNome}"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(AppColors.COLOR_TEXT_MAIN)
                }
                rowHeader.addView(tvAutor)

                val spacer = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                }
                rowHeader.addView(spacer)

                val tvData = TextView(context).apply {
                    text = com.dataPublicacao
                    textSize = 12f
                    setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
                rowHeader.addView(tvData)

                itemLayout.addView(rowHeader)

                var emEdicao = false
                val tvTexto = TextView(context).apply {
                    text = com.texto
                    textSize = 14f
                    setTextColor(AppColors.COLOR_TEXT_MAIN)
                    setPadding(0, UIComponents.dpToPx(context, 6), 0, UIComponents.dpToPx(context, 8))
                }
                itemLayout.addView(tvTexto)

                val containerEdicao = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                }

                val inputEdicao = UIComponents.criarCampoTexto(context, "Editar comentário...", valorInicial = com.texto)
                containerEdicao.addView(inputEdicao)

                val rowBotoesEdicao = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val btnSalvarEdicao = UIComponents.criarBotao(context, "SALVAR", primario = true) {
                    val novoTexto = inputEdicao.text.toString().trim()
                    if (novoTexto.isEmpty()) {
                        Toast.makeText(context, "O comentário não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                        return@criarBotao
                    }
                    userRepo.atualizarComentario(com.id, novoTexto)
                    Toast.makeText(context, "Comentário atualizado!", Toast.LENGTH_SHORT).show()
                    onAtualizarTela()
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                        setMargins(0, 0, UIComponents.dpToPx(context, 4), 0)
                    }
                }
                rowBotoesEdicao.addView(btnSalvarEdicao)

                val btnCancelarEdicao = UIComponents.criarBotao(context, "CANCELAR", primario = false) {
                    emEdicao = false
                    containerEdicao.visibility = View.GONE
                    tvTexto.visibility = View.VISIBLE
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 42), 1f).apply {
                        setMargins(UIComponents.dpToPx(context, 4), 0, 0, 0)
                    }
                }
                rowBotoesEdicao.addView(btnCancelarEdicao)

                containerEdicao.addView(rowBotoesEdicao)
                itemLayout.addView(containerEdicao)

                if (com.autorEmail.equals(usuarioEmailAtual, ignoreCase = true) && usuarioEmailAtual.isNotEmpty()) {
                    val rowAcoes = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.END
                    }

                    val btnEditar = TextView(context).apply {
                        text = "✏️ Editar"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AppColors.COLOR_TEXT_MAIN)
                        setPadding(
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 4),
                            UIComponents.dpToPx(context, 12),
                            UIComponents.dpToPx(context, 4)
                        )
                        setOnClickListener {
                            emEdicao = !emEdicao
                            if (emEdicao) {
                                tvTexto.visibility = View.GONE
                                containerEdicao.visibility = View.VISIBLE
                            } else {
                                tvTexto.visibility = View.VISIBLE
                                containerEdicao.visibility = View.GONE
                            }
                        }
                    }
                    rowAcoes.addView(btnEditar)

                    val btnExcluir = TextView(context).apply {
                        text = "🗑️ Excluir"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.parseColor("#DC2626"))
                        setPadding(
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 4),
                            UIComponents.dpToPx(context, 4),
                            UIComponents.dpToPx(context, 4)
                        )
                        setOnClickListener {
                            userRepo.deletarComentario(com.id)
                            Toast.makeText(context, "Comentário excluído.", Toast.LENGTH_SHORT).show()
                            onAtualizarTela()
                        }
                    }
                    rowAcoes.addView(btnExcluir)

                    itemLayout.addView(rowAcoes)
                }

                cardItem.addView(itemLayout)

                val paramsItem = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 6))
                }

                layoutComentarios.addView(cardItem, paramsItem)
            }
        }

        cardComentarios.addView(layoutComentarios)

        val paramsCardComentarios = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 12), 0, UIComponents.dpToPx(context, 16))
        }

        layout.addView(cardComentarios, paramsCardComentarios)
    }
}
