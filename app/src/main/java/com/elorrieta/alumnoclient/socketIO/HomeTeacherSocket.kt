package com.elorrieta.alumnoclient.socketIO

import android.util.Log
import android.widget.Toast
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.TeacherScheduleActivity
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.entity.TeacherSchedule
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageSchedule
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import com.elorrieta.alumnoclient.socketIO.config.Events
import org.json.JSONObject

/**
 * The client
 */
class HomeTeacherSocket(private val activity: TeacherScheduleActivity) {
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_TEACHER_SCHEDULE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val gridLayout = activity.findViewById<GridLayout>(R.id.gridLayout)

                activity.runOnUiThread {
                    activity.loadScheduleSkeleton(gridLayout)
                }

                if (mi.code == 200) {/*
                    Lo que llega: {"code":200,"message":"{\"schedules\":[{\"event\":\"Reunión\",\"day\":1,\"hour
                    */
                    val schedulesJson = JSONObject(mi.message)
                    val schedulesArray = schedulesJson.getJSONArray("schedules")
                    val schedules = mutableListOf<TeacherSchedule>()

                    for (i in 0 until schedulesArray.length()) {
                        val schedule = JSONUtil.fromJson<TeacherSchedule>(
                            schedulesArray.getJSONObject(i).toString()
                        )
                        schedules.add(schedule)
                    }

                    activity.runOnUiThread {
                        activity.loadSchedule(schedules, gridLayout)

                        Toast.makeText(
                            activity,
                            activity.getString(R.string.schedules_200),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    var error = ""
                    when (mi.code) {
                        400 -> error = activity.getString(R.string.schedules_400)
                        404 -> error = activity.getString(R.string.schedules_404)
                        500 -> error = activity.getString(R.string.schedules_500)
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
    fun doGetSchedules(week: Int) {
        val message = MessageSchedule(LoggedUser.user?.id, week)
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_TEACHER_SCHEDULE.value, encryptedMsg)

        Log.d(tag, "Attempt of get schedules - $message")
    }
}

