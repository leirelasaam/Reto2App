package com.elorrieta.alumnoclient.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject
import java.util.Calendar

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

    fun getCurrentWeek(): Int {
        val calendar = Calendar.getInstance()

        // 2 de septiembre de 2024 (lunes) es el primer dia
        val startOfClasses = Calendar.getInstance().apply {
            set(2024, Calendar.SEPTEMBER, 2)
        }

        // 30 de mayo de 2025 (viernes) es el último día de clases
        val endOfClasses = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 30)
        }

        // No lectivo, retorna -1
        if (calendar.before(startOfClasses) || calendar.after(endOfClasses)) {
            return -1
        }

        // Se selecciona el lunes de la semana actual
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Calcular la diferencia en milisegundos entre las dos fechas
        val diffInMillis = calendar.timeInMillis - startOfClasses.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

        return (diffInDays / 7) + 1
    }
}