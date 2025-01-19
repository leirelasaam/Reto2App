package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.HomeStudentActivity
import com.elorrieta.alumnoclient.HomeTeacherActivity
import com.elorrieta.alumnoclient.LoginActivity
import com.elorrieta.alumnoclient.RegistrationActivity
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
class LoginSocket(private val activity: Activity) {

    // Server IP:Port
    private val ipPort = "http://172.22.240.1:3000"
    private val socket: Socket = IO.socket(ipPort)
    private var enteredPassword: String? = null

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
                val jsonMessage = gson.fromJson(mi.message, JsonObject::class.java)
                val userDTO = gson.fromJson(jsonMessage, UserDTO::class.java)

                var newActivity: Class<out Activity> = LoginActivity::class.java

                if (mi.code == 200) {
                    if (userDTO.role == "profesor" || userDTO.role == "estudiante") {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Login correcto", Toast.LENGTH_SHORT).show()
                        }
                        newActivity = if(userDTO.role == "profesor") HomeTeacherActivity::class.java else HomeStudentActivity::class.java

                        // El login es correcto, por lo que se guarda en la db ROOM
                        val db = UsersRoomDatabase(activity)
                        GlobalScope.launch(Dispatchers.IO) {
                            val user = enteredPassword?.let { User(email = userDTO.email, pin = userDTO.pin, password = it) }
                            if (user != null) {
                                db.usersDao().insert(user)
                                Log.d(tag, "Se ha insertado en ROOM: $user")
                            }
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "No puedes acceder", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Debes registrarte", Toast.LENGTH_SHORT).show()
                    }
                    newActivity = RegistrationActivity::class.java
                }

                Log.d(tag, "Usuario logueado: $userDTO")
                Thread.sleep(2000)

                // Crear el Intent para la nueva actividad
                if (newActivity != LoginActivity::class.java) {
                    val intent = Intent(activity, newActivity)
                    activity.startActivity(intent)
                    activity.finish()
                }
            } else {
                Log.d(tag, "Error: $mi.code")
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Login incorrecto - Error $mi.code $mi.message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        socket.on(Events.ON_RESET_PASS_EMAIL_ANSWER.value) { args ->
            val response = args[0] as JSONObject
            Log.d(tag, "Response: $response")

            val jsonString = response.toString()
            val mi = Gson().fromJson(jsonString, MessageInput::class.java)

            if (mi.code==200){
                Log.d(tag, "Correo enviado.")
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Se ha enviado la nueva clave",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d(tag, "Error al enviar el correo: $mi")
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Error al enviar el correo",
                        Toast.LENGTH_SHORT
                    ).show()
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
    fun doLogin(loginMsg: MessageLogin) {
        val message = MessageOutput(Gson().toJson(loginMsg))
        enteredPassword = loginMsg.password
        socket.emit(Events.ON_LOGIN.value, Gson().toJson(message))

        Log.d(tag, "Attempt of login - $message")
    }

    fun doSendPassEmail(msg: MessageOutput) {
        socket.emit(Events.ON_RESET_PASS_EMAIL.value, Gson().toJson(msg))

        Log.d(tag, "Attempt of reset password - $msg")
    }
}