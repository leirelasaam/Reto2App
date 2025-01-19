package com.elorrieta.alumnoclient.room.utils

import org.mindrot.jbcrypt.BCrypt

object BcryptUtils {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return BCrypt.checkpw(password, storedHash)
    }
}
