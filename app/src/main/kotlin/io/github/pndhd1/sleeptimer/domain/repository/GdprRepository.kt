package io.github.pndhd1.sleeptimer.domain.repository

import io.github.pndhd1.sleeptimer.domain.model.GdprState
import kotlinx.coroutines.flow.Flow

interface GdprRepository {

    val state: Flow<GdprState>

    suspend fun setUserConsent(consent: Boolean)
}
