package com.Nightjar.caliq5.ads

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
    private var lastAdShowTime = 0L
    private const val MIN_INTERVAL_MS = 60_000L // Minimum 1 minute between interstitial impressions

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = BuildConfig.ADMOB_INTERSTITIAL_ID

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

    fun showInterstitial(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        val now = System.currentTimeMillis()
        if (now - lastAdShowTime < MIN_INTERVAL_MS) {
            onAdDismissed?.invoke()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShowTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
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

@Composable
fun CaliqAdaptiveBanner(
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    AnimatedVisibility(
        visible = isAdLoaded,
        enter = fadeIn(androidx.compose.animation.core.tween(250)) + expandVertically(androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)),
        exit = fadeOut(androidx.compose.animation.core.tween(180)) + shrinkVertically(androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.85f else 0.92f),
                border = BorderStroke(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
                ),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.wrapContentSize(),
                        factory = { context ->
                            val adView = AdView(context)
                            adView.adUnitId = BuildConfig.ADMOB_BANNER_ID

                            // Official AdMob Guidelines for Tablets, Large Screens, Foldables & Phones
                            val displayMetrics = context.resources.displayMetrics
                            val widthPixels = displayMetrics.widthPixels.toFloat()
                            val density = displayMetrics.density
                            val rawWidth = (widthPixels / density).toInt()

                            // Standard Tablet Leaderboard is 728dp width. For phones, use anchored adaptive width.
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
                                    isAdLoaded = true
                                }
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isAdLoaded = false
                                }
                            }
                            val adRequest = AdRequest.Builder().build()
                            adView.loadAd(adRequest)
                            adView
                        },
                        onRelease = { adView ->
                            adView.destroy()
                        }
                    )
                }
            }
        }
    }
}
