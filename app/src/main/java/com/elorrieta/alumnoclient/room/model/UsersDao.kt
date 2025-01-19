package com.elorrieta.alumnoclient.room.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    fun getAll() : List<User>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): User?

    // Si ya existe un login para este correo, se va a reemplazar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    // Si se pone que devuelva Int, devuelve 0 si no se ha realizado delete y X si se han eliminado X registros
    @Delete
    fun delete(user: User): Int
}