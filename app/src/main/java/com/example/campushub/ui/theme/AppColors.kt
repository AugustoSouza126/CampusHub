package com.example.campushub.ui.theme

import android.graphics.Color

/**
 * Paleta de cores oficial do CampusHub.
 * Design leve, vibrante e equilibrado:
 * - Fundo claro e limpo (#F1F5F9) com cards em branco puro (#FFFFFF).
 * - Acentos em Azul Real (#1E40AF e #2563EB) com fontes BRANCAS sobre botões e etiquetas.
 * - Leitura perfeita com texto escuro grafite (#0F172A) no corpo do aplicativo.
 */
object AppColors {
    val COLOR_PRIMARY = Color.parseColor("#1E40AF")       // Azul Real Vibrante (Cabeçalho e Botões)
    val COLOR_PRIMARY_LIGHT = Color.parseColor("#2563EB") // Azul Claro Vibrante (Selos e Destaques)
    val COLOR_ACCENT = Color.parseColor("#3B82F6")        // Azul Celeste
    val COLOR_BG = Color.parseColor("#F1F5F9")            // Fundo Claro e Leve
    val COLOR_CARD_BG = Color.parseColor("#FFFFFF")       // Superfície do Card (Branco)
    val COLOR_STROKE = Color.parseColor("#CBD5E1")        // Borda Definida
    val COLOR_TEXT_MAIN = Color.parseColor("#0F172A")     // Texto Grafite Escuro para leitura em Cards
    val COLOR_TEXT_MUTED = Color.parseColor("#64748B")    // Texto Secundário Muted
    val COLOR_GREEN_BG = Color.parseColor("#059669")      // Verde Esmeralda
    val COLOR_GREEN_TEXT = Color.WHITE                    // Texto Branco
    val COLOR_CHIP_BG = Color.parseColor("#2563EB")       // Fundo Categoria (Azul)
    val COLOR_CHIP_TEXT = Color.WHITE                     // Texto Categoria (Branco)
}
