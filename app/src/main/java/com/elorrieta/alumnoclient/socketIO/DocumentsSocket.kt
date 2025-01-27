package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events

class DocumentsSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = AESUtil.loadKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_TEACHER_SCHEDULE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)
            }
        }
    }

    // Custom events
    fun doGetDocumentList(week: Int) {
        val message = MessageOutput(LoggedUser.user?.id.toString())
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_STUDENT_DOCUMENTS.value, encryptedMsg)

        Log.d(tag, "Attempt of get documents - $message")
    }
}