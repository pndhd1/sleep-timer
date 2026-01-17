package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import io.github.pndhd1.sleeptimer.BuildConfig

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val bannerWidth = maxWidth.value.toInt()
        // Do not recalculate ad size on recomposition
        val adSize = remember { BannerAdSize.stickySize(context, bannerWidth) }

        AndroidView(
            factory = {
                BannerAdView(context).apply {
                    setAdUnitId(BuildConfig.AD_BANNER_UNIT_ID)
                    setAdSize(adSize)
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() },
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
        )
    }
}
