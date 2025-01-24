package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.dto.ScheduleDTO
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.entity.TeacherSchedule
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.socketIO.model.MessageSchedule
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.socketsio.sockets.config.Events
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


/**
 * The client
 */
class HomeTeacherSocket(private val activity: Activity) {

    // Server IP:Port
    private val ipPort = "http://10.5.104.31:3000"
    private val socket: Socket = IO.socket(ipPort)

    // For log purposes
    private var tag = "socket.io"

    init {
        // Event called when the socket connects
        socket.on(Socket.EVENT_CONNECT) {
            Log.d(tag, "Connected...")
        }

        // Event called when the socket disconnects
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d(tag, "Disconnected...")
        }

        socket.on(Events.ON_TEACHER_SCHEDULE_ANSWER.value) { args ->
            val response = args[0] as JSONObject
            val mi = JSONUtil.fromJson<MessageInput>(response.toString())

            if (mi.code == 200) {
                /* Lo que llega: {"code":200,"message":"{\"schedules\":[{\"event\":\"Reunión\",\"day\":1,\"hour\*/
                /*
                mi.message.schedules.forEach { schedule ->
                    println("Evento: ${schedule.event}, Día: ${schedule.day}, Hora: ${schedule.hour}")
                }
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

                // Poner en la primera fila los días
                val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
                val horas = listOf("15:00", "16:00", "17:00", "18:00", "19:00", "20:00")

                /*
                val pruebaTxt = activity.findViewById<TextView>(R.id.pruebaTxt)
                pruebaTxt.text = ""
                // Fila
                for (i in 0..6){
                    // Columna
                    for (j in 0..5){

                        if (i == 0 && j > 0){
                            val dia = dias[j-1]
                            pruebaTxt.text = pruebaTxt.text.toString() + "$dia "
                        } else if(i > 0 && j == 0){
                            val hora = horas[i-1]
                            pruebaTxt.text = pruebaTxt.text.toString() + "$hora "
                        }
                        else {
                            for (schedule in schedules){
                                if (schedule.day == j && schedule.hour == i){
                                    val event = schedule.event
                                    pruebaTxt.text = pruebaTxt.text.toString() + "$event "
                                }
                            }
                        }


                        /*
                        * i = 0, j...
                        * j = 0 -> entre an segundo if ->
                        * */
                    }
                    pruebaTxt.text = pruebaTxt.text.toString() + "\n"
                }
                */

                val tableLayout = activity.findViewById<TableLayout>(R.id.tableLayout)

                activity.runOnUiThread {
                    val headerRow = TableRow(activity)
                    for (dia in dias) {
                        headerRow.addView(TextView(activity).apply {
                            text = dia
                            gravity = Gravity.CENTER
                        })
                    }
                    tableLayout.addView(headerRow)

                    for (i in 0..6) {
                        val row = TableRow(activity)

                        // Hora
                        row.addView(TextView(activity).apply {
                            text = horas[i]
                            gravity = Gravity.CENTER
                        })

                        // Eventos
                        for (j in 1..5) { // Solo de lunes a viernes
                            val eventText =
                                schedules.find { it.day == j && it.hour == i }?.event ?: ""
                            row.addView(TextView(activity).apply {
                                text = eventText
                                gravity = Gravity.CENTER
                            })
                        }

                        tableLayout.addView(row)
                    }
                }
            }
        }
    }

    // Default events
    fun connect() {
        if (!socket.connected()) {
            socket.connect()
            Log.d(tag, "Connecting to server...")
        } else {
            Log.d(tag, "Already connected.")
        }
    }

    fun disconnect() {
        if (socket.connected()) {
            socket.disconnect()
            Log.d(tag, "Disconnecting from server...")
        } else {
            Log.d(tag, "Not connected, cannot disconnect.")
        }
    }

    // Custom events
    fun doGetSchedules() {
        val message = JSONUtil.toJson(MessageSchedule(LoggedUser.user?.id, 1))
        socket.emit(Events.ON_TEACHER_SCHEDULE.value, message)

        Log.d(tag, "Attempt of get schedules - $message")
    }

}