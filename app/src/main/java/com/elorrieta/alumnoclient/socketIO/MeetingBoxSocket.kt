package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.adapters.MeetingBoxAdapter
import com.elorrieta.alumnoclient.entity.Document
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events
import org.json.JSONObject

class MeetingBoxSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_ALL_MEETINGS_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                if (mi.code == 200) {
                    val documentsJson = JSONObject(mi.message as String)
                    val documentsArray = documentsJson.getJSONArray("courses")
                    val documents = mutableListOf<Document>()

                    for (i in 0 until documentsArray.length()) {
                        val document = JSONUtil.fromJson<Document>(
                            documentsArray.getJSONObject(i).toString()
                        )
                        documents.add(document)
                    }

                    val adapter = MeetingBoxAdapter(activity, documents)
                    //activity.findViewById<RecyclerView>(R.id.listaHistorico).adapter = adapter
                }


            }
        }
    }

    // Custom events
    fun doGetAllMeetings() {
        val message = MessageOutput(LoggedUser.user?.id.toString())
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_ALL_MEETINGS.value, encryptedMsg)

        Log.d(tag, "Attempt of get meetings - $message")
    }
}