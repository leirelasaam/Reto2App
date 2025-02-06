package com.elorrieta.alumnoclient.socketIO

import android.util.Log
import android.widget.Toast
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.StudentScheduleActivity
import com.elorrieta.alumnoclient.entity.StudentSchedule
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.PrivateKeyManager
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import org.json.JSONObject

class HomeStudentSocket(private val activity: StudentScheduleActivity) {
    private var tag = "socket.io"
    private var key = PrivateKeyManager.getKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_STUDENT_SCHEDULE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val gridLayout = activity.findViewById<GridLayout>(R.id.gridLayoutStudent)

                activity.runOnUiThread {
                    activity.loadScheduleSkeleton(gridLayout)
                }

                if (mi.code == 200) {
                    // El JSON recibido
                    val schedulesJson = JSONObject(mi.message)
                    val schedulesArray = schedulesJson.getJSONArray("schedules")
                    val schedules = mutableListOf<StudentSchedule>()

                    for (i in 0 until schedulesArray.length()) {
                        val schedule = JSONUtil.fromJson<StudentSchedule>(
                            schedulesArray.getJSONObject(i).toString()
                        )
                        schedules.add(schedule)
                    }


                    // Actualización de la UI en el hilo principal
                    activity.runOnUiThread {
                        activity.loadStudentSchedule(schedules, gridLayout)

                        /*Toast.makeText(
                            activity,
                            activity.getString(R.string.schedules_200),
                            Toast.LENGTH_SHORT
                        ).show()*/

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
    fun doGetSchedules() {
        val message = MessageOutput(LoggedUser.user?.id.toString())
        val encryptedMsg = AESUtil.encryptObject(message, key)
        socket.emit(Events.ON_STUDENT_SCHEDULE.value, encryptedMsg)

        Log.d(tag, "Attempt of get schedules - $message")
    }
}
