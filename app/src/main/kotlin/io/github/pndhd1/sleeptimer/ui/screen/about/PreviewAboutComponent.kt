package io.github.pndhd1.sleeptimer.ui.screen.about

object PreviewAboutComponent : AboutComponent {

    override val state = AboutState(
        appVersion = "1.0.0",
        githubUrl = "https://github.com/example/repo",
    )

    override fun onBackClick() = Unit
}
