package com.emre.swipecounter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emre.swipecounter.data.SwipeDataStore
import com.emre.swipecounter.data.SwipeRepository
import com.emre.swipecounter.data.SwipeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data class for UI display
 */
data class DailyStat(
    val dayName: String,
    val count: Int
)

/**
 * App breakdown data
 */
data class AppBreakdownData(
    val packageName: String,
    val totalCount: Int
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SwipeDataStore(application)
    private val database = com.emre.swipecounter.data.AppDatabase.getDatabase(application)
    private val repository = SwipeRepository(dataStore, database.swipeDao())
    
    private val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

    // Current day stats from repository
    val totalSwipeCount: StateFlow<Int> = repository.totalCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val tiktokCount: StateFlow<Int> = repository.tiktokCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val instagramCount: StateFlow<Int> = repository.instagramCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isPremium: StateFlow<Boolean> = repository.isPremium
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Weekly Stats
    private val _weeklyStats = MutableStateFlow<List<DailyStat>>(emptyList())
    val weeklyStats: StateFlow<List<DailyStat>> = _weeklyStats.asStateFlow()

    // Total weekly swipes
    private val _totalWeeklySwipes = MutableStateFlow(0)
    val totalWeeklySwipes: StateFlow<Int> = _totalWeeklySwipes.asStateFlow()

    // App breakdown for today (uses DataStore data)
    private val _appBreakdown = MutableStateFlow<List<AppBreakdownData>>(emptyList())
    val appBreakdown: StateFlow<List<AppBreakdownData>> = _appBreakdown.asStateFlow()

    init {
        // Collect real history from DB
        viewModelScope.launch {
            // Get stats since 7 days ago
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val startTime = getStartOfDay(cal.timeInMillis)

            repository.getHistory(startTime).collect { entities ->
                // Aggregate entities by date
                val dailyMap = entities.groupBy { it.date }
                    .mapValues { entry -> entry.value.sumOf { it.count } }

                val stats = mutableListOf<DailyStat>()
                
                // Generate list for last 7 days (fill missing with 0)
                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStart = getStartOfDay(c.timeInMillis)
                    val dayName = dayNameFormat.format(c.time)
                    
                    val count = dailyMap[dateStart] ?: 0
                    stats.add(DailyStat(dayName, count))
                }
                
                _weeklyStats.value = stats
                _totalWeeklySwipes.value = stats.sumOf { it.count }
            }
        }
        
        // Collect app breakdown from DataStore using combine
        viewModelScope.launch {
            combine(
                repository.tiktokCount,
                repository.instagramCount
            ) { tiktok, instagram ->
                updateAppBreakdown(tiktok, instagram)
            }.collect()
        }
    }
    
    private fun getStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    private fun updateAppBreakdown(tiktok: Int, instagram: Int) {
        val breakdown = mutableListOf<AppBreakdownData>()
        if (tiktok > 0) {
            breakdown.add(AppBreakdownData("com.zhiliaoapp.musically", tiktok))
        }
        if (instagram > 0) {
            breakdown.add(AppBreakdownData("com.instagram.android", instagram))
        }
        _appBreakdown.value = breakdown
    }
}
