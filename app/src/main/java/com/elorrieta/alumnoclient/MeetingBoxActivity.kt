package com.elorrieta.alumnoclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.adapters.MeetingBoxAdapter
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.socketIO.MeetingBoxSocket

class MeetingBoxActivity : BaseActivity() {
    private lateinit var socketClient: MeetingBoxSocket
    private lateinit var recycler: RecyclerView
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Con esto conseguimos que la barra de navegación aparezca en la ventana
        val inflater = layoutInflater
        val contentView = inflater.inflate(R.layout.activity_meeting_box, null)
        findViewById<FrameLayout>(R.id.content_frame).addView(contentView)
        recycler = findViewById(R.id.recyclerMeetings)

        socketClient = MeetingBoxSocket(this)
        socketClient.doGetAllMeetings()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadAdapter(meetings: MutableList<Meeting>){
        val adapter = MeetingBoxAdapter(this, meetings, socketClient)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun showEmpty(){
        recycler.visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
    }
}