package io.github.pndhd1.sleeptimer.ui.screens.bottomnav.widgets

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import io.github.pndhd1.sleeptimer.BuildConfig
import io.github.pndhd1.sleeptimer.utils.YandexAdsState

@Composable
fun BottomNavAdBanner(
    modifier: Modifier = Modifier,
    state: BottomNavAdBannerState = rememberBottomNavAdBannerState(),
) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val adSize = YandexAdsState.stickySize(context, maxWidth.value.toInt())
        SideEffect { state.bannerSize = adSize }
        AndroidView(
            factory = { context ->
                BannerAdView(context).apply {
                    setAdUnitId(BuildConfig.AD_BANNER_UNIT_ID)
                    setAdSize(adSize)
                    setBannerAdEventListener(object : BannerAdEventListener {
                        override fun onAdFailedToLoad(error: AdRequestError) {
                            // TODO: Send to analytics
                        }

                        override fun onAdLoaded() {
                            state.isBannerVisible = true
                        }

                        override fun onAdClicked() = Unit
                        override fun onImpression(impressionData: ImpressionData?) = Unit
                        override fun onLeftApplication() = Unit
                        override fun onReturnedToApplication() = Unit

                    })
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = {
                state.isBannerVisible = false
                it.destroy()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
        )
    }
}

@Stable
class BottomNavAdBannerState {

    var bannerSize by mutableStateOf<BannerAdSize?>(null)
    var isBannerVisible by mutableStateOf(false)
}

@Composable
fun rememberBottomNavAdBannerState() = remember { BottomNavAdBannerState() }
