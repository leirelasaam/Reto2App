package com.elorrieta.alumnoclient.room.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.sql.Date

@Dao
interface MeetingDao {

    @Query("SELECT * FROM meetings")
    fun getAll() : List<User>

    @Query("UPDATE users SET lastLogged = 0")
    fun resetLastLogged()

    @Query("UPDATE users SET lastLogged = 1 WHERE email = :email")
    fun updateLastLogged(email: String)

    @Query("SELECT * FROM users ORDER BY lastLogged DESC LIMIT 1")
    fun getLastLoggedUser(): User?
}