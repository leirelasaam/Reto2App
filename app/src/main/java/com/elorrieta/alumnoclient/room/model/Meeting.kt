package com.elorrieta.alumnoclient.room.model

import android.net.http.UrlRequest.Status
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "meetings")
data class Meeting(
    @PrimaryKey
    val day: Int,
    val time: Int,
    val status: Status
) {
}