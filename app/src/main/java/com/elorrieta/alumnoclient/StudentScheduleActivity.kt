package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.gridlayout.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.elorrieta.alumnoclient.entity.StudentSchedule
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.HomeStudentSocket
import com.elorrieta.alumnoclient.utils.Util
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudentScheduleActivity : BaseActivity() {
    private var socketClient: HomeStudentSocket? = null
    private var currentWeek = Util.getCurrentWeek()
    private var selectedWeek = currentWeek

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_schedule_student, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        Log.d("STUDENT HOME", LoggedUser.user.toString())

        socketClient = HomeStudentSocket(this)
        socketClient!!.doGetSchedules()

        val weekTxt = findViewById<TextView>(R.id.weekTxt)
        weekTxt.text = getString(R.string.week_label) + " " + currentWeek
        weekTxt.setTypeface(null, Typeface.BOLD)

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val today = currentDate.format(formatter)

        val (startDate, endDate) = Util.getWeekRange(selectedWeek)

        findViewById<TextView>(R.id.todayTxt).text = today
        findViewById<TextView>(R.id.actualWeekTxt).text =getString(R.string.curr_week_label)  + " " +currentWeek
        findViewById<TextView>(R.id.rangeTxt).text = "$startDate - $endDate"

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            socketClient?.doGetSchedules()
            swipeRefreshLayout.isRefreshing = false
        }

        findViewById<ImageView>(R.id.btnNextWeek)
            .setOnClickListener {
                if (selectedWeek < 39) {
                    selectedWeek++
                    updateSelectedWeekText()
                    socketClient!!.doGetSchedules()

                }
            }

        findViewById<ImageView>(R.id.btnPrevWeek)
            .setOnClickListener {
                if (selectedWeek > 1) {
                    selectedWeek--
                    updateSelectedWeekText()
                    socketClient!!.doGetSchedules()
                }
            }

        findViewById<ImageView>(R.id.btnFirstWeek)
            .setOnClickListener {
                selectedWeek = 1
                updateSelectedWeekText()
                socketClient!!.doGetSchedules()
            }

        findViewById<ImageView>(R.id.btnLastWeek)
            .setOnClickListener {
                selectedWeek = 39
                updateSelectedWeekText()
                socketClient!!.doGetSchedules()
            }

        findViewById<TextView>(R.id.actualWeekTxt)
            .setOnClickListener {
                selectedWeek = currentWeek
                updateSelectedWeekText()
                socketClient!!.doGetSchedules()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSelectedWeekText() {
        val weekTxt = findViewById<TextView>(R.id.weekTxt)
        val rangeTxt = findViewById<TextView>(R.id.rangeTxt)

        weekTxt.text = getString(R.string.week_label) + "$selectedWeek"
        weekTxt.setTypeface(null, if (currentWeek == selectedWeek) Typeface.BOLD else Typeface.NORMAL)

        val (startDate, endDate) = Util.getWeekRange(selectedWeek)
        rangeTxt.text = "$startDate - $endDate"
    }

    fun loadScheduleSkeleton(gridLayout: GridLayout) {
        // FALTA PASAR ESTO A STRINGS
        val dias = listOf(
            getString(R.string.day_monday),
            getString(R.string.day_tuesday),
            getString(R.string.day_wednesday),
            getString(R.string.day_thursday),
            getString(R.string.day_friday)
        )

        val horas = listOf("15:00", "16:00", "17:00", "18:00", "19:00", "20:00")

        // vaciar primero
        gridLayout.removeAllViews()

        // Añadir una columna vacía, es para dar color al bg
        val txt = TextView(this)
        txt.setBackgroundColor(ContextCompat.getColor(this, R.color.pantone_dark))
        txt.setTextColor(ContextCompat.getColor(this, R.color.white))
        val param = GridLayout.LayoutParams()
        param.rowSpec = GridLayout.spec(0, 0.5f)
        param.columnSpec = GridLayout.spec(0, 1f)
        txt.layoutParams = param
        gridLayout.addView(txt)

        // Añadir los días en la primera fila
        for (i in dias.indices) {
            val textView = TextView(this)
            textView.text = dias[i]
            textView.gravity = Gravity.CENTER
            textView.setTypeface(null, Typeface.BOLD)

            textView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.pantone_dark
                )
            )
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))

            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(0, 0.5f)
            params.columnSpec = GridLayout.spec(i + 1, 1f)
            textView.layoutParams = params
            gridLayout.addView(textView)
        }

        // Añadir las horas en la primera columna
        for (i in horas.indices) {
            val textView = TextView(this)
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
    fun loadStudentSchedule(schedules: MutableList<StudentSchedule>, gridLayout: GridLayout?) {
        schedules.forEach { (module, day, hour) ->

            val container = LinearLayout(this)
            container.orientation = LinearLayout.HORIZONTAL
            container.gravity = Gravity.CENTER

            val textView = TextView(this)
            textView.text = module
            textView.gravity = Gravity.CENTER
            textView.setTypeface(null, Typeface.BOLD)
            textView.setBackgroundColor(this.getColor(R.color.pink))
            textView.setTextColor(
                ContextCompat.getColor(
                    this,
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
            gridLayout?.addView(container)
        }
    }
}




