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
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.dto.UserDTO
import com.elorrieta.alumnoclient.room.model.UserRoom
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.socketsio.sockets.config.Events
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
    private val ipPort = "http://10.5.104.31:3000"
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
            Log.d(tag, "JSON String: $jsonString")

            val objectMapper = ObjectMapper().apply {
                registerModule(KotlinModule())
                registerModule(Jdk8Module())
            }

            // Parsear JSON dentro de message
            val rootNode: JsonNode = objectMapper.readTree(jsonString)
            val messageNode = rootNode.get("message").asText()
            Log.d(tag, "Message extracted: $messageNode")
            val user = objectMapper.readValue(messageNode, User::class.java)
            Log.d(tag, "User: $user")

            val mi = Gson().fromJson(jsonString, MessageInput::class.java)

            if (mi.code == 200 || mi.code == 403) {
                val jsonMessage = Gson().fromJson(mi.message, JsonObject::class.java)

                var newActivity: Class<out Activity> = LoginActivity::class.java

                if (mi.code == 200) {
                    if (user.role?.role == "profesor" || user.role?.role == "estudiante") {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Login correcto", Toast.LENGTH_SHORT).show()
                        }
                        newActivity =
                            if (user.role?.role == "profesor") HomeTeacherActivity::class.java else HomeStudentActivity::class.java

                        // El login es correcto, por lo que se guarda en la db ROOM
                        val db = UsersRoomDatabase(activity)
                        GlobalScope.launch(Dispatchers.IO) {
                            val userRoom = enteredPassword?.let {
                                user.email?.let { it1 ->
                                    user.pin?.let { it2 ->
                                        UserRoom(
                                            email = it1,
                                            pin = it2,
                                            password = it,
                                            lastLogged = false
                                        )
                                    }
                                }
                            }
                            if (user != null) {
                                if (userRoom != null) {
                                    db.usersDao().insert(userRoom)
                                }
                                Log.d(tag, "Se ha insertado en ROOM: $user")
                                db.usersDao().resetLastLogged()
                                if (userRoom != null) {
                                    db.usersDao().updateLastLogged(userRoom.email)
                                }
                                LoggedUser.user = user
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

                Log.d(tag, "Usuario logueado: $user")
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

            if (mi.code == 200) {
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