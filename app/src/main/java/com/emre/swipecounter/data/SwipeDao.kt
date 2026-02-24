package com.emre.swipecounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SwipeDao {
    @Query("SELECT * FROM swipe_stats WHERE date = :date AND packageName = :packageName")
    suspend fun getSwipeStat(date: Long, packageName: String): SwipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipeStat(swipeStat: SwipeEntity)

    @Query("SELECT * FROM swipe_stats WHERE date >= :startDate ORDER BY date ASC")
    fun getStatsSince(startDate: Long): Flow<List<SwipeEntity>>
    
    // For app breakdown
    @Query("SELECT * FROM swipe_stats WHERE date = :date")
    fun getStatsForDay(date: Long): Flow<List<SwipeEntity>>
}
