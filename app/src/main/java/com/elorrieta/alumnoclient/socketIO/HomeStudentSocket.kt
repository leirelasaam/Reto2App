package com.elorrieta.alumnoclient.socketIO

import android.app.Activity
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.StudentSchedule
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.singletons.SocketConnectionManager
import com.elorrieta.alumnoclient.socketIO.config.Events
import com.elorrieta.alumnoclient.socketIO.model.MessageInput
import com.elorrieta.alumnoclient.socketIO.model.MessageOutput
import com.elorrieta.alumnoclient.utils.AESUtil
import com.elorrieta.alumnoclient.utils.JSONUtil
import com.elorrieta.alumnoclient.utils.Util
import org.json.JSONObject

class HomeStudentSocket(private val activity: Activity) {
    private var tag = "socket.io"
    private var key = AESUtil.loadKey(activity)
    private val socket = SocketConnectionManager.getSocket()

    init {
        socket.on(Events.ON_STUDENT_SCHEDULE_ANSWER.value) { args ->
            Util.safeExecute(tag, activity) {
                val encryptedMessage = args[0] as String
                val decryptedMessage = AESUtil.decrypt(encryptedMessage, key)
                val mi = JSONUtil.fromJson<MessageInput>(decryptedMessage)

                val gridLayout = activity.findViewById<GridLayout>(R.id.gridLayoutStudent)

                activity.runOnUiThread {
                    loadScheduleSkeleton(gridLayout)
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
                        schedules.forEach { (module, day, hour) ->

                            val container = LinearLayout(activity)
                            container.orientation = LinearLayout.HORIZONTAL
                            container.gravity = Gravity.CENTER

                            val textView = TextView(activity)
                            textView.text = module
                            textView.gravity = Gravity.CENTER
                            textView.setTypeface(null, Typeface.BOLD)
                            textView.setBackgroundColor(activity.getColor(R.color.pink))
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.white

                                )
                            )

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            textView.layoutParams = layoutParams

                            container.addView(textView)

                            val params = GridLayout.LayoutParams()
                            // Los eventos serán distribuidos en las filas y columnas según su hora y día
                            params.rowSpec = GridLayout.spec(hour, 1f)
                            params.columnSpec = GridLayout.spec(day, 1f)
                            container.layoutParams = params
                            gridLayout.addView(container)
                        }

                        /*
                        Toast.makeText(
                            activity,
                            "Horario cargado",
                            Toast.LENGTH_SHORT
                        ).show()
                        */

                    }
                } else {
                    var error = ""
                    when (mi.code) {
                        400 -> error = "Semana no lectiva"
                        404 -> error = "No hay horario cargado para esa semana"
                        500 -> error = "No se ha podido cargar el horario"
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


    private fun loadScheduleSkeleton(gridLayout: GridLayout) {
        activity.runOnUiThread {
            // FALTA PASAR ESTO A STRINGS
            val dias = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES")
            val horas = listOf("15:00", "16:00", "17:00", "18:00", "19:00", "20:00")

            // vaciar primero
            gridLayout.removeAllViews()

            // Añadir una columna vacía, es para dar color al bg
            val txt = TextView(activity)
            txt.setBackgroundColor(ContextCompat.getColor(activity, R.color.pantone_dark))
            txt.setTextColor(ContextCompat.getColor(activity, R.color.white))
            val param = GridLayout.LayoutParams()
            param.rowSpec = GridLayout.spec(0, 0.5f)
            param.columnSpec = GridLayout.spec(0, 1f)
            txt.layoutParams = param
            gridLayout.addView(txt)

            // Añadir los días en la primera fila
            for (i in dias.indices) {
                val textView = TextView(activity)
                textView.text = dias[i]
                textView.gravity = Gravity.CENTER
                textView.setTypeface(null, Typeface.BOLD)

                textView.setBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.pantone_dark
                    )
                )
                textView.setTextColor(ContextCompat.getColor(activity, R.color.white))

                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(0, 0.5f)
                params.columnSpec = GridLayout.spec(i + 1, 1f)
                textView.layoutParams = params
                gridLayout.addView(textView)
            }

            // Añadir las horas en la primera columna
            for (i in horas.indices) {
                val textView = TextView(activity)
                textView.text = horas[i]
                textView.gravity = Gravity.CENTER
                textView.setTypeface(null, Typeface.BOLD)

                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(i + 1, 1f)
                params.columnSpec = GridLayout.spec(0, 1f)
                textView.layoutParams = params
                gridLayout.addView(textView)
            }
        }
    }
}
