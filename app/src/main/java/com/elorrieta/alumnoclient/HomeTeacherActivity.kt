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

class HomeTeacherActivity : AppCompatActivity() {
    private var socketClient: HomeTeacherSocket? = null
    private var currentWeek = 1
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
        weekTxt.text = "Semana actual: " + currentWeek
        weekTxt.setTypeface(null, Typeface.BOLD)

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            socketClient?.doGetSchedules(selectedWeek)
            swipeRefreshLayout.isRefreshing = false
        }

        findViewById<Button>(R.id.btnNextWeek)
            .setOnClickListener {
                if (selectedWeek < 25){
                    selectedWeek++;
                    if (currentWeek == selectedWeek){
                        weekTxt.text = "Semana actual: " + selectedWeek
                        weekTxt.setTypeface(null, Typeface.BOLD)
                    } else {
                        weekTxt.text = "Semana: " + selectedWeek
                        weekTxt.setTypeface(null, Typeface.NORMAL)
                    }
                    socketClient!!.doGetSchedules(selectedWeek)

                }
            }

        findViewById<Button>(R.id.btnPrevWeek)
            .setOnClickListener {
                if (selectedWeek > 1){
                    selectedWeek--;
                    if (currentWeek == selectedWeek){
                        weekTxt.text = "Semana actual: " + selectedWeek
                        weekTxt.setTypeface(null, Typeface.BOLD)
                    } else {
                        weekTxt.text = "Semana: " + selectedWeek
                        weekTxt.setTypeface(null, Typeface.NORMAL)
                    }
                    socketClient!!.doGetSchedules(selectedWeek)
                }
            }
    }
}