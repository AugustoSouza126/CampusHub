package com.example.campushub.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.View
import android.widget.*
import com.example.campushub.data.model.Event
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.MapViewComponent
import com.example.campushub.ui.components.MediaHelper
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CreateEventScreen {

    fun construirTelaCriarEvento(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        eventoParaEditar: Event? = null,
        onEscolherFotoCapa: ((onFotoSelecionada: (String) -> Unit) -> Unit) = {},
        onSucesso: () -> Unit,
        onVoltar: () -> Unit
    ) {
        val ehEdicao = (eventoParaEditar != null)
        val tituloHeader = if (ehEdicao) "Editar Evento" else "Criar Novo Evento"
        val subHeader = if (ehEdicao) "Atualize as informações do evento no Firebase." else "Preencha as informações para publicar no campus."

        layout.addView(UIComponents.criarHeaderBar(context, tituloHeader, subHeader, onVoltar = onVoltar))

        var ehEncerrado = eventoParaEditar?.encerrado ?: false
        var imagemUrlSalva = eventoParaEditar?.imagemUrl ?: ""
        var dataBadgeCalculada = eventoParaEditar?.dataBadge ?: ""

        // =========================================================
        // SEÇÃO 1: INFORMAÇÕES BÁSICAS
        // =========================================================
        layout.addView(UIComponents.criarRotulo(context, "Informações Básicas"))

        val inputTitulo = UIComponents.criarCampoTexto(context, "Título do Evento (Ex: Workshop Android)", valorInicial = eventoParaEditar?.titulo ?: "")
        layout.addView(inputTitulo)

        layout.addView(UIComponents.criarRotulo(context, "Categoria"))
        val categoriasLista = listOf("Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança")
        val adapterCat = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            categoriasLista
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                if (v is TextView) {
                    v.setTextColor(AppColors.COLOR_TEXT_MAIN)
                    v.textSize = 15f
                    v.typeface = Typeface.DEFAULT_BOLD
                }
                return v
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                if (v is TextView) {
                    v.setTextColor(AppColors.COLOR_TEXT_MAIN)
                    v.setBackgroundColor(AppColors.COLOR_CARD_BG)
                    v.setPadding(32, 24, 32, 24)
                }
                return v
            }
        }
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerCategoria = Spinner(context).apply {
            adapter = adapterCat
            val index = categoriasLista.indexOf(eventoParaEditar?.categoria ?: "Acadêmico")
            if (index >= 0) setSelection(index)
            background = UIComponents.criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 8f)
            setPadding(UIComponents.dpToPx(context, 14), UIComponents.dpToPx(context, 12), UIComponents.dpToPx(context, 14), UIComponents.dpToPx(context, 12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
        }
        layout.addView(spinnerCategoria)

        val inputDescricao = UIComponents.criarCampoTexto(context, "Descrição e programação do evento...", valorInicial = eventoParaEditar?.descricao ?: "")
        layout.addView(inputDescricao)

        // =========================================================
        // SEÇÃO 2: DATA E HORÁRIO
        // =========================================================
        layout.addView(UIComponents.criarRotulo(context, "Data e Horário"))

        val inputData = UIComponents.criarCampoTexto(context, "Data (Clique para abrir o calendário)", valorInicial = eventoParaEditar?.data ?: "").apply {
            isFocusable = false
            isClickable = true
        }
        layout.addView(inputData)

        fun abrirDatePicker() {
            val cal = Calendar.getInstance()
            if (inputData.text.isNotEmpty() && inputData.text.toString().contains("/")) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateParsed = sdf.parse(inputData.text.toString())
                    if (dateParsed != null) cal.time = dateParsed
                } catch (_: Exception) {}
            }

            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    inputData.setText(dateStr)

                    val calBadge = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    val monthFormat = SimpleDateFormat("MMM", Locale.forLanguageTag("pt-BR"))
                    dataBadgeCalculada = String.format(Locale.getDefault(), "%02d %s", dayOfMonth, monthFormat.format(calBadge.time).uppercase(Locale.getDefault()))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dialog.show()
        }
        inputData.setOnClickListener { abrirDatePicker() }

        val inputHorario = UIComponents.criarCampoTexto(context, "Horário (Clique para abrir o relógio)", valorInicial = eventoParaEditar?.horario ?: "").apply {
            isFocusable = false
            isClickable = true
        }
        layout.addView(inputHorario)

        fun abrirTimePicker() {
            val cal = Calendar.getInstance()
            if (inputHorario.text.isNotEmpty() && inputHorario.text.toString().contains(":")) {
                try {
                    val parts = inputHorario.text.toString().split(":")
                    if (parts.size == 2) {
                        cal.set(Calendar.HOUR_OF_DAY, parts[0].trim().toInt())
                        cal.set(Calendar.MINUTE, parts[1].trim().toInt())
                    }
                } catch (_: Exception) {}
            }

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    inputHorario.setText(timeStr)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
        inputHorario.setOnClickListener { abrirTimePicker() }

        // =========================================================
        // SEÇÃO 3: LOCALIZAÇÃO
        // =========================================================
        layout.addView(UIComponents.criarRotulo(context, "Localização"))

        val inputLocal = UIComponents.criarCampoTexto(context, "Local / Prédio (Ex: Auditório Central)", valorInicial = eventoParaEditar?.local ?: "")
        layout.addView(inputLocal)

        val inputEndereco = UIComponents.criarCampoTexto(
            context,
            "Endereço Completo",
            valorInicial = eventoParaEditar?.endereco ?: "Av. Prof. Luciano Gualberto, 380 - Cidade Universitária"
        )
        layout.addView(inputEndereco)

        val latitude = eventoParaEditar?.latitude ?: -23.5583
        val longitude = eventoParaEditar?.longitude ?: -46.7314

        val mapaPreview = MapViewComponent.criarMapaLocalizacao(
            context = context,
            latitude = latitude,
            longitude = longitude,
            titulo = eventoParaEditar?.titulo?.ifEmpty { "Local do Evento" } ?: "Local do Evento",
            subtitulo = eventoParaEditar?.local?.ifEmpty { "Campus" } ?: "Campus",
            alturaDp = 130
        )
        layout.addView(mapaPreview)

        // =========================================================
        // SEÇÃO 4: DETALHES E IMAGEM DE CAPA
        // =========================================================
        layout.addView(UIComponents.criarRotulo(context, "Detalhes e Imagem"))

        val inputVagas = UIComponents.criarCampoTexto(context, "Quantidade de Vagas (Ex: 50)", InputType.TYPE_CLASS_NUMBER, valorInicial = if (ehEdicao) eventoParaEditar?.vagas.toString() else "")
        layout.addView(inputVagas)

        val ivPreviewCapa = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = if (imagemUrlSalva.isNotEmpty()) View.VISIBLE else View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UIComponents.dpToPx(context, 140)
            ).apply {
                setMargins(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
        }
        if (imagemUrlSalva.isNotEmpty()) {
            MediaHelper.carregarImagemParaImageView(ivPreviewCapa, imagemUrlSalva)
        }
        layout.addView(ivPreviewCapa)

        val btnEscolherFoto = UIComponents.criarBotao(context, "SELECIONAR FOTO DA GALERIA", primario = false) {
            onEscolherFotoCapa { caminhoLocal ->
                imagemUrlSalva = caminhoLocal
                MediaHelper.carregarImagemParaImageView(ivPreviewCapa, caminhoLocal)
                ivPreviewCapa.visibility = View.VISIBLE
            }
        }
        layout.addView(btnEscolherFoto)

        // =========================================================
        // SEÇÃO 5: PUBLICAÇÃO
        // =========================================================
        layout.addView(UIComponents.criarRotulo(context, "Situação"))

        val checkEncerrado = CheckBox(context).apply {
            text = "Evento Encerrado"
            textSize = 14f
            isChecked = ehEncerrado
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setOnCheckedChangeListener { _, isChecked ->
                ehEncerrado = isChecked
            }
            setPadding(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 12))
        }
        layout.addView(checkEncerrado)

        val textoBotaoSalvar = if (ehEdicao) "SALVAR ALTERAÇÕES" else "PUBLICAR EVENTO"
        val btnPublicar = UIComponents.criarBotao(context, textoBotaoSalvar, primario = true) {
            val titulo = inputTitulo.text.toString().trim()
            val descricao = inputDescricao.text.toString().trim()
            val data = inputData.text.toString().trim()
            val horario = inputHorario.text.toString().trim()
            val local = inputLocal.text.toString().trim()
            val endereco = inputEndereco.text.toString().trim()
            val vagasStr = inputVagas.text.toString().trim()
            val categoria = spinnerCategoria.selectedItem.toString()

            if (titulo.isEmpty()) {
                Toast.makeText(context, "Digite o título do evento.", Toast.LENGTH_SHORT).show()
                inputTitulo.requestFocus()
                return@criarBotao
            }

            if (descricao.isEmpty()) {
                Toast.makeText(context, "Digite a descrição do evento.", Toast.LENGTH_SHORT).show()
                inputDescricao.requestFocus()
                return@criarBotao
            }

            if (data.isEmpty()) {
                Toast.makeText(context, "Selecione a data do evento.", Toast.LENGTH_SHORT).show()
                abrirDatePicker()
                return@criarBotao
            }

            if (dataBadgeCalculada.isEmpty()) {
                dataBadgeCalculada = if (data.length >= 6) data.substring(0, 5) else data
            }

            if (horario.isEmpty()) {
                Toast.makeText(context, "Selecione o horário do evento.", Toast.LENGTH_SHORT).show()
                abrirTimePicker()
                return@criarBotao
            }

            if (local.isEmpty()) {
                Toast.makeText(context, "Digite o local do evento.", Toast.LENGTH_SHORT).show()
                inputLocal.requestFocus()
                return@criarBotao
            }

            try {
                val sdfCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dataHoraEvento = sdfCompleto.parse("$data $horario")
                val agora = java.util.Date()
                if (dataHoraEvento != null && dataHoraEvento.before(agora) && !ehEncerrado) {
                    Toast.makeText(context, "A data e o horário do evento não podem ser no passado.", Toast.LENGTH_LONG).show()
                    return@criarBotao
                }
            } catch (_: Exception) {}

            val vagas = vagasStr.toIntOrNull() ?: 50
            val eventoId = eventoParaEditar?.id ?: (System.currentTimeMillis() % 1000000).toInt()
            val criadorEmail = if (ehEdicao && eventoParaEditar?.criadorEmail?.isNotEmpty() == true) {
                eventoParaEditar.criadorEmail
            } else {
                userRepo.getUsuarioEmail()
            }

            val eventoSalvar = Event(
                id = eventoId,
                titulo = titulo,
                categoria = categoria,
                descricao = descricao,
                data = data,
                dataBadge = dataBadgeCalculada.uppercase(Locale.getDefault()),
                horario = horario,
                local = local,
                endereco = endereco,
                latitude = latitude,
                longitude = longitude,
                vagas = vagas,
                encerrado = ehEncerrado,
                criadorEmail = criadorEmail,
                imagemUrl = imagemUrlSalva
            )

            if (ehEdicao) {
                EventRepository.atualizarEvento(
                    evento = eventoSalvar,
                    onSuccess = {
                        Toast.makeText(context, "Evento atualizado com sucesso!", Toast.LENGTH_LONG).show()
                        onSucesso()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Erro ao atualizar evento: $error", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                EventRepository.adicionarEvento(
                    evento = eventoSalvar,
                    onSuccess = {
                        Toast.makeText(context, "Evento publicado com sucesso!", Toast.LENGTH_LONG).show()
                        onSucesso()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Erro ao publicar evento: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        layout.addView(btnPublicar)

        layout.addView(UIComponents.criarBotao(context, "CANCELAR", primario = false) {
            onVoltar()
        })
    }
}
