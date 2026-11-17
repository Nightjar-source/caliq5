package com.Nightjar.caliq5.ads

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.Nightjar.caliq5.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object CaliqAdsManager {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var calculationCount = 0

    // Cached Banner AdView instance to prevent ad reloading on keyboard toggle or navigation
    private var cachedAdView: AdView? = null
    var isBannerLoaded = false
        private set

    // Official Google Mobile Ads Test Unit IDs fallback
    private const val FALLBACK_TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val FALLBACK_TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"

    /**
     * Reuses the existing loaded Banner AdView to guarantee that the ad NEVER reloads
     * every time the virtual keyboard opens or closes.
     */
    fun getOrCreateBannerAdView(context: Context, onLoadedChange: (Boolean) -> Unit): AdView {
        val existing = cachedAdView
        if (existing != null) {
            (existing.parent as? ViewGroup)?.removeView(existing)
            onLoadedChange(isBannerLoaded)
            return existing
        }

        val adView = AdView(context)
        val bannerId = try {
            BuildConfig.ADMOB_BANNER_ID.ifBlank { FALLBACK_TEST_BANNER }
        } catch (_: Throwable) {
            FALLBACK_TEST_BANNER
        }
        adView.adUnitId = bannerId

        val displayMetrics = context.resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density
        val rawWidth = (widthPixels / density).toInt()

        val adSize = if (rawWidth >= 728) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 728)
        } else if (rawWidth >= 600) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, rawWidth.coerceAtMost(640))
        } else {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, rawWidth)
        }
        adView.setAdSize(adSize)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isBannerLoaded = true
                onLoadedChange(true)
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                isBannerLoaded = false
                onLoadedChange(false)
            }
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        cachedAdView = adView
        return adView
    }

    fun destroyBanner() {
        try {
            cachedAdView?.destroy()
            cachedAdView = null
            isBannerLoaded = false
        } catch (_: Throwable) {}
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = try {
            BuildConfig.ADMOB_INTERSTITIAL_ID.ifBlank { FALLBACK_TEST_INTERSTITIAL }
        } catch (_: Throwable) {
            FALLBACK_TEST_INTERSTITIAL
        }

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }

    /**
     * Triggered automatically whenever a calculation result is displayed.
     * Guaranteed to display an interstitial ad exactly once every 3 calculations.
     */
    fun onCalculationResultShown(activity: Activity) {
        calculationCount++
        if (calculationCount % 3 == 0) {
            showInterstitial(activity)
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdDismissed?.invoke()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onAdDismissed?.invoke()
        }
    }
}

/**
 * Material 3 Expressive Adaptive Banner Ad:
 * - Fluid oval/capsule shape (CircleShape).
 * - Preserved across virtual keyboard toggles without re-requesting ads.
 * - Zero height & zero alpha while loading so no empty gap or blank border is shown.
 * - Graceful fade-in once loaded without jumping or layout shift.
 */
@Composable
fun CaliqAdaptiveBanner(
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(CaliqAdsManager.isBannerLoaded) }
    val isDark = isSystemInDarkTheme()

    val alphaAnim by animateFloatAsState(
        targetValue = if (isAdLoaded) 1f else 0f,
        animationSpec = tween(300),
        label = "banner_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isAdLoaded) Modifier.padding(vertical = 1.dp)
                else Modifier.height(1.dp).graphicsLayer { alpha = 0f }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.88f else 0.94f),
            border = BorderStroke(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
            ),
            shadowElevation = 0.dp,
            modifier = Modifier
                .clip(CircleShape)
                .graphicsLayer { alpha = alphaAnim }
                .padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.wrapContentSize(),
                    factory = { context ->
                        CaliqAdsManager.getOrCreateBannerAdView(context) { loaded ->
                            isAdLoaded = loaded
                        }
                    },
                    update = {
                        // View stays active without reloading
                    },
                    onRelease = { adView ->
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        // Preserved in memory across keyboard & navigation states
                    }
                )
            }
        }
    }
}
