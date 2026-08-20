package com.wifitracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * একটি একটানা WiFi সংযোগ সেশন।
 * startTime: সংযোগ শুরু হওয়ার epoch millis সময়
 * endTime: সংযোগ শেষ হওয়ার (অথবা এখনো চলমান থাকলে সর্বশেষ আপডেট হওয়া) epoch millis সময়
 */
@Entity(tableName = "wifi_sessions")
data class WifiSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long
)
