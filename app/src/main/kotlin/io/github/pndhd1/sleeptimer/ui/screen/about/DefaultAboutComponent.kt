package io.github.pndhd1.sleeptimer.ui.screen.about

import com.arkivanov.decompose.ComponentContext
import io.github.pndhd1.sleeptimer.BuildConfig

private const val GITHUB_URL = "https://github.com/pndhd1/sleep-timer"

class DefaultAboutComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : AboutComponent, ComponentContext by componentContext {

    override val state = AboutState(
        appVersion = BuildConfig.VERSION_NAME,
        githubUrl = GITHUB_URL,
    )

    override fun onBackClick() {
        onBack()
    }
}
