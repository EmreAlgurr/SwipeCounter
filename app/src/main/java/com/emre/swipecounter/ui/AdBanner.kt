package com.emre.swipecounter.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.emre.swipecounter.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Ad Unit IDs
 * 
 * TODO: Closed Testing bitince aşağıdaki yorumu kaldır ve gerçek ID'yi aktif et!
 */
private object AdUnitIds {
    // Google's official test ID (Closed Testing süresince bu kullanılacak)
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    // Your real production ID (Production'a geçince bunu aktif et)
    private val PROD_BANNER = BuildConfig.ADMOB_BANNER_ID
    
    // ŞİMDİLİK: Closed Testing bitene kadar sadece test ID kullan
    val banner: String = TEST_BANNER
    
    // PRODUCTION'A GEÇİNCE: Aşağıdaki satırı aktif et, yukarıdakini sil
    // val banner: String
    //     get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
}

/**
 * AdMob Banner Ad Composable
 * 
 * Automatically uses test ads in debug builds and real ads in release builds.
 * This prevents accidental policy violations during testing.
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdUnitIds.banner
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("AdBanner", "Ad loaded successfully")
                    }
                    
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        android.util.Log.e("AdBanner", "Ad failed to load: ${error.message}")
                    }
                    
                    override fun onAdClicked() {
                        android.util.Log.d("AdBanner", "Ad clicked")
                    }
                }
                
                // Load the ad
                loadAd(AdRequest.Builder().build())
                adView = this
            }
        },
        update = { view ->
            // Reload ad if needed
        }
    )
}

/**
 * Adaptive Banner - automatically adjusts to screen width
 * Better for different device sizes
 */
@Composable
fun AdaptiveAdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdUnitIds.banner
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            val displayMetrics = ctx.resources.displayMetrics
            val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth)
            
            AdView(ctx).apply {
                setAdSize(adSize)
                setAdUnitId(adUnitId)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("AdBanner", "Adaptive ad loaded successfully")
                    }
                    
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        android.util.Log.e("AdBanner", "Adaptive ad failed to load: ${error.message}")
                    }
                }
                
                loadAd(AdRequest.Builder().build())
                adView = this
            }
        }
    )
}
