package com.elorrieta.alumnoclient.utils

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject

object JSONUtil {

    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        registerModule(Jdk8Module())
    }

    const val TAG = "json.util"

    // Deserializar
    @Throws(Exception::class)
    inline fun <reified T> fromJson(json: String): T {
        Log.d(TAG, "Received JSON: $json")
        return objectMapper.readValue(json, T::class.java)
    }

    // Serializar
    @Throws(Exception::class)
    fun <T> toJson(value: T): String {
        Log.d(TAG, "Received object: $value")
        return objectMapper.writeValueAsString(value)
    }
}