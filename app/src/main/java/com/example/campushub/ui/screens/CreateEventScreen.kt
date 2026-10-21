package com.example.campushub.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.widget.*
import com.example.campushub.data.model.Evento
import com.example.campushub.data.repository.EventRepository
import com.example.campushub.data.repository.UserPreferencesRepository
import com.example.campushub.ui.components.UIComponents
import com.example.campushub.ui.theme.AppColors
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Responsável por construir a Tela de Criação e Edição de Eventos do CampusHub.
 * Oferece seletores de data/hora (DatePicker e TimePicker), seleção de categoria,
 * preenchimento de localização, vagas e persistência no Firebase Firestore.
 */
object CreateEventScreen {

    /**
      Constrói e exibe o formulário de criação ou edição de evento.
     */
    fun construirTelaCriarEvento(
        context: Context,
        layout: LinearLayout,
        userRepo: UserPreferencesRepository,
        eventoParaEditar: Evento? = null,
        onSucesso: () -> Unit,
        onVoltar: () -> Unit
    ) {
        val ehEdicao = (eventoParaEditar != null)
        val tituloHeader = if (ehEdicao) "Editar Evento" else "Criar Novo Evento"
        val subHeader = if (ehEdicao) "Atualize as informações do evento no Firebase." else "Preencha as informações para publicar no campus."

        layout.addView(UIComponents.criarHeaderBar(context, tituloHeader, subHeader))

        var categoriaSelecionada = eventoParaEditar?.categoria ?: "Acadêmico"
        var ehEncerrado = eventoParaEditar?.encerrado ?: false

        // 1. TÍTULO DO EVENTO
        layout.addView(UIComponents.criarRotulo(context, "Título do Evento"))
        val inputTitulo = UIComponents.criarCampoTexto(context, "Ex: Workshop de Kotlin e Android", valorInicial = eventoParaEditar?.titulo ?: "")
        layout.addView(inputTitulo)

        // 2. SELEÇÃO DE CATEGORIA (Pílulas Horizontais)
        layout.addView(UIComponents.criarRotulo(context, "Categoria"))
        val scrollCategorias = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, UIComponents.dpToPx(context, 8))
            }
        }

        val rowCategorias = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val categoriasOpcoes = listOf("Acadêmico", "Workshop", "Hackathon", "Palestra", "Carreiras", "Segurança")
        val botoesCategorias = mutableListOf<Button>()

        fun atualizarEstiloCategorias() {
            botoesCategorias.forEachIndexed { index, btn ->
                val cat = categoriasOpcoes[index]
                val selecionada = (cat == categoriaSelecionada)
                if (selecionada) {
                    btn.background = UIComponents.criarShape(context, AppColors.COLOR_PRIMARY_LIGHT, Color.TRANSPARENT, 0, 20f)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.background = UIComponents.criarShape(context, Color.WHITE, AppColors.COLOR_STROKE, 1, 20f)
                    btn.setTextColor(AppColors.COLOR_TEXT_MUTED)
                }
            }
        }

        categoriasOpcoes.forEach { cat ->
            val btnCat = MaterialButton(context).apply {
                text = cat
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                isAllCaps = false
                cornerRadius = UIComponents.dpToPx(context, 20)
                setOnClickListener {
                    categoriaSelecionada = cat
                    atualizarEstiloCategorias()
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    UIComponents.dpToPx(context, 38)
                ).apply {
                    setMargins(0, 0, UIComponents.dpToPx(context, 8), 0)
                }
            }
            botoesCategorias.add(btnCat)
            rowCategorias.addView(btnCat)
        }
        atualizarEstiloCategorias()
        scrollCategorias.addView(rowCategorias)
        layout.addView(scrollCategorias)

        // 3. DESCRIÇÃO
        layout.addView(UIComponents.criarRotulo(context, "Descrição do Evento"))
        val inputDescricao = UIComponents.criarCampoTexto(context, "Informe os detalhes, palestrantes e programação...", valorInicial = eventoParaEditar?.descricao ?: "")
        layout.addView(inputDescricao)

        // 4. SELEÇÃO DE DATA (DatePicker Dialog)
        layout.addView(UIComponents.criarRotulo(context, "Data do Evento (Clique para selecionar no calendário)"))
        val inputData = UIComponents.criarCampoTexto(context, "📅 Clique para selecionar a data", valorInicial = eventoParaEditar?.data ?: "").apply {
            isFocusable = false
            isClickable = true
        }
        layout.addView(inputData)

        layout.addView(UIComponents.criarRotulo(context, "Selo de Data (Gerado automaticamente)"))
        val inputDataBadge = UIComponents.criarCampoTexto(context, "Ex: 25 NOV", valorInicial = eventoParaEditar?.dataBadge ?: "")
        layout.addView(inputDataBadge)

        fun abrirDatePicker() {
            val cal = Calendar.getInstance()
            if (inputData.text.isNotEmpty() && inputData.text.toString().contains("/")) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateParsed = sdf.parse(inputData.text.toString())
                    if (dateParsed != null) cal.time = dateParsed
                } catch (_: Exception) {}
            }

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    inputData.setText(dateStr)

                    val calBadge = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    val monthFormat = SimpleDateFormat("MMM", Locale.forLanguageTag("pt-BR"))
                    val badgeStr = String.format(Locale.getDefault(), "%02d %s", dayOfMonth, monthFormat.format(calBadge.time).uppercase(Locale.getDefault()))
                    inputDataBadge.setText(badgeStr)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        inputData.setOnClickListener { abrirDatePicker() }

        // 5. SELEÇÃO DE HORÁRIO (TimePicker Dialog)
        layout.addView(UIComponents.criarRotulo(context, "Horário de Início (Clique para selecionar no relógio)"))
        val inputHorario = UIComponents.criarCampoTexto(context, "⏰ Clique para selecionar o horário", valorInicial = eventoParaEditar?.horario ?: "").apply {
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

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    inputHorario.setText(timeStr)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        inputHorario.setOnClickListener { abrirTimePicker() }

        // 6. LOCAL
        layout.addView(UIComponents.criarRotulo(context, "Local do Evento"))
        val inputLocal = UIComponents.criarCampoTexto(context, "Ex: Auditório Central / Lab 02", valorInicial = eventoParaEditar?.local ?: "")
        layout.addView(inputLocal)

        // 7. QUANTIDADE DE VAGAS
        layout.addView(UIComponents.criarRotulo(context, "Quantidade de Vagas"))
        val inputVagas = UIComponents.criarCampoTexto(context, "Ex: 50", InputType.TYPE_CLASS_NUMBER, valorInicial = if (ehEdicao) eventoParaEditar?.vagas.toString() else "")
        layout.addView(inputVagas)

        // 8. CHECKBOX SITUAÇÃO (Encerrado)
        layout.addView(UIComponents.criarRotulo(context, "Situação"))
        val checkEncerrado = CheckBox(context).apply {
            text = "Este evento já ocorreu (marcar como Encerrado)"
            textSize = 14f
            isChecked = ehEncerrado
            setTextColor(AppColors.COLOR_TEXT_MAIN)
            setOnCheckedChangeListener { _, isChecked ->
                ehEncerrado = isChecked
            }
            setPadding(0, UIComponents.dpToPx(context, 4), 0, UIComponents.dpToPx(context, 12))
        }
        layout.addView(checkEncerrado)

        // BOTÃO PUBLICAR / SALVAR
        val textoBotaoSalvar = if (ehEdicao) "SALVAR ALTERAÇÕES NO FIREBASE" else "PUBLICAR EVENTO NO FIREBASE"
        val btnPublicar = UIComponents.criarBotao(context, textoBotaoSalvar, primario = true) {
            val titulo = inputTitulo.text.toString().trim()
            val descricao = inputDescricao.text.toString().trim()
            val data = inputData.text.toString().trim()
            var dataBadge = inputDataBadge.text.toString().trim()
            val horario = inputHorario.text.toString().trim()
            val local = inputLocal.text.toString().trim()
            val vagasStr = inputVagas.text.toString().trim()

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
                Toast.makeText(context, "Digite a data do evento.", Toast.LENGTH_SHORT).show()
                inputData.requestFocus()
                return@criarBotao
            }

            if (dataBadge.isEmpty()) {
                dataBadge = if (data.length >= 6) data.substring(0, 5) else data
            }

            if (horario.isEmpty()) {
                Toast.makeText(context, "Digite o horário do evento.", Toast.LENGTH_SHORT).show()
                inputHorario.requestFocus()
                return@criarBotao
            }

            if (local.isEmpty()) {
                Toast.makeText(context, "Digite o local do evento.", Toast.LENGTH_SHORT).show()
                inputLocal.requestFocus()
                return@criarBotao
            }

            val vagas = vagasStr.toIntOrNull() ?: 50
            val eventoId = eventoParaEditar?.id ?: (System.currentTimeMillis() % 1000000).toInt()
            val criadorEmail = if (ehEdicao && eventoParaEditar?.criadorEmail?.isNotEmpty() == true) {
                eventoParaEditar.criadorEmail
            } else {
                userRepo.getUsuarioEmail()
            }

            val eventoSalvar = Evento(
                id = eventoId,
                titulo = titulo,
                categoria = categoriaSelecionada,
                descricao = descricao,
                data = data,
                dataBadge = dataBadge.uppercase(Locale.getDefault()),
                horario = horario,
                local = local,
                vagas = vagas,
                encerrado = ehEncerrado,
                criadorEmail = criadorEmail
            )

            if (ehEdicao) {
                EventRepository.atualizarEvento(
                    evento = eventoSalvar,
                    onSuccess = {
                        Toast.makeText(context, "Evento atualizado com sucesso no Firebase!", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(context, "Evento publicado com sucesso no Firebase!", Toast.LENGTH_LONG).show()
                        onSucesso()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Erro ao publicar evento: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        layout.addView(btnPublicar)

        // BOTÃO CANCELAR
        layout.addView(UIComponents.criarBotao(context, "CANCELAR", primario = false) {
            onVoltar()
        })
    }
}
