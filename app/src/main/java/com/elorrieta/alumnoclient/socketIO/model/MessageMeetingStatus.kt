package com.elorrieta.alumnoclient.socketIO.model

data class MessageMeetingStatus(
    var userId: Long?,
    var meetingId: Long?,
    val status: String
) {
}