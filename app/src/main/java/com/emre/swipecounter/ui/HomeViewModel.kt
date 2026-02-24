package com.emre.swipecounter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emre.swipecounter.data.SwipeDataStore
import com.emre.swipecounter.data.SwipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SwipeDataStore(application)
    private val database = com.emre.swipecounter.data.AppDatabase.getDatabase(application)
    private val repository = SwipeRepository(dataStore, database.swipeDao())

    init {
        viewModelScope.launch {
            repository.checkAndResetIfNewDay()
        }
    }

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
        
    val lastDayTotal: StateFlow<Int> = repository.lastDayTotal
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
    
    val dailyLimit: StateFlow<Int> = repository.dailyLimit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 500
        )
    
    /**
     * Returns true if the current swipe count has exceeded the daily limit
     */
    val isLimitExceeded: StateFlow<Boolean> = combine(
        repository.totalCount,
        repository.dailyLimit
    ) { count, limit ->
        count >= limit
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}

