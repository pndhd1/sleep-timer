package io.github.pndhd1.sleeptimer.ui.screens.about

interface AboutComponent {

    // Immutable state
    val state: AboutState

    fun onBackClick()
}

data class AboutState(
    val appVersion: String,
    val githubUrl: String,
)
