package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.dto.ScheduleDTO
import com.elorrieta.alumnoclient.dto.UserDTO
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
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
            Log.d(tag, "Response: $response")

            val jsonString = response.toString()
            val mi = Gson().fromJson(jsonString, MessageInput::class.java)
            val jsonMessage = Gson().fromJson(mi.message, JsonObject::class.java)
            val schedulesJsonArray = jsonMessage.getAsJsonArray("schedules")
            val schedulesList = Gson().fromJson(schedulesJsonArray, Array<ScheduleDTO>::class.java).toList()

            val grid = activity.findViewById<GridLayout>(R.id.gridTeacher)

            // Poner en la primera fila los días
            val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
            for (i in dias.indices) {
                val tx = TextView(activity)
                tx.text = dias[i]
                tx.gravity = Gravity.CENTER
                val param = GridLayout.LayoutParams()
                param.rowSpec = GridLayout.spec(0)
                param.columnSpec = GridLayout.spec(i + 1)
                tx.layoutParams = param
                grid.addView(tx)
            }

            // Poner en la primera columna las horas
            val horas = listOf("15:00", "16:00", "17:00", "18:00", "19:00", "20:00")
            for (i in horas.indices) {
                val tx = TextView(activity)
                tx.text = horas[i]
                tx.gravity = Gravity.CENTER
                val param = GridLayout.LayoutParams()
                param.rowSpec = GridLayout.spec(i + 1)
                param.columnSpec = GridLayout.spec(0)
                tx.layoutParams = param
                grid.addView(tx)
            }
/*
            for ((i, schedule) in schedulesList.withIndex()) {
                val tx = TextView(activity)
                val param = GridLayout.LayoutParams(
                    GridLayout.spec(
                        GridLayout.UNDEFINED, GridLayout.FILL, 1f
                    ),
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                )

                tx.layoutParams = param
                grid.addView(tx)
            }
*/
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
        val message = MessageOutput(LoggedUser.user?.id.toString())
        socket.emit(Events.ON_TEACHER_SCHEDULE.value, Gson().toJson(message))

        Log.d(tag, "Attempt of get schedules - $message")
    }
}