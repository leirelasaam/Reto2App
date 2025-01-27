package com.elorrieta.alumnoclient

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elorrieta.alumnoclient.entity.LoggedUser
import com.elorrieta.alumnoclient.socketIO.HomeStudentSocket

class HomeStudentActivity : BaseActivity() {
    private var socketClient: HomeStudentSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_home_student, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("HOME", LoggedUser.user.toString());

        socketClient = HomeStudentSocket(this)
        socketClient!!.connect()

        findViewById<Button>(R.id.btnScheduleStudent)
            .setOnClickListener {
                socketClient!!.doGetSchedules()
            }
    }
}