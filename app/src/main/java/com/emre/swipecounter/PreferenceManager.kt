package com.emre.swipecounter

import android.content.Context

class PreferenceManager(context: Context) {

    private val prefs = context.getSharedPreferences("swipe_counter_prefs", Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) {
            prefs.edit().putBoolean("onboarding_completed", value).apply()
        }
}

