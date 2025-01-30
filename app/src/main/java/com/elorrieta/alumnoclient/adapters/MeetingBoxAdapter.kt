package com.elorrieta.alumnoclient.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.Document

class MeetingBoxAdapter(private val context: Context?, private var meetings: MutableList<Document>) : RecyclerView.Adapter<MeetingBoxAdapter.MeetingBoxViewHolder>(){

    class MeetingBoxViewHolder(view: View) :RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameMeetingTxt)
        val status: TextView = view.findViewById(R.id.statusMeetingTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): MeetingBoxViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_meeting, parent, false)
        return MeetingBoxViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder:MeetingBoxViewHolder, position:Int) {
        val meeting = meetings[position]
        //holder.name.text = meeting.subject
        //holder.status.text = meeting.status
    }

    override fun getItemCount(): Int {
        return meetings.size
    }
}