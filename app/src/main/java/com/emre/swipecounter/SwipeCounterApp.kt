package com.emre.swipecounter

import android.app.Application
import com.google.android.gms.ads.MobileAds


class SwipeCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RevenueCatManager.init(this)
        
        // Initialize AdMob SDK
        MobileAds.initialize(this) { initializationStatus ->
            android.util.Log.d("SwipeCounter", "AdMob initialized: $initializationStatus")
        }
    }
}
