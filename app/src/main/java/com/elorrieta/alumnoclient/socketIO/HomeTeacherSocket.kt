package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.HomeStudentActivity
import com.elorrieta.alumnoclient.HomeTeacherActivity
import com.elorrieta.alumnoclient.LoginActivity
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.entity.UserDTO
import com.elorrieta.alumnoclient.room.model.User
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.socketsio.sockets.config.Events
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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