package io.github.pndhd1.sleeptimer.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.yandex.mobile.ads.banner.BannerAdSize
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

    // https://ads.yandex.com/helpcenter/en/dev/android/adaptive-sticky-banner
    // It is recommended to recalculate the size on initialization
    @Stable
    fun stickySize(context: Context, width: Int): BannerAdSize {
        isInitialized // Trigger recalculation
        return BannerAdSize.stickySize(context, width)
    }
}

@Composable
fun AdStickySizeInset(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    Layout(modifier) { _, constraints ->
        val adSize = YandexAdsState.stickySize(context, constraints.maxWidth)
        layout(adSize.width.dp.toPx().toInt(), adSize.height.dp.toPx().toInt()) {}
    }
}
