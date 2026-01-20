package io.github.pndhd1.sleeptimer.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.domain.model.GdprState
import io.github.pndhd1.sleeptimer.domain.repository.GdprRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Locale

private val GdprConsentDialogShownKey = booleanPreferencesKey("gdpr_consent_dialog_shown")
private val GdprUserConsentKey = booleanPreferencesKey("gdpr_user_consent")

// EEA countries (EU + Iceland, Liechtenstein, Norway) + Switzerland + UK
private val GdprCountryCodes = setOf(
    // EU member states
    "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
    "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL",
    "PL", "PT", "RO", "SK", "SI", "ES", "SE",
    // EEA (non-EU)
    "IS", "LI", "NO",
    // Other GDPR-like regulations
    "CH", // Switzerland
    "GB", // United Kingdom
)

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class GdprRepositoryImpl(
    context: Context,
    private val preferences: DataStore<Preferences>,
) : GdprRepository {

    private val telephonyManager = context.getSystemService<TelephonyManager>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state = flow { emit(isGdprApplicable()) }
        .flatMapLatest { isApplicable ->
            if (!isApplicable) return@flatMapLatest flowOf(
                GdprState(
                    isApplicable = false,
                    dialogShown = true,
                    isConsentGiven = true,
                )
            )
            preferences.data.map { prefs ->
                GdprState(
                    isApplicable = true,
                    dialogShown = prefs[GdprConsentDialogShownKey] ?: false,
                    isConsentGiven = prefs[GdprUserConsentKey] ?: false,
                )
            }
        }

    override suspend fun setUserConsent(consent: Boolean) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[GdprConsentDialogShownKey] = true
                this[GdprUserConsentKey] = consent
            }
        }
    }

    private fun isGdprApplicable(): Boolean {
        val countryCode = detectCountryCode()
        return countryCode.uppercase() in GdprCountryCodes
    }

    private fun detectCountryCode(): String {
        // Priority 1: SIM card country
        telephonyManager?.simCountryIso?.takeIf { it.isNotBlank() }?.let { return it }

        // Priority 2: Network country
        telephonyManager?.networkCountryIso?.takeIf { it.isNotBlank() }?.let { return it }

        // Priority 3: Device locale
        return Locale.getDefault().country
    }
}
