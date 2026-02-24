package com.emre.swipecounter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SwipeRepository(
    private val dataStore: SwipeDataStore,
    private val swipeDao: SwipeDao
) {

    val tiktokCount: Flow<Int> = dataStore.tiktokCount
    val instagramCount: Flow<Int> = dataStore.instagramCount
    
    val totalCount: Flow<Int> = combine(tiktokCount, instagramCount) { t, i -> t + i }
    
    val lastDayTotal: Flow<Int> = dataStore.lastDayTotal
    val dailyLimit: Flow<Int> = dataStore.dailyLimit
    val isPremium: Flow<Boolean> = dataStore.isPremium

    suspend fun checkAndResetIfNewDay() {
        val lastDate = dataStore.lastOpenDate.first()
        val now = System.currentTimeMillis()

        if (lastDate == 0L || !isSameDay(lastDate, now)) {
            // New day
            if (lastDate != 0L) {
                // Save yesterday's total
                val t = dataStore.tiktokCount.first()
                val i = dataStore.instagramCount.first()
                dataStore.resetCounts(t + i)
            } else {
                // First run logic or corrupted state
                 dataStore.resetCounts(0)
            }
            dataStore.saveLastOpenDate(now)
        }
    }

    suspend fun incrementSwipe(packageName: String) {
        checkAndResetIfNewDay()
        
        val newCount: Int
        if (packageName.contains("instagram")) {
            dataStore.incrementInstagram()
            newCount = dataStore.instagramCount.first()
        } else {
            // TikTok (musically, trill)
            dataStore.incrementTiktok()
            newCount = dataStore.tiktokCount.first()
        }
        
        // Update last open date
        val now = System.currentTimeMillis()
        dataStore.saveLastOpenDate(now)
        
        // Persist to Room DB
        val date = getStartOfDay(now)
        // Store the specific package name (simplified)
        val storedPackage = if (packageName.contains("instagram")) "com.instagram.android" else "com.zhiliaoapp.musically"
        swipeDao.insertSwipeStat(SwipeEntity(date, storedPackage, newCount))
    }
    
    // Get history for analysis (e.g. last 7 days)
    // Returns raw entities, ViewModel can aggregate
    fun getHistory(startDate: Long): Flow<List<SwipeEntity>> {
        return swipeDao.getStatsSince(startDate)
    }

    suspend fun updateDailyLimit(limit: Int) {
        dataStore.saveDailyLimit(limit)
    }
    
    suspend fun resetAllData() {
        dataStore.clearAll()
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date(t1)) == sdf.format(Date(t2))
    }
    
    private fun getStartOfDay(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
