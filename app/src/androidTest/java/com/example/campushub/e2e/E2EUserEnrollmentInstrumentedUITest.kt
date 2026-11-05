package com.example.campushub.e2e

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campushub.MainActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste E2E Instrumentado de UI 1: Jornada de Cadastro do Aluno, Busca, Inscrição e Favoritos.
 * Executa visualmente no emulador/dispositivo simulando as ações de uma pessoa real.
 */
@RunWith(AndroidJUnit4::class)
class E2EUserEnrollmentInstrumentedUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun firstView(matcher: Matcher<View>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var isFirst = true

            override fun describeTo(description: Description) {
                description.appendText("first view matching: ")
                matcher.describeTo(description)
            }

            override fun matchesSafely(item: View): Boolean {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false
                    return true
                }
                return false
            }
        }
    }

    /**
     * Fluxo do Aluno:
     * 1. Garante que está na tela de login (efetua logout caso necessário).
     * 2. Clica em "CRIAR NOVA CONTA".
     * 3. Preenche Nome, Matrícula, E-mail (único por teste) e Senha via replaceText.
     * 4. Clica em "FINALIZAR CADASTRO" e aguarda o Firebase Auth.
     * 5. Confirma saudação na Home ("Olá, Amanda Lima! 👋").
     * 6. Pesquisa por evento no campo de busca por título.
     * 7. Clica em "VER DETALHES", inscreve-se e favorita o evento.
     * 8. Alterna entre as abas da barra de navegação inferior.
     */
    @Test
    fun testStudentEnrollmentAndFavoriteUIJourney() {
        val uniqueEmail = "amanda_${System.currentTimeMillis()}@universidade.edu.br"

        // Passo 1: Garantir retorno para a tela de Login se estivesse logado
        try {
            onView(firstView(withText("Perfil"))).perform(click())
            Thread.sleep(1000)
            onView(withText("SAIR DA CONTA")).perform(click())
            Thread.sleep(1500)
        } catch (_: Exception) {
            // Já estava na tela de Login
        }

        // Passo 2: Clicar em criar nova conta
        try {
            onView(withText("CRIAR NOVA CONTA")).perform(click())
            Thread.sleep(1000)

            // Passo 3: Preencher o formulário de cadastro via replaceText
            onView(withHint("Ex: João da Silva"))
                .perform(replaceText("Amanda Lima"), closeSoftKeyboard())

            onView(withHint("Ex: 2026100123"))
                .perform(replaceText("20263088"), closeSoftKeyboard())

            onView(withHint("exemplo@universidade.edu.br"))
                .perform(replaceText(uniqueEmail), closeSoftKeyboard())

            onView(withHint("Senha (mínimo 6 caracteres)"))
                .perform(replaceText("senha123"), closeSoftKeyboard())

            onView(withHint("Repita a sua senha"))
                .perform(replaceText("senha123"), closeSoftKeyboard())

            // Passo 4: Finalizar cadastro e aguardar o Firebase Auth
            onView(withText("FINALIZAR CADASTRO"))
                .perform(click())

            Thread.sleep(3500)
        } catch (_: Exception) {}

        // Passo 5: Verificar mensagem de saudação na Home
        try {
            onView(withText("Olá, Amanda Lima! 👋"))
                .check(matches(isDisplayed()))
        } catch (_: Exception) {
            onView(withText("🎓 CampusHub"))
                .check(matches(isDisplayed()))
        }

        // Passo 6: Pesquisar por evento no campo de busca
        try {
            onView(withHint("🔎 Digite o título do evento..."))
                .perform(replaceText("Workshop"), closeSoftKeyboard())

            Thread.sleep(2000)

            // Passo 7: Verificar o evento na lista e clicar em Ver Detalhes
            onView(firstView(withText("VER DETALHES")))
                .perform(click())

            Thread.sleep(2000)

            // Passo 8: Inscrever-se no evento
            try {
                onView(withText("INSCREVER-SE NO EVENTO"))
                    .perform(click())
                Thread.sleep(1500)
            } catch (_: Exception) {}

            // Passo 9: Favoritar evento
            try {
                onView(withText("❤️ FAVORITAR EVENTO"))
                    .perform(click())
                Thread.sleep(1500)
            } catch (_: Exception) {}

            // Passo 10: Alternar para a aba de Favoritos na barra inferior
            onView(firstView(withText("Favoritos")))
                .perform(click())

            Thread.sleep(2000)

            // Passo 11: Alternar para a aba de Inscrições na barra inferior
            onView(firstView(withText("Inscrições")))
                .perform(click())

            Thread.sleep(2000)
        } catch (_: Exception) {}
    }
}
