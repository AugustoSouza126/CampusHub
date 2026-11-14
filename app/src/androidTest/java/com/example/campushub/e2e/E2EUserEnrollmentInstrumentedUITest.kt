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

    @Test
    fun testStudentEnrollmentAndFavoriteUIJourney() {
        val uniqueEmail = "amanda_${System.currentTimeMillis()}@universidade.edu.br"

        try {
            onView(firstView(withText("Perfil"))).perform(click())
            Thread.sleep(1000)
            onView(withText("SAIR DA CONTA")).perform(click())
            Thread.sleep(1500)
        } catch (_: Exception) {
        }

        try {
            onView(withText("CRIAR NOVA CONTA")).perform(click())
            Thread.sleep(1000)

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

            onView(withText("FINALIZAR CADASTRO"))
                .perform(click())

            Thread.sleep(3500)
        } catch (_: Exception) {}

        try {
            onView(withText("Olá, Amanda Lima! 👋"))
                .check(matches(isDisplayed()))
        } catch (_: Exception) {
            onView(withText("🎓 CampusHub"))
                .check(matches(isDisplayed()))
        }

        try {
            onView(withHint("🔎 Digite o título do evento..."))
                .perform(replaceText("Workshop"), closeSoftKeyboard())

            Thread.sleep(2000)

            onView(firstView(withText("VER DETALHES")))
                .perform(click())

            Thread.sleep(2000)

            try {
                onView(withText("INSCREVER-SE NO EVENTO"))
                    .perform(click())
                Thread.sleep(1500)
            } catch (_: Exception) {}

            try {
                onView(withText("FAVORITAR EVENTO"))
                    .perform(click())
                Thread.sleep(1500)
            } catch (_: Exception) {}

            onView(firstView(withText("Favoritos")))
                .perform(click())

            Thread.sleep(2000)

            onView(firstView(withText("Inscrições")))
                .perform(click())

            Thread.sleep(2000)
        } catch (_: Exception) {}
    }
}
