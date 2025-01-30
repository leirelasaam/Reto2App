package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageChangePassword
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfileSocket(private val activity: Activity) {
    private val tag = "socket.io"
    private val key = AESUtil.loadKey(activity)
    private val socket: Socket = SocketConnectionManager.getSocket()

    init {
        // Listener para la respuesta del cambio de contraseña
        socket.on(Events.ON_UPDATE_PASS_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val response = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                Log.d(tag, "Respuesta recibida: $response")

                activity.runOnUiThread {
                    when (response.code) {
                        200 -> {
                            Toast.makeText(activity, activity.getString(R.string.password_change_200), Toast.LENGTH_SHORT).show()
                        }
                        400 -> {
                            Toast.makeText(activity, activity.getString(R.string.password_change_400), Toast.LENGTH_LONG).show()
                        }
                        401 -> {
                            Toast.makeText(activity, activity.getString(R.string.password_change_401), Toast.LENGTH_LONG).show()
                        }
                        404 -> {
                            Toast.makeText(activity, activity.getString(R.string.password_change_404), Toast.LENGTH_LONG).show()
                        }
                        409 -> {
                            Toast.makeText(activity, activity.getString(R.string.password_change_409), Toast.LENGTH_LONG).show()
                        }
                        500 -> {
                            Toast.makeText(activity, activity.getString(R.string.server_500), Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(activity, "Error desconocido: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun doChangePassword(message: MessageChangePassword) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val encryptedMsg = AESUtil.encryptObject(message, key)
                socket.emit(Events.ON_UPDATE_PASS.value, encryptedMsg)
                Log.d(tag, "Intento de cambio de contraseña - $message")
            } catch (e: Exception) {
                Log.e(tag, "Error al enviar el cambio de contraseña: ${e.message}")
            }
        }
    }
}
