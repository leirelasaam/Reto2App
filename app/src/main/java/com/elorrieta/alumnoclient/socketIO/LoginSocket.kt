package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.StudentScheduleActivity
import com.elorrieta.alumnoclient.TeacherScheduleActivity
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.room.model.UserRoom
import com.elorrieta.alumnoclient.room.model.UsersRoomDatabase
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The client
 */
@OptIn(DelicateCoroutinesApi::class)
class LoginSocket(private val activity: Activity) {
    private var enteredPassword: String? = null
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_LOGIN_ANSWER.value) { args ->
            // Usar el wrapper, que es solo un try/catch
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                if (mi.code == 200 || mi.code == 403) {
                    val newActivity: Class<out Activity>
                    // Extraer el usuario
                    val user = JSONUtil.fromJson<User>(mi.message)
                    Log.d(tag, "User: $user")

                    LoggedUser.user = user

                    if (mi.code == 200) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.login_200),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        newActivity =
                            if (user.role?.role == "profesor") TeacherScheduleActivity::class.java else StudentScheduleActivity::class.java

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
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

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

    // Custom events
    fun doLogin(loginMsg: MessageLogin) {
        enteredPassword = loginMsg.password
        val encryptedMsg = AESUtil.encryptObject(loginMsg, key)
        socket.emit(Events.ON_LOGIN.value, encryptedMsg)
        Log.d(tag, "Attempt of login - $loginMsg")
    }

    fun doSendPassEmail(msg: MessageOutput) {
        val encryptedMsg = AESUtil.encryptObject(msg, key)
        socket.emit(Events.ON_RESET_PASS_EMAIL.value, encryptedMsg)

        Log.d(tag, "Attempt of reset password - $msg")
    }
}