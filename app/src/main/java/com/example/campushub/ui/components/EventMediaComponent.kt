package com.example.campushub.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.campushub.data.model.Event
import com.example.campushub.data.model.EventPhoto
import com.example.campushub.data.model.EventVideo
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.theme.AppColors
import java.io.File

/**
 * Componentes de UI reutilizáveis para exibir e gerenciar as Seções de Fotos, Áudios e Vídeos do Evento.
 * Restringe publicação exclusivamente a alunos inscritos no evento e gerencia o salvamento local.
 */
object EventMediaComponent {

    // =========================================================
    // SEÇÃO DE FOTOS DO EVENTO
    // =========================================================

    fun construirSecaoFotos(
        context: Context,
        layout: LinearLayout,
        evento: Event,
        userRepo: UserPreferencesRepository,
        onTirarFotoCamera: () -> Unit,
        onEscolherFotoGaleria: () -> Unit,
        onAtualizarTela: () -> Unit
    ) {
        val inscrito = userRepo.isInscrito(evento.id)

        val cardFotos = CardView(context).apply {
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

        val layoutFotos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvTitulo = TextView(context).apply {
            text = "📷 Fotos do Evento"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 4))
        }
        layoutFotos.addView(tvTitulo)

        val tvSub = TextView(context).apply {
            text = "Fotos publicadas pelos alunos participantes deste evento (Clique na foto para expandir)."
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 10))
        }
        layoutFotos.addView(tvSub)

        // Restrição: Apenas alunos inscritos podem publicar fotos
        if (inscrito) {
            val rowBotoesFoto = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            val btnCamera = UIComponents.criarBotao(context, "📷 TIRAR FOTO", primario = true) {
                onTirarFotoCamera()
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 44), 1f).apply {
                    setMargins(0, 0, UIComponents.dpToPx(context, 4), 0)
                }
            }
            rowBotoesFoto.addView(btnCamera)

            val btnGaleria = UIComponents.criarBotao(context, "🖼️ GALERIA", primario = false) {
                onEscolherFotoGaleria()
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 44), 1f).apply {
                    setMargins(UIComponents.dpToPx(context, 4), 0, 0, 0)
                }
            }
            rowBotoesFoto.addView(btnGaleria)

            layoutFotos.addView(rowBotoesFoto)
        } else {
            val tvInfoNaoInscrito = TextView(context).apply {
                text = "⚠️ Inscreva-se neste evento para poder tirar e enviar fotos."
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
            layoutFotos.addView(tvInfoNaoInscrito)
        }

        // Lista / Carrossel de Fotos Publicadas
        val fotosLista = userRepo.getFotosDoEvento(evento.id)

        if (fotosLista.isEmpty()) {
            val tvSemFotos = TextView(context).apply {
                text = "Nenhuma foto publicada ainda."
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
                gravity = Gravity.CENTER
            }
            layoutFotos.addView(tvSemFotos)
        } else {
            val scrollHorizontal = HorizontalScrollView(context).apply {
                isHorizontalScrollBarEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, UIComponents.dpToPx(context, 12), 0, 0)
                }
            }

            val rowGaleria = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val usuarioEmailAtual = userRepo.getUsuarioEmail()

            fotosLista.forEach { foto ->
                val file = File(foto.caminhoArquivo)
                if (file.exists()) {
                    val cardFotoItem = CardView(context).apply {
                        radius = UIComponents.dpToPx(context, 12).toFloat()
                        cardElevation = UIComponents.dpToPx(context, 2).toFloat()
                        useCompatPadding = true
                        setCardBackgroundColor(Color.parseColor("#F8FAFC"))
                        layoutParams = LinearLayout.LayoutParams(
                            UIComponents.dpToPx(context, 180),
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, UIComponents.dpToPx(context, 10), 0)
                        }
                    }

                    val containerFoto = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8)
                        )
                    }

                    val imgView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            UIComponents.dpToPx(context, 130)
                        )
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        try {
                            val bitmap = BitmapFactory.decodeFile(foto.caminhoArquivo)
                            setImageBitmap(bitmap)
                        } catch (_: Exception) {}

                        // Clique na imagem abre modal expandido
                        setOnClickListener {
                            abrirFotoExpandida(context, foto)
                        }
                    }
                    containerFoto.addView(imgView)

                    val tvAutor = TextView(context).apply {
                        text = "👤 ${foto.autorNome}"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AppColors.COLOR_TEXT_MAIN)
                        gravity = Gravity.CENTER
                        setPadding(0, UIComponents.dpToPx(context, 4), 0, 0)
                    }
                    containerFoto.addView(tvAutor)

                    val tvData = TextView(context).apply {
                        text = foto.dataPublicacao
                        textSize = 10f
                        setTextColor(AppColors.COLOR_TEXT_MUTED)
                        gravity = Gravity.CENTER
                    }
                    containerFoto.addView(tvData)

                    if (foto.autorEmail.equals(usuarioEmailAtual, ignoreCase = true) && usuarioEmailAtual.isNotEmpty()) {
                        val btnExcluir = TextView(context).apply {
                            text = "🗑️ Excluir Foto"
                            textSize = 11f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(Color.parseColor("#DC2626"))
                            gravity = Gravity.CENTER
                            setPadding(0, UIComponents.dpToPx(context, 4), 0, 0)
                            setOnClickListener {
                                userRepo.deletarFoto(foto.id)
                                Toast.makeText(context, "Foto excluída.", Toast.LENGTH_SHORT).show()
                                onAtualizarTela()
                            }
                        }
                        containerFoto.addView(btnExcluir)
                    }

                    cardFotoItem.addView(containerFoto)
                    rowGaleria.addView(cardFotoItem)
                }
            }

            scrollHorizontal.addView(rowGaleria)
            layoutFotos.addView(scrollHorizontal)
        }

        cardFotos.addView(layoutFotos)

        val paramsCardFotos = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 10), 0, UIComponents.dpToPx(context, 10))
        }

        layout.addView(cardFotos, paramsCardFotos)
    }

    /**
     * Exibe a foto em tela cheia/expandida em um diálogo sobreposto.
     */
    private fun abrirFotoExpandida(context: Context, foto: EventPhoto) {
        val file = File(foto.caminhoArquivo)
        if (!file.exists()) return

        val dialog = AlertDialog.Builder(context).create()
        val containerDialog = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16),
                UIComponents.dpToPx(context, 16)
            )
            setBackgroundColor(Color.parseColor("#0F172A"))
        }

        val imgExpandida = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UIComponents.dpToPx(context, 350)
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            try {
                val bitmap = BitmapFactory.decodeFile(foto.caminhoArquivo)
                setImageBitmap(bitmap)
            } catch (_: Exception) {}
        }
        containerDialog.addView(imgExpandida)

        val tvInfo = TextView(context).apply {
            text = "👤 ${foto.autorNome}  •  ${foto.dataPublicacao}"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, UIComponents.dpToPx(context, 8), 0, UIComponents.dpToPx(context, 8))
        }
        containerDialog.addView(tvInfo)

        val btnFechar = UIComponents.criarBotao(context, "❌ FECHAR FOTO", primario = true) {
            dialog.dismiss()
        }
        containerDialog.addView(btnFechar)

        dialog.setView(containerDialog)
        dialog.show()
    }

    // =========================================================
    // SEÇÃO DE VÍDEOS DO EVENTO
    // =========================================================

    fun construirSecaoVideos(
        context: Context,
        layout: LinearLayout,
        evento: Event,
        userRepo: UserPreferencesRepository,
        onGravarVideoCamera: () -> Unit,
        onEscolherVideoGaleria: () -> Unit,
        onAtualizarTela: () -> Unit
    ) {
        val inscrito = userRepo.isInscrito(evento.id)

        val cardVideos = CardView(context).apply {
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

        val layoutVideos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvTitulo = TextView(context).apply {
            text = "🎬 Vídeos do Evento"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 4))
        }
        layoutVideos.addView(tvTitulo)

        val tvSub = TextView(context).apply {
            text = "Grave um vídeo curto ou selecione da galeria para publicar no evento (Clique para assistir)."
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 10))
        }
        layoutVideos.addView(tvSub)

        // Restrição: Apenas alunos inscritos podem enviar/gravar vídeos
        if (inscrito) {
            val rowBotoesVideo = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            val btnCamera = UIComponents.criarBotao(context, "🎥 GRAVAR VÍDEO", primario = true) {
                onGravarVideoCamera()
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 44), 1f).apply {
                    setMargins(0, 0, UIComponents.dpToPx(context, 4), 0)
                }
            }
            rowBotoesVideo.addView(btnCamera)

            val btnGaleria = UIComponents.criarBotao(context, "🎬 GALERIA", primario = false) {
                onEscolherVideoGaleria()
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, UIComponents.dpToPx(context, 44), 1f).apply {
                    setMargins(UIComponents.dpToPx(context, 4), 0, 0, 0)
                }
            }
            rowBotoesVideo.addView(btnGaleria)

            layoutVideos.addView(rowBotoesVideo)
        } else {
            val tvInfoNaoInscrito = TextView(context).apply {
                text = "⚠️ Inscreva-se neste evento para poder gravar e enviar vídeos."
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
            layoutVideos.addView(tvInfoNaoInscrito)
        }

        // Lista de Vídeos Publicados
        val videosLista = userRepo.getVideosDoEvento(evento.id)

        if (videosLista.isEmpty()) {
            val tvSemVideos = TextView(context).apply {
                text = "Nenhum vídeo publicado para este evento ainda."
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
                gravity = Gravity.CENTER
            }
            layoutVideos.addView(tvSemVideos)
        } else {
            val scrollHorizontal = HorizontalScrollView(context).apply {
                isHorizontalScrollBarEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, UIComponents.dpToPx(context, 12), 0, 0)
                }
            }

            val rowGaleria = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val usuarioEmailAtual = userRepo.getUsuarioEmail()

            videosLista.forEach { video ->
                val file = File(video.caminhoArquivo)
                if (file.exists()) {
                    val cardVideoItem = CardView(context).apply {
                        radius = UIComponents.dpToPx(context, 12).toFloat()
                        cardElevation = UIComponents.dpToPx(context, 2).toFloat()
                        useCompatPadding = true
                        setCardBackgroundColor(Color.parseColor("#F8FAFC"))
                        layoutParams = LinearLayout.LayoutParams(
                            UIComponents.dpToPx(context, 190),
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, UIComponents.dpToPx(context, 10), 0)
                        }
                    }

                    val containerVideo = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8),
                            UIComponents.dpToPx(context, 8)
                        )
                    }

                    val btnAssistir = UIComponents.criarBotao(context, "▶️ ASSISTIR VÍDEO", primario = true) {
                        MediaHelper.abrirPlayerVideoDialog(context, video.caminhoArquivo, "Vídeo por ${video.autorNome}")
                    }.apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            UIComponents.dpToPx(context, 40)
                        )
                    }
                    containerVideo.addView(btnAssistir)

                    val tvAutor = TextView(context).apply {
                        text = "👤 ${video.autorNome}"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AppColors.COLOR_TEXT_MAIN)
                        gravity = Gravity.CENTER
                        setPadding(0, UIComponents.dpToPx(context, 4), 0, 0)
                    }
                    containerVideo.addView(tvAutor)

                    val tvData = TextView(context).apply {
                        text = video.dataPublicacao
                        textSize = 10f
                        setTextColor(AppColors.COLOR_TEXT_MUTED)
                        gravity = Gravity.CENTER
                    }
                    containerVideo.addView(tvData)

                    if (video.autorEmail.equals(usuarioEmailAtual, ignoreCase = true) && usuarioEmailAtual.isNotEmpty()) {
                        val btnExcluir = TextView(context).apply {
                            text = "🗑️ Excluir Vídeo"
                            textSize = 11f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(Color.parseColor("#DC2626"))
                            gravity = Gravity.CENTER
                            setPadding(0, UIComponents.dpToPx(context, 4), 0, 0)
                            setOnClickListener {
                                userRepo.deletarVideo(video.id)
                                Toast.makeText(context, "Vídeo excluído.", Toast.LENGTH_SHORT).show()
                                onAtualizarTela()
                            }
                        }
                        containerVideo.addView(btnExcluir)
                    }

                    cardVideoItem.addView(containerVideo)
                    rowGaleria.addView(cardVideoItem)
                }
            }

            scrollHorizontal.addView(rowGaleria)
            layoutVideos.addView(scrollHorizontal)
        }

        cardVideos.addView(layoutVideos)

        val paramsCardVideos = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 10), 0, UIComponents.dpToPx(context, 10))
        }

        layout.addView(cardVideos, paramsCardVideos)
    }

    // =========================================================
    // SEÇÃO DE ÁUDIOS DO EVENTO
    // =========================================================

    fun construirSecaoAudios(
        context: Context,
        layout: LinearLayout,
        evento: Event,
        userRepo: UserPreferencesRepository,
        onPedirPermissaoAudio: () -> Unit,
        onAtualizarTela: () -> Unit
    ) {
        val inscrito = userRepo.isInscrito(evento.id)

        val cardAudios = CardView(context).apply {
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

        val layoutAudios = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvTitulo = TextView(context).apply {
            text = "🎙️ Áudios do Evento"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 4))
        }
        layoutAudios.addView(tvTitulo)

        val tvSub = TextView(context).apply {
            text = "Grave um áudio curto sobre o evento, reproduza ou exclua seus próprios áudios."
            textSize = 13f
            setTextColor(AppColors.COLOR_TEXT_MUTED)
            setPadding(0, 0, 0, UIComponents.dpToPx(context, 10))
        }
        layoutAudios.addView(tvSub)

        // Restrição: Apenas alunos inscritos podem gravar áudio
        if (inscrito) {
            val temPermissaoAudio = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val tvStatusGravacao = TextView(context).apply {
                text = if (MediaHelper.isGravando()) "🔴 Gravando áudio..." else "🎙️ Clique abaixo para iniciar a gravação do seu áudio"
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (MediaHelper.isGravando()) Color.parseColor("#DC2626") else AppColors.COLOR_TEXT_MUTED)
                setPadding(0, 0, 0, UIComponents.dpToPx(context, 6))
            }
            layoutAudios.addView(tvStatusGravacao)

            if (MediaHelper.isGravando()) {
                val btnParar = UIComponents.criarBotao(context, "⏹️ PARAR E PUBLICAR ÁUDIO", primario = true, perigo = true) {
                    val caminho = MediaHelper.pararGravacaoAudio()
                    if (caminho != null) {
                        userRepo.adicionarAudio(evento.id, caminho, "00:15")
                        Toast.makeText(context, "Áudio gravado e publicado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Falha ao salvar áudio.", Toast.LENGTH_SHORT).show()
                    }
                    onAtualizarTela()
                }
                layoutAudios.addView(btnParar)
            } else {
                val btnGravar = UIComponents.criarBotao(context, "🎙️ GRAVAR NOVO ÁUDIO", primario = true) {
                    if (!temPermissaoAudio) {
                        onPedirPermissaoAudio()
                    } else {
                        val caminho = MediaHelper.iniciarGravacaoAudio(context)
                        if (caminho != null) {
                            Toast.makeText(context, "Gravação iniciada! Fale no microfone.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Não foi possível iniciar o microfone.", Toast.LENGTH_SHORT).show()
                        }
                        onAtualizarTela()
                    }
                }
                layoutAudios.addView(btnGravar)
            }
        } else {
            val tvInfoNaoInscrito = TextView(context).apply {
                text = "⚠️ Inscreva-se neste evento para poder gravar e enviar áudios."
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#D97706"))
                setPadding(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
            layoutAudios.addView(tvInfoNaoInscrito)
        }

        // Lista de Áudios Publicados
        val audiosLista = userRepo.getAudiosDoEvento(evento.id)

        if (audiosLista.isEmpty()) {
            val tvSemAudios = TextView(context).apply {
                text = "Nenhum áudio gravado para este evento ainda."
                textSize = 14f
                setTextColor(AppColors.COLOR_TEXT_MUTED)
                setPadding(0, UIComponents.dpToPx(context, 16), 0, UIComponents.dpToPx(context, 8))
                gravity = Gravity.CENTER
            }
            layoutAudios.addView(tvSemAudios)
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
            layoutAudios.addView(divisor)

            val usuarioEmailAtual = userRepo.getUsuarioEmail()

            audiosLista.forEach { audio ->
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
                    setCardBackgroundColor(Color.parseColor("#F8FAFC"))
                }

                val itemLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val rowHeader = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val tvAutor = TextView(context).apply {
                    text = "🎵 Áudio por ${audio.autorNome}"
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
                    text = audio.dataPublicacao
                    textSize = 12f
                    setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
                rowHeader.addView(tvData)

                itemLayout.addView(rowHeader)

                // Botão de Play/Stop Audio
                var tocando = false
                val btnPlay = UIComponents.criarBotao(context, "▶️ OUVIR ÁUDIO", primario = false) {
                    if (tocando) {
                        MediaHelper.pararAudio()
                        tocando = false
                        onAtualizarTela()
                    } else {
                        tocando = true
                        MediaHelper.reproduzirAudio(audio.caminhoArquivo) {
                            tocando = false
                            onAtualizarTela()
                        }
                    }
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        UIComponents.dpToPx(context, 40)
                    ).apply {
                        setMargins(0, UIComponents.dpToPx(context, 6), 0, UIComponents.dpToPx(context, 4))
                    }
                }
                itemLayout.addView(btnPlay)

                if (audio.autorEmail.equals(usuarioEmailAtual, ignoreCase = true) && usuarioEmailAtual.isNotEmpty()) {
                    val btnExcluir = TextView(context).apply {
                        text = "🗑️ Excluir meu áudio"
                        textSize = 12f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.parseColor("#DC2626"))
                        gravity = Gravity.END
                        setPadding(0, UIComponents.dpToPx(context, 4), 0, 0)
                        setOnClickListener {
                            MediaHelper.pararAudio()
                            userRepo.deletarAudio(audio.id)
                            Toast.makeText(context, "Áudio excluído.", Toast.LENGTH_SHORT).show()
                            onAtualizarTela()
                        }
                    }
                    itemLayout.addView(btnExcluir)
                }

                cardItem.addView(itemLayout)

                val paramsItem = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 6))
                }

                layoutAudios.addView(cardItem, paramsItem)
            }
        }

        cardAudios.addView(layoutAudios)

        val paramsCardAudios = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, UIComponents.dpToPx(context, 10), 0, UIComponents.dpToPx(context, 12))
        }

        layout.addView(cardAudios, paramsCardAudios)
    }
}
