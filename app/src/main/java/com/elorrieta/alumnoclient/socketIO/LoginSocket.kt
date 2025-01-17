package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.elorrieta.alumnoclient.MainActivity
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.socketsio.sockets.config.Events
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


/**
 * The client
 */
class LoginSocket(private val activity: Activity) {

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

        socket.on(Events.ON_LOGIN_ANSWER.value) { args ->
            val response = args[0] as JSONObject
            Log.d(tag, "Response: $response")

            val code = response.getInt("code")
            val message = response.getString("message") as String
            if (code == 200) {
                val gson = Gson()
                val jsonObject = gson.fromJson(message, JsonObject::class.java)
                val email = jsonObject["email"].asString
                val password = jsonObject["password"].asString

                val user = User(email, password)
                Log.d(tag, "Answer to Login: $user")

                activity.runOnUiThread {
                    Toast.makeText(activity, "Login correcto", Toast.LENGTH_SHORT).show()
                    Thread.sleep(2000)
                    val intent = Intent(activity, MainActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            } else {
                //activity.findViewById<TextView>(R.id.textView).append("\nError: $code")
                Log.d(tag, "Error: $code")
                activity.runOnUiThread {
                    Toast.makeText(activity, "Login incorrecto - Error $code", Toast.LENGTH_SHORT).show()
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
        val user = User(email, password)

        val userJson = Gson().toJson(user)
        val message = MessageInput(userJson)

        socket.emit(Events.ON_LOGIN.value, Gson().toJson(message))

        Log.d(tag, "Attempt of login - $message")
    }
}