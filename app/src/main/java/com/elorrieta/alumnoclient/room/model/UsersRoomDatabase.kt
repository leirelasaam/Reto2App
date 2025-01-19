package com.elorrieta.alumnoclient.room.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
abstract class UsersRoomDatabase: RoomDatabase() {

    companion object {
        @Volatile private var instance : UsersRoomDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase (context).also { instance = it}
        }

        private fun buildDatabase (context: Context) = Room.databaseBuilder(context, UsersRoomDatabase::class.java, "usersDatabase").build()
    }

    abstract fun usersDao() : UsersDao
}