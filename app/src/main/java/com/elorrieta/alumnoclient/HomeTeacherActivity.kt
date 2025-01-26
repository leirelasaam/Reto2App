package com.elorrieta.alumnoclient

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.socketIO.HomeTeacherSocket
import com.elorrieta.alumnoclient.utils.Util
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeTeacherActivity : AppCompatActivity() {
    private var socketClient: HomeTeacherSocket? = null
    private var currentWeek = Util.getCurrentWeek()
    private var selectedWeek = currentWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_teacher)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("HOME", LoggedUser.user.toString());

        socketClient = HomeTeacherSocket(this)
        socketClient!!.doGetSchedules(selectedWeek)

        val weekTxt = findViewById<TextView>(R.id.weekTxt)
        weekTxt.text = "Semana: " + currentWeek
        weekTxt.setTypeface(null, Typeface.BOLD)

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val today = currentDate.format(formatter)

        findViewById<TextView>(R.id.todayTxt).text = today
        findViewById<TextView>(R.id.actualWeekTxt).text = "Semana actual: " + currentWeek

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            socketClient?.doGetSchedules(selectedWeek)
            swipeRefreshLayout.isRefreshing = false
        }

        findViewById<Button>(R.id.btnNextWeek)
            .setOnClickListener {
                if (selectedWeek < 39) {
                    selectedWeek++;
                    weekTxt.text = "Semana seleccionada: " + selectedWeek
                    if (currentWeek == selectedWeek) {
                        weekTxt.setTypeface(null, Typeface.BOLD)
                    } else {
                        weekTxt.setTypeface(null, Typeface.NORMAL)
                    }
                    socketClient!!.doGetSchedules(selectedWeek)

                }
            }

        findViewById<Button>(R.id.btnPrevWeek)
            .setOnClickListener {
                if (selectedWeek > 1) {
                    selectedWeek--;
                    weekTxt.text = "Semana seleccionada: " + selectedWeek
                    if (currentWeek == selectedWeek) {
                        weekTxt.setTypeface(null, Typeface.BOLD)
                    } else {
                        weekTxt.setTypeface(null, Typeface.NORMAL)
                    }
                    socketClient!!.doGetSchedules(selectedWeek)
                }
            }

        findViewById<Button>(R.id.btnFirstWeek)
            .setOnClickListener {
                selectedWeek = 1
                if (currentWeek == selectedWeek) {
                    weekTxt.setTypeface(null, Typeface.BOLD)
                } else {
                    weekTxt.setTypeface(null, Typeface.NORMAL)
                }
                weekTxt.text = "Semana seleccionada: " + selectedWeek
                socketClient!!.doGetSchedules(selectedWeek)
            }

        findViewById<Button>(R.id.btnLastWeek)
            .setOnClickListener {
                selectedWeek = 39
                if (currentWeek == selectedWeek) {
                    weekTxt.setTypeface(null, Typeface.BOLD)
                } else {
                    weekTxt.setTypeface(null, Typeface.NORMAL)
                }
                weekTxt.text = "Semana seleccionada: " + selectedWeek
                socketClient!!.doGetSchedules(selectedWeek)
            }

        findViewById<TextView>(R.id.actualWeekTxt)
            .setOnClickListener {
                selectedWeek = currentWeek
                weekTxt.setTypeface(null, Typeface.BOLD)
                weekTxt.text = "Semana seleccionada: " + selectedWeek
                socketClient!!.doGetSchedules(selectedWeek)
            }
    }
}