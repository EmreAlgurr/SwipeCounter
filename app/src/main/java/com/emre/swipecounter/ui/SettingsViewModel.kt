package com.emre.swipecounter.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emre.swipecounter.RevenueCatManager
import com.emre.swipecounter.data.SwipeDataStore
import com.emre.swipecounter.data.SwipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SwipeDataStore(application)
    private val database = com.emre.swipecounter.data.AppDatabase.getDatabase(application)
    private val repository = SwipeRepository(dataStore, database.swipeDao())

    val dailyLimit: StateFlow<Int> = repository.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 500)
        
    val isPremium: StateFlow<Boolean> = RevenueCatManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val totalSwipeCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val showOverlay: StateFlow<Boolean> = dataStore.showOverlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateShowOverlay(show: Boolean) {
        viewModelScope.launch {
            dataStore.setShowOverlay(show)
        }
    }

    fun updateDailyLimit(limit: Int) {
        viewModelScope.launch {
            repository.updateDailyLimit(limit)
        }
    }
    
    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
    
    fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        RevenueCatManager.restorePurchases(onSuccess, onError)
    }
}
