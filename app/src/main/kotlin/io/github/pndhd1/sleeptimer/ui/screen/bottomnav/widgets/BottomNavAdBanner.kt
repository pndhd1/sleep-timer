package io.github.pndhd1.sleeptimer.ui.screen.bottomnav.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import io.github.pndhd1.sleeptimer.BuildConfig
import io.github.pndhd1.sleeptimer.utils.exceptions.AdLoadException

// does not support resizing after creation
@Composable
fun BottomNavAdBanner(
    size: BannerAdSize,
    modifier: Modifier = Modifier,
    onAdVisibilityChanged: (isVisible: Boolean) -> Unit = {},
) {
    if (LocalInspectionMode.current) return
    AndroidView(
        factory = { context ->
            BannerAdView(context).apply {
                setAdUnitId(BuildConfig.AD_BANNER_UNIT_ID)
                setAdSize(size)
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() = onAdVisibilityChanged(true)

                    override fun onAdFailedToLoad(error: AdRequestError) =
                        Firebase.crashlytics.recordException(
                            AdLoadException(error.code, error.description)
                        )

                    override fun onAdClicked() = Unit
                    override fun onImpression(impressionData: ImpressionData?) = Unit
                    override fun onLeftApplication() = Unit
                    override fun onReturnedToApplication() = Unit

                })
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = {
            onAdVisibilityChanged(false)
            it.destroy()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(size.height.dp),
    )
}
