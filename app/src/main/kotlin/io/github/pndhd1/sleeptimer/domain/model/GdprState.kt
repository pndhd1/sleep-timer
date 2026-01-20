package io.github.pndhd1.sleeptimer.domain.model

data class GdprState(
    val isApplicable: Boolean,
    val dialogShown: Boolean,
    val isConsentGiven: Boolean,
)
