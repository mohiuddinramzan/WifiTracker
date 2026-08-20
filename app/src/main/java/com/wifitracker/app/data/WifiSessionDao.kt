package com.wifitracker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WifiSessionDao {

    @Insert
    suspend fun insert(session: WifiSession): Long

    @Update
    suspend fun update(session: WifiSession)

    @Query("SELECT * FROM wifi_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WifiSession?

    // rangeStart এবং rangeEnd এর সাথে যে সেশনগুলোর কোনো না কোনো ওভারল্যাপ আছে
    @Query("SELECT * FROM wifi_sessions WHERE endTime > :rangeStart AND startTime < :rangeEnd ORDER BY startTime ASC")
    suspend fun getSessionsInRange(rangeStart: Long, rangeEnd: Long): List<WifiSession>

    @Query("SELECT MIN(startTime) FROM wifi_sessions")
    suspend fun getFirstSessionStart(): Long?
}
