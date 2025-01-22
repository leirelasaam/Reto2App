package com.elorrieta.alumnoclient.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject

object Util {
    // Función genérica para envolver en try-catch
    fun <T> safeExecute(tag: String, activity: Activity, block: () -> T) {
        try {
            // Ejecutar el bloque de código
            block()
        } catch (e: Exception) {
            Log.e(tag, "Exception occurred: ", e)
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.app_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}