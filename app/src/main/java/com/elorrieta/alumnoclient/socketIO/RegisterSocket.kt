package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.RegistrationActivity
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.socketIO.config.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageLogin
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.socketIO.model.MessageRegister
import com.elorrieta.alumnoclient.socketIO.model.MessageRegisterUpdate
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.socketsio.sockets.config.Events
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
        socket.on(Events.ON_REGISTER_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                if (mi.code == 200) {
                    val gson = Gson()
                    val jsonMessage = gson.fromJson(mi.message, JsonObject::class.java)

                    // Deserializa el jsonMessage en un objeto User
                    val user = gson.fromJson(jsonMessage,
                        com.elorrieta.alumnoclient.entity.User::class.java
                    )
                    val name = user.name
                    val email = user.email
                    val lastname = user.lastname
                    val pin = user.pin
                    val address = user.address
                    val phone1 = user.phone1
                    val phone2 = user.phone2
                    val dual = user.intensive
                    val cycle = "DAM"
                    val course = "1"

                    activity.findViewById<EditText>(R.id.editTextNombre).setText(name)
                    activity.findViewById<EditText>(R.id.editTextEmail).setText(email)
                    activity.findViewById<EditText>(R.id.editTextApellidos).setText(lastname)
                    activity.findViewById<EditText>(R.id.editTextDNI).setText(pin)
                    activity.findViewById<EditText>(R.id.editTextDireccion).setText(address)
                    activity.findViewById<EditText>(R.id.editTextTelefono1).setText(phone1)
                    activity.findViewById<EditText>(R.id.editTextTelefono2).setText(phone2)
                    activity.findViewById<com.google.android.material.chip.Chip>(R.id.chipDualIntesiva).append(
                        dual.toString()
                    )
                    activity.findViewById<EditText>(R.id.editTextCicloFormativo).setText(cycle)
                    activity.findViewById<EditText>(R.id.editTextCurso).setText(course)

                    // Pasa el usuario a RegisterActivity
                    //val intent = Intent(activity, RegistrationActivity::class.java)
                    //intent.putExtra("user", user) // Pasa el objeto User como extra
                    //activity.startActivity(intent)


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

    //Manda el correo para recibir los datos del usuario
    fun doSignUp(registerMsg: MessageRegister) {
        val email = registerMsg.login
        val encryptedMsg = AESUtil.encryptObject(registerMsg, key)
        socket.emit(Events.ON_REGISTER_INFO.value, encryptedMsg)
        Log.d(tag, "Attempt of sign up desde el RegisterSocket Hola Lucian- $email")
    }

    //val message = MessageOutput(Gson().toJson(email))
    //val encryptedMsg = AESUtil.encryptObject(registerMsg)
    //falta encriptar el mensaje


    //Manda
    fun doRegisterUpdate(registerMsg: MessageRegisterUpdate) {

    }
}
