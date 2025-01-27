package com.elorrieta.alumnoclient.room.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elorrieta.alumnoclient.entity.User

@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    fun getAll() : List<UserRoom>

    @Query("UPDATE users SET lastLogged = 0")
    fun resetLastLogged()

    @Query("UPDATE users SET lastLogged = 1 WHERE email = :email")
    fun updateLastLogged(email: String)

    @Query("SELECT * FROM users ORDER BY lastLogged DESC LIMIT 1")
    fun getLastLoggedUser(): UserRoom?

    // Si ya existe un login para este correo, se va a reemplazar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userRoom: UserRoom?)

    // Si se pone que devuelva Int, devuelve 0 si no se ha realizado delete y X si se han eliminado X registros
    @Delete
    fun delete(userRoom: UserRoom): Int
}