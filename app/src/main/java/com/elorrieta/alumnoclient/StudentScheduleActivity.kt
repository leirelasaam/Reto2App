package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.elorrieta.alumnoclient.singletons.LoggedUser
import com.elorrieta.alumnoclient.socketIO.HomeStudentSocket
import com.elorrieta.alumnoclient.utils.Util
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudentScheduleActivity : BaseActivity() {
    private var socketClient: HomeStudentSocket? = null
    private var currentWeek = Util.getCurrentWeek()
    private var selectedWeek = currentWeek

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_schedule_student, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        Log.d("STUDENT HOME", LoggedUser.user.toString());

        socketClient = HomeStudentSocket(this)
        socketClient!!.doGetSchedules()

        val weekTxt = findViewById<TextView>(R.id.weekTxt)
        weekTxt.text = "Semana: " + currentWeek
        weekTxt.setTypeface(null, Typeface.BOLD)

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val today = currentDate.format(formatter)

        val (startDate, endDate) = Util.getWeekRange(selectedWeek)

        findViewById<TextView>(R.id.todayTxt).text = today
        findViewById<TextView>(R.id.actualWeekTxt).text = "Semana actual: " + currentWeek
        findViewById<TextView>(R.id.rangeTxt).text = startDate + " - " + endDate

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            socketClient?.doGetSchedules()
            swipeRefreshLayout.isRefreshing = false
        }

        findViewById<ImageView>(R.id.btnNextWeek)
            .setOnClickListener {
                if (selectedWeek < 39) {
                    selectedWeek++;
                    updateSelectedWeekText()
                    socketClient!!.doGetSchedules()

                }
            }

        findViewById<ImageView>(R.id.btnPrevWeek)
            .setOnClickListener {
                if (selectedWeek > 1) {
                    selectedWeek--;
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

    private fun updateSelectedWeekText() {
        val weekTxt = findViewById<TextView>(R.id.weekTxt)
        val rangeTxt = findViewById<TextView>(R.id.rangeTxt)

        weekTxt.text = "Semana: $selectedWeek"
        weekTxt.setTypeface(null, if (currentWeek == selectedWeek) Typeface.BOLD else Typeface.NORMAL)

        val (startDate, endDate) = Util.getWeekRange(selectedWeek)
        rangeTxt.text = "$startDate - $endDate"
    }
}