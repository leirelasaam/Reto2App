package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.adapters.MeetingBoxAdapter
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageMeetingStatus
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
                val recycler = activity.findViewById<RecyclerView>(R.id.recyclerMeetings)

                if (mi.code == 200) {
                    val meetingsJson = JSONObject(mi.message)
                    val meetingsArray = meetingsJson.getJSONArray("meetings")
                    val meetings = mutableListOf<Meeting>()

                    for (i in 0 until meetingsArray.length()) {
                        val meeting = JSONUtil.fromJson<Meeting>(
                            meetingsArray.getJSONObject(i).toString()
                        )
                        meetings.add(meeting)
                    }

                    activity.runOnUiThread {
                        val adapter = MeetingBoxAdapter(activity, meetings, this)
                        recycler.layoutManager = LinearLayoutManager(activity)
                        recycler.adapter = adapter
                    }
                } else {
                    recycler.visibility = View.GONE
                    activity.findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
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

    fun doUpdateParticipantStatus(message: MessageMeetingStatus) {
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_PARTICIPANT_STATUS_UPDATE.value, encryptedMsg)

        Log.d(tag, "Attempt of update participant status for a meeting - $message")
    }

    fun doUpdateMeetingStatus(message: MessageMeetingStatus) {
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_MEETING_STATUS_UPDATE.value, encryptedMsg)

        Log.d(tag, "Attempt of update meeting status for a meeting - $message")
    }
}