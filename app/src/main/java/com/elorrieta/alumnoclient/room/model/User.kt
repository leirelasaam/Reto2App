package com.elorrieta.alumnoclient.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val pin: String,
    val password: String
) {
}