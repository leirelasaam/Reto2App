package com.elorrieta.alumnoclient.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elorrieta.alumnoclient.R
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.singletons.LoggedUser

class MeetingBoxAdapter(private val context: Context?, private var meetings: MutableList<Meeting>) : RecyclerView.Adapter<MeetingBoxAdapter.MeetingBoxViewHolder>(){

    class MeetingBoxViewHolder(view: View) :RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleMeetingTxt)
        val subject: TextView = view.findViewById(R.id.subjectMeetingTxt)
        val status: TextView = view.findViewById(R.id.statusMeetingTxt)
        val statusPersonal: TextView = view.findViewById(R.id.statusMeetingPersonalTxt)
        val owner: TextView = view.findViewById(R.id.ownerMeetingTxt)
        val date: TextView = view.findViewById(R.id.dateMeetingTxt)
        val room: TextView = view.findViewById(R.id.roomMeetingTxt)
        val participants: TextView = view.findViewById(R.id.participantsMeetingTxt)
        val participantsCount: TextView = view.findViewById(R.id.participantsCountMeetingTxt)
        val ownerButtons: LinearLayout = view.findViewById(R.id.linearButtonsOwner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): MeetingBoxViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_meeting, parent, false)
        return MeetingBoxViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder:MeetingBoxViewHolder, position:Int) {
        val meeting = meetings[position]
        holder.title.text = meeting.title
        holder.subject.text = meeting.subject
        holder.status.text = meeting.status
        holder.owner.text = meeting.user?.name + " " + meeting.user?.lastname
        holder.date.text = "Día: " + meeting.day + "\nHora: " + meeting.time + "\nSemana: " + meeting.week
        holder.room.text = meeting.room.toString()
        holder.participantsCount.text = meeting.participants.size.toString()

        for (participant in meeting.participants) {
            holder.participants.text = holder.participants.text.toString() + participant.user.name + " " + participant.user.lastname + "; "
            if (participant.user.id == LoggedUser.user?.id) {
                holder.statusPersonal.text = participant.status

                if (meeting.user?.id == participant.user.id){
                    holder.owner.text = "Yo"
                    holder.ownerButtons.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return meetings.size
    }
}