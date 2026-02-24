package com.emre.swipecounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "swipe_prefs")

class SwipeDataStore(private val context: Context) {

    companion object {
        private val TIKTOK_COUNT = intPreferencesKey("tiktok_count")
        private val INSTAGRAM_COUNT = intPreferencesKey("instagram_count")
        private val LAST_DAY_TOTAL = intPreferencesKey("last_day_count")
        private val LAST_OPEN_DATE = longPreferencesKey("last_open_date")
        private val DAILY_LIMIT = intPreferencesKey("daily_limit")
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val SHOW_OVERLAY_KEY = booleanPreferencesKey("show_overlay")
    }

    val tiktokCount: Flow<Int> = context.dataStore.data.map { prefs -> prefs[TIKTOK_COUNT] ?: 0 }
    val instagramCount: Flow<Int> = context.dataStore.data.map { prefs -> prefs[INSTAGRAM_COUNT] ?: 0 }
    val lastOpenDate: Flow<Long> = context.dataStore.data.map { prefs -> prefs[LAST_OPEN_DATE] ?: 0L }
    val lastDayTotal: Flow<Int> = context.dataStore.data.map { prefs -> prefs[LAST_DAY_TOTAL] ?: 0 }
    
    val dailyLimit: Flow<Int> = context.dataStore.data.map { prefs -> prefs[DAILY_LIMIT] ?: 500 }
    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[IS_PREMIUM] ?: false }
    val showOverlay: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[SHOW_OVERLAY_KEY] ?: true }

    suspend fun incrementTiktok() {
        context.dataStore.edit { prefs ->
            val current = prefs[TIKTOK_COUNT] ?: 0
            prefs[TIKTOK_COUNT] = current + 1
        }
    }
    


    suspend fun incrementInstagram() {
        context.dataStore.edit { prefs ->
            val current = prefs[INSTAGRAM_COUNT] ?: 0
            prefs[INSTAGRAM_COUNT] = current + 1
        }
    }

    suspend fun resetCounts(yesterdayTotal: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_DAY_TOTAL] = yesterdayTotal
            prefs[TIKTOK_COUNT] = 0
            prefs[INSTAGRAM_COUNT] = 0
        }
    }
    
    suspend fun saveLastOpenDate(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_OPEN_DATE] = timestamp
        }
    }
    
    suspend fun saveDailyLimit(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_LIMIT] = limit
        }
    }
    
    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = isPremium
        }
    }
    
    suspend fun setShowOverlay(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_OVERLAY_KEY] = show
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs[TIKTOK_COUNT] = 0
            prefs[INSTAGRAM_COUNT] = 0
            prefs[LAST_DAY_TOTAL] = 0
            // Optionally clear premium? No, usually persistent.
        }
    }
}
