package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.StudentScheduleActivity
import com.elorrieta.alumnoclient.TeacherScheduleActivity
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.LoggedUser.user
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
//import com.elorrieta.socketsio.sockets.config.Events
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class RegisterSocket(private val activity: Activity) {
    private var key = AESUtil.loadKey(activity)
    // For log purposes
    private var tag = "socket.io"
    val socket = SocketConnectionManager.getSocket()

    init {

        // Evento del servidor - Android recibe el usuario logueado
        socket.on(Events.ON_REGISTER_UPDATE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val newActivity: Class<out Activity>


                    LoggedUser.user = user
                    Log.d(tag, "User: $user")

                    if (mi.code == 200 || mi.code == 500) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.login_200),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        newActivity =
                            if (user?.role?.role == "profesor") TeacherScheduleActivity::class.java else StudentScheduleActivity::class.java


                } else {
                    Log.d("socket", "Error: ${mi.code}")
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "${activity.getString(R.string.register_toast_intent_code)} ${mi.code} ${mi.message}",
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

    //Manda un evento con todos los datos comprobados por el usuario
    fun doRegisterUpdate(updateMsg: MessageRegisterUpdate) {
        Log.d(tag, "Attempt of update sign up.${updateMsg.name}")
        Log.d(tag, "Attempt of update sign up.${updateMsg.password}")

        val encryptedMsg = AESUtil.encryptObject(updateMsg, key)

        // Obtener el tamaño del mensaje en KB
        val messageSizeKb = encryptedMsg.toByteArray().size / 1024.0
        Log.d(tag, "Encrypted message size: %.2f KB".format(messageSizeKb))

        socket.emit(Events.ON_REGISTER_UPDATE.value, encryptedMsg)
        Log.d(tag, "Attempt of update sign up.")
    }
}
