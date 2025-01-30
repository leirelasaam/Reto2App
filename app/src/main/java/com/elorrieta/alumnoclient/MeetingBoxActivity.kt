package com.elorrieta.alumnoclient

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import com.elorrieta.alumnoclient.socketIO.MeetingBoxSocket

class MeetingBoxActivity : BaseActivity() {
    private var socketClient: MeetingBoxSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /*
        setContentView(R.layout.activity_meeting_status)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_meeting_box, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)

        socketClient = MeetingBoxSocket(this)
        socketClient!!.doGetAllMeetings()
    }
}