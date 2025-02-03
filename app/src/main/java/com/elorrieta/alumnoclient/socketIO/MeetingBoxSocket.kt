package com.elorrieta.alumnoclient.socketIO

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.elorrieta.alumnoclient.MeetingBoxActivity
import com.elorrieta.alumnoclient.R
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

@SuppressLint("NotifyDataSetChanged")
class MeetingBoxSocket(private val activity: MeetingBoxActivity) {
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
                    val meetingsJson = JSONObject(mi.message)
                    val meetingsArray = meetingsJson.getJSONArray("meetings")
                    val meetings = mutableListOf<Meeting>()

                    for (i in 0 until meetingsArray.length()) {
                        val meeting = JSONUtil.fromJson<Meeting>(
                            meetingsArray.getJSONObject(i).toString()
                        )
                        meetings.add(meeting)
                    }

                    activity.loadAdapter(meetings)
                } else {
                    activity.showEmpty()
                }
            }
        }

        socket.on(Events.ON_MEETING_STATUS_UPDATE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                var msg = ""
                when (mi.code) {
                    200 -> msg = activity.getString(R.string.meeting_box_status_200)
                    400 -> msg = activity.getString(R.string.meeting_box_status_400)
                    404 -> msg = activity.getString(R.string.meeting_box_status_404)
                    500 -> msg = activity.getString(R.string.meeting_box_status_500)
                }

                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        msg,
                        Toast.LENGTH_LONG
                    ).show()
                }

                doGetAllMeetings()
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