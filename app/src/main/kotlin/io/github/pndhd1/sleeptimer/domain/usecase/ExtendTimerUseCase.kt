package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Duration

@Inject
class ExtendTimerUseCase(
    private val activeTimerRepository: ActiveTimerRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(additionalDuration: Duration) {
        val settings = settingsRepository.timerSettings.first()
        activeTimerRepository.extendTimer(
            additionalDuration = additionalDuration,
            fadeOutSettings = settings.fadeOut,
        )
    }
}
