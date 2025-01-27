package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.elorrieta.socketsio.sockets.config.Events
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class RegisterSocket(private val activity: Activity) {

    // Server IP:Port
    private val ipPort = "http://172.22.240.1:3000"
    val socket: Socket = IO.socket(ipPort)
    private var enteredPassword: String? = null

    // For log purposes
    private var tag = "socket.io"

    init {
        // Establecer eventos de conexión y desconexión
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("socket", "Connected to server.")
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("socket", "Disconnected from server.")
        }

        // Evento del servidor - Android recibe el usuario logueado
        socket.on(Events.ON_REGISTER_ANSWER.value) { args ->
            val response = args[0] as JSONObject
            Log.d("socket", "Response: $response")

            val jsonString = response.toString()
            val mi = Gson().fromJson(jsonString, MessageInput::class.java)

            if (mi.code == 200) {
                val gson = Gson()
                val jsonMessage = gson.fromJson(mi.message, JsonObject::class.java)

                // Deserializa el jsonMessage en un objeto User
                val user = gson.fromJson(jsonMessage, com.elorrieta.alumnoclient.entity.User::class.java)

                // Pasa el usuario a RegisterActivity
                val intent = Intent(activity, RegistrationActivity::class.java)
                intent.putExtra("user", user) // Pasa el objeto User como extra
                activity.startActivity(intent)

            } else {
                Log.d("socket", "Error: $mi.code")
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Error en el registro - Error ${mi.code} ${mi.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

/* Esto irá en el ON_REGISTER_ANSWER_UPDATE cuando recibamos que se ha actualizado correctamente el usuario en el servidor
    Log.d(tag, "Usuario dado de alta: $jsonMessage")
    Thread.sleep(2000)
  */

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

    //Manda el correo para recibir los datos del usuario
    fun doSignUp(email: String) {
        val emailPayload = mapOf("email" to email)
        val message = MessageOutput(Gson().toJson(emailPayload))
        socket.emit(Events.ON_REGISTER.value, Gson().toJson(message))
        Log.d(tag, "Attempt of sign up - $message")
    }

    //Manda
    fun doRegisterUpdate(registerMsg: MessageRegisterUpdate) {

    }
}
