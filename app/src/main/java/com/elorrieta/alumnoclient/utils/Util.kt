package com.elorrieta.alumnoclient.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject
import java.text.SimpleDateFormat
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

        if (calendar.before(startOfClasses) || calendar.after(endOfClasses)) {
            return -1
        }

        calendar.firstDayOfWeek = Calendar.MONDAY
        val diffInMillis = calendar.timeInMillis - startOfClasses.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

        return (diffInDays / 7) + 1
    }

    @SuppressLint("SimpleDateFormat")
    fun getWeekRange(week: Int): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        val startOfClasses = Calendar.getInstance().apply {
            set(2024, Calendar.SEPTEMBER, 2)
        }

        val daysOffset = (week - 1) * 7
        calendar.timeInMillis = startOfClasses.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)

        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 4)
        val endOfWeek = calendar.time

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        return Pair(dateFormat.format(startOfWeek), dateFormat.format(endOfWeek))
    }
}