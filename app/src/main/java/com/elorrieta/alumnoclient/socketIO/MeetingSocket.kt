package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.entity.User
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.Date

/**
 * The client
 */
class MeetingSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        // Listener para la respuesta del cambio de contraseña
        socket.on(Events.ON_CREATE_MEETING_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val response = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                Log.d(tag, "Respuesta recibida: $response")

                activity.runOnUiThread {
                    when (response.code) {
                        200 -> {
                            Toast.makeText(activity, activity.getString(R.string.meeting_status_200), Toast.LENGTH_SHORT).show()
                        }
                        204 -> {
                            Toast.makeText(activity, activity.getString(R.string.meeting_status_204), Toast.LENGTH_LONG).show()
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

    /*OBTENER USUARIOS POR EL ROL DE CADA UNO*/

    fun getUsersByRole(roleId: Int, callback: (List<User>?) -> Unit) {
        // Crear JSON con el ID del rol
        val jsonObject = JSONObject()
        jsonObject.put("roleId", roleId)

        // Encriptar el mensaje
        val encryptedMessage = AESUtil.encrypt(jsonObject.toString(), key)

        // Enviar evento al servidor
        socket.emit(Events.ON_GET_ALL_USERS.value, encryptedMessage)

        // Escuchar la respuesta del servidor
        socket.on(Events.ON_GET_ALL_USERS_ANSWER.value) { args ->
            if (args.isNotEmpty()) {
                val encryptedResponse = args[0] as String
                try {
                    // Desencriptar la respuesta
                    val decryptedResponse = AESUtil.decrypt(encryptedResponse, key)
                    Log.d("SocketClient", "Decrypted response: $decryptedResponse")

                    // Convertir la respuesta JSON en una lista de usuarios
                    val users = parseUsers(decryptedResponse)
                    Log.d("Users", "Users response: $users")
                    callback(users)
                } catch (e: Exception) {
                    Log.e("SocketClient", "Error decrypting response: ${e.message}")
                    callback(null)
                }
            }
        }
    }

    /* GUARDAR LA REUNIÓN */

    fun saveMeeting(meeting: Meeting) {
        val jsonObject = JSONObject().apply {
            put("id", meeting.id ?: JSONObject.NULL) // Si el id es null, usa JSONObject.NULL
            put("user", meeting.user?.let { user ->
                JSONObject().apply {
                    put("id", user.id)
                    put("name", user.name)
                    put("email", user.email)
                    put("lastname", user.lastname)
                    put("pin", user.pin)
                    put("address", user.address)
                    put("phone1", user.phone1)
                    put("phone2", user.phone2 ?: JSONObject.NULL)
                    put("photo", user.photo ?: JSONObject.NULL)
                    put("intensive", user.intensive)
                    put("registered", user.registered)
                    put("createdAt", user.createdAt)
                    put("updatedAt", user.updatedAt)
                    put("deletedAt", user.deletedAt ?: JSONObject.NULL)
                }?: JSONObject.NULL})
            put("day", meeting.day)
            put("time", meeting.time)
            put("week", meeting.week)
            put("status", meeting.status)
            put("title", meeting.title)
            put("room", meeting.room)
            put("subject", meeting.subject)
            put("created_at", meeting.createdAt)
            put("updated_at", meeting.updatedAt)
            put("participants", JSONArray(meeting.participants.map { participant ->
                JSONObject().apply {
                    put("idUser", participant.user.id)
                }
            }))
        }

        val encryptedMessage = AESUtil.encrypt(jsonObject.toString(), key)

        socket.emit(Events.ON_CREATE_MEETING.value, encryptedMessage)

        Log.d("MeetingActivity", "Attempt to create meeting - $meeting")
    }

    private fun parseUsers(json: String): List<User> {
        return try {
            val gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date>,
                    JsonSerializer<Date> {
                    override fun deserialize(
                        json: JsonElement,
                        typeOfT: Type,
                        context: JsonDeserializationContext
                    ): Date {
                        return Date(json.asLong) // Convierte el timestamp en milisegundos a un objeto Date
                    }

                    override fun serialize(
                        src: Date,
                        typeOfSrc: Type,
                        context: JsonSerializationContext
                    ): JsonElement {
                        return JsonPrimitive(src.time) // Convierte un objeto Date a su representación en milisegundos
                    }
                })
                .create()

            // Convierte el JSON principal al objeto MessageInput
            val mi = gson.fromJson(json, MessageInput::class.java)

            // Convierte el campo `message` directamente a un JsonArray si ya lo es
            val usersArray = gson.fromJson(mi.message, JsonArray::class.java)

            // Convierte el JsonArray en una lista de objetos User
            val userList: List<User> = gson.fromJson(usersArray, Array<User>::class.java).toList()

            userList // Devuelve la lista de usuarios
        } catch (e: Exception) {
            Log.e("ParseUsers", "Error al parsear usuarios: ${e.message}")
            emptyList() // Devuelve una lista vacía en caso de error
        }
    }
}

