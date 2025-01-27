package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.entity.TeacherSchedule
import com.elorrieta.alumnoclient.socketIO.config.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageSchedule
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.socketsio.sockets.config.Events
import org.json.JSONObject


/**
 * The client
 */
class HomeTeacherSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = AESUtil.loadKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_TEACHER_SCHEDULE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val gridLayout = activity.findViewById<GridLayout>(R.id.gridLayout)

                activity.runOnUiThread {
                    loadScheduleSkeleton(gridLayout)
                }

                if (mi.code == 200) {/*
                    Lo que llega: {"code":200,"message":"{\"schedules\":[{\"event\":\"Reunión\",\"day\":1,\"hour
                    */
                    val schedulesJson = JSONObject(mi.message as String)
                    val schedulesArray = schedulesJson.getJSONArray("schedules")
                    val schedules = mutableListOf<TeacherSchedule>()

                    for (i in 0 until schedulesArray.length()) {
                        val schedule = JSONUtil.fromJson<TeacherSchedule>(
                            schedulesArray.getJSONObject(i).toString()
                        )
                        schedules.add(schedule)
                    }

                    // Se crea listado, teniendo en cuenta day,hour como key
                    // Así, se recogen los eventos cuyo campo key es igual, para incluirlos en el mismo punto
                    val eventGrid = mutableMapOf<Pair<Int, Int>, MutableList<TeacherSchedule>>()
                    for (schedule in schedules) {
                        val key = Pair(schedule.day!!, schedule.hour!!)
                        if (!eventGrid.containsKey(key)) {
                            eventGrid[key] = mutableListOf()
                        }
                        eventGrid[key]?.add(schedule)
                    }

                    activity.runOnUiThread {
                        eventGrid.forEach { (key, eventList) ->
                            val (day, hour) = key
                            // Contenedor para apilar eventos el mismo día y hora
                            val container = LinearLayout(activity)
                            container.orientation = LinearLayout.VERTICAL
                            container.gravity = Gravity.CENTER

                            for (event in eventList) {
                                val textView = TextView(activity)
                                textView.text = event.event
                                textView.gravity = Gravity.CENTER
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        activity,
                                        R.color.white
                                    )
                                )
                                textView.setBackgroundColor(getEventColor(event))
                                container.addView(textView)
                            }

                            val params = GridLayout.LayoutParams()
                            params.rowSpec = GridLayout.spec(hour, 1f)
                            params.columnSpec = GridLayout.spec(day, 1f)
                            container.layoutParams = params
                            gridLayout.addView(container)
                        }

                        Toast.makeText(
                            activity,
                            "Horario cargado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    var error = ""
                    when (mi.code) {
                        400 -> error = "Semana no lectiva"
                        404 -> error = "No hay horario cargado para esa semana"
                        500 -> error = "No se ha podido cargar el horario"
                    }
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    // Custom events
    fun doGetSchedules(week: Int) {
        val message = MessageSchedule(LoggedUser.user?.id, week)
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_TEACHER_SCHEDULE.value, encryptedMsg)

        Log.d(tag, "Attempt of get schedules - $message")
    }

    private fun getEventColor(schedule: TeacherSchedule): Int {
        return when (schedule.type) {
            "own_meeting" -> {
                when (schedule.status) {
                    "aceptada" -> ContextCompat.getColor(activity, R.color.green)
                    "rechazada" -> ContextCompat.getColor(activity, R.color.pink)
                    else -> ContextCompat.getColor(activity, R.color.pantone_medium)
                }
            }

            "invited_meeting" -> {
                when (schedule.status) {
                    "aceptada" -> ContextCompat.getColor(activity, R.color.green)
                    "rechazada" -> ContextCompat.getColor(activity, R.color.pink)
                    else -> ContextCompat.getColor(activity, R.color.pantone_medium)
                }
            }

            else -> ContextCompat.getColor(activity, R.color.purple)
        }
    }

    private fun loadScheduleSkeleton(gridLayout: GridLayout) {
        activity.runOnUiThread {
            // FALTA PASAR ESTO A STRINGS
            val dias = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES")
            val horas = listOf("15:00", "16:00", "17:00", "18:00", "19:00", "20:00")

            // vaciar primero
            gridLayout.removeAllViews()

            // Añadir una columna vacía, es para dar color al bg
            val txt = TextView(activity)
            txt.setBackgroundColor(ContextCompat.getColor(activity, R.color.pantone_dark))
            txt.setTextColor(ContextCompat.getColor(activity, R.color.white))
            val param = GridLayout.LayoutParams()
            param.rowSpec = GridLayout.spec(0, 0.5f)
            param.columnSpec = GridLayout.spec(0, 1f)
            txt.layoutParams = param
            gridLayout.addView(txt)

            // Añadir los días en la primera fila
            for (i in dias.indices) {
                val textView = TextView(activity)
                textView.text = dias[i]
                textView.gravity = Gravity.CENTER
                textView.setTypeface(null, Typeface.BOLD)

                textView.setBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.pantone_dark
                    )
                )
                textView.setTextColor(ContextCompat.getColor(activity, R.color.white))

                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(0, 0.5f)
                params.columnSpec = GridLayout.spec(i + 1, 1f)
                textView.layoutParams = params
                gridLayout.addView(textView)
            }

            // Añadir las horas en la primera columna
            for (i in horas.indices) {
                val textView = TextView(activity)
                textView.text = horas[i]
                textView.gravity = Gravity.CENTER
                textView.setTypeface(null, Typeface.BOLD)

                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(i + 1, 1f)
                params.columnSpec = GridLayout.spec(0, 1f)
                textView.layoutParams = params
                gridLayout.addView(textView)
            }
        }
    }
}