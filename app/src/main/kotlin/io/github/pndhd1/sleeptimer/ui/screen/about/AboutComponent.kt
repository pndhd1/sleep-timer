package io.github.pndhd1.sleeptimer.ui.screen.about

interface AboutComponent {

    // Immutable state
    val state: AboutState

    fun onBackClick()
}

data class AboutState(
    val appVersion: String,
    val githubUrl: String,
)
