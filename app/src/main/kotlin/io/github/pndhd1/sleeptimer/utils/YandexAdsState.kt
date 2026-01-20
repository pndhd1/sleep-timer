package io.github.pndhd1.sleeptimer.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.yandex.mobile.ads.common.MobileAds
import io.github.pndhd1.sleeptimer.BuildConfig

object YandexAdsState {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isInitialized = mutableStateOf(false)
    val isInitialized: State<Boolean> get() = _isInitialized

    private val _gdprConsentInitialized = mutableStateOf(false)
    val gdprConsentInitialized: State<Boolean> get() = _gdprConsentInitialized

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            mainHandler.post { _isInitialized.value = true }
        }

        // Disable Ad debug error indicator
        MobileAds.enableDebugErrorIndicator(BuildConfig.DEBUG)
    }

    // https://ads.yandex.com/helpcenter/en/dev/android/gdpr
    // GDPR: Pass user consent to Yandex Ads SDK
    // Must be called on every app launch after user makes their choice
    fun setUserConsent(consent: Boolean) {
        mainHandler.post {
            MobileAds.setUserConsent(consent)
            _gdprConsentInitialized.value = true
        }
    }
}
