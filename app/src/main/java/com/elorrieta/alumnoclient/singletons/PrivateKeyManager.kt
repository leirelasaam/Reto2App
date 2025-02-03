package com.elorrieta.alumnoclient.singletons

import android.content.Context
import com.elorrieta.alumnoclient.utils.AESUtil
import java.io.FileNotFoundException
import java.io.IOException
import javax.crypto.SecretKey

object  PrivateKeyManager {
    private var key: SecretKey? = null

    @Throws(FileNotFoundException::class, IOException::class)
    fun getKey(context: Context): SecretKey {
        key?.let {
            return it
        }

        key = AESUtil.loadKey(context)
        return key!!
    }
}