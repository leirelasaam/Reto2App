package com.elorrieta.alumnoclient.room.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.sql.Date

@Dao
interface MeetingDao : Parcelable {

    @Query("SELECT * FROM meetings")
    fun getAll() : List<Meeting>

    @Insert
    suspend fun insertMeeting(meeting: Meeting): Long
}