package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.HomeStudentActivity
import com.elorrieta.alumnoclient.HomeTeacherActivity
import com.elorrieta.alumnoclient.LoginActivity
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.dto.UserDTO
import com.elorrieta.alumnoclient.room.model.UserRoom
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.socketsio.sockets.config.Events
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

    private var key = AESUtil.loadKey(activity)

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
            // Usar el wrapper, que es solo un try/catch
            Util.safeExecute(tag, activity) {
                val response = args[0] as JSONObject
                val mi = JSONUtil.fromJson<MessageInput>(response.toString())

                if (mi.code == 200 || mi.code == 403) {
                    var newActivity: Class<out Activity> = LoginActivity::class.java
                    val decryptedMessage = AESUtil.decrypt(mi.message, key)
                    // Extraer el usuario
                    val user = JSONUtil.fromJson<User>(decryptedMessage)
                    Log.d(tag, "User: $user")

                    if (mi.code == 200) {
                        LoggedUser.user = user

                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.login_200),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        newActivity =
                            if (user.role?.role == "profesor") HomeTeacherActivity::class.java else HomeStudentActivity::class.java

                        // El login es correcto, por lo que se guarda en la db ROOM
                        val db = UsersRoomDatabase(activity)
                        GlobalScope.launch(Dispatchers.IO) {
                            val email = user.email
                            val pin = user.pin
                            val password = enteredPassword

                            if (email != null && pin != null && password != null) {
                                val userRoom = UserRoom(
                                    email = email,
                                    pin = pin,
                                    password = password,
                                    lastLogged = false
                                )

                                db.usersDao().insert(userRoom)
                                Log.d(tag, "Se ha insertado en ROOM: $user")
                                db.usersDao().resetLastLogged()
                                db.usersDao().updateLastLogged(userRoom.email)
                            }
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.login_403),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        newActivity = RegistrationActivity::class.java
                    }
                    Thread.sleep(2000)

                    val intent = Intent(activity, newActivity)
                    activity.startActivity(intent)
                    activity.finish()
                } else {
                    Log.d(tag, "Error: $mi.code - $mi.message")

                    // Según el código de error, mostrar un mensaje
                    var error = ""
                    when (mi.code) {
                        404 -> error = activity.getString(R.string.login_404)
                        400 -> error = activity.getString(R.string.login_400)
                        401 -> error = activity.getString(R.string.login_401)
                        500 -> error = activity.getString(R.string.server_500)
                    }

                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        socket.on(Events.ON_RESET_PASS_EMAIL_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val response = args[0] as JSONObject
                val mi = JSONUtil.fromJson<MessageInput>(response.toString())

                if (mi.code == 200) {
                    Log.d(tag, "Correo enviado.")
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.reset_pass_200),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.d(tag, "Error al enviar el correo: $mi")

                    var error = ""
                    when (mi.code) {
                        404 -> error = activity.getString(R.string.login_404)
                        500 -> error = activity.getString(R.string.server_500)
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
        val message = JSONUtil.toJson(loginMsg)
        enteredPassword = loginMsg.password
        socket.emit(Events.ON_LOGIN.value, message)

        Log.d(tag, "Attempt of login - $message")
    }

    fun doSendPassEmail(msg: MessageOutput) {
        val message = JSONUtil.toJson(msg)
        socket.emit(Events.ON_RESET_PASS_EMAIL.value, message)

        Log.d(tag, "Attempt of reset password - $message")
    }
}