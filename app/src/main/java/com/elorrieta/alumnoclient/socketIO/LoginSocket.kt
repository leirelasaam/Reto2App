package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.entity.UserDTO
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
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
class LoginSocket(private val activity: Activity) {

    // Server IP:Port
    private val ipPort = "http://172.22.240.1:3000"
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

        socket.on(Events.ON_LOGIN_ANSWER.value) { args ->
            val response = args[0] as JSONObject
            Log.d(tag, "Response: $response")

            val jsonString = response.toString()
            val mi = Gson().fromJson(jsonString, MessageInput::class.java)

            if (mi.code == 200 || mi.code == 403) {
                val gson = Gson()
                val jsonObject = gson.fromJson(mi.message, JsonObject::class.java)
                val messageInput = gson.fromJson(jsonObject, MessageInput::class.java)
                val userDTO = gson.fromJson(messageInput.message, UserDTO::class.java)

                Log.d(tag, "Usuario logueado: $userDTO")

                if (mi.code == 200){
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Login correcto", Toast.LENGTH_SHORT).show()
                        Thread.sleep(2000)
                    }
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Debes registrarte", Toast.LENGTH_SHORT).show()
                        Thread.sleep(2000)
                    }
                }
            } else {
                Log.d(tag, "Error: $mi.code")
                activity.runOnUiThread {
                    Toast.makeText(activity, "Login incorrecto - Error $mi.code $mi.message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Default events
    fun connect() {
        socket.connect()
        Log.d(tag, "Connecting to server...")
    }
    fun disconnect() {
        socket.disconnect()
        Log.d(tag, "Disconnecting from server...")
    }

    // Custom events
    fun doLogin(email: String, password: String) {
        val login = MessageLogin(email, password)
        val message = MessageOutput(Gson().toJson(login))

        socket.emit(Events.ON_LOGIN.value, Gson().toJson(message))

        Log.d(tag, "Attempt of login - $message")
    }
}