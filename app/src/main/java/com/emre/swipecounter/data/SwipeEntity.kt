package com.emre.swipecounter.data

import androidx.room.Entity

@Entity(tableName = "swipe_stats", primaryKeys = ["date", "packageName"])
data class SwipeEntity(
    val date: Long, // Start of day in millis (UTC)
    val packageName: String,
    val count: Int
)
