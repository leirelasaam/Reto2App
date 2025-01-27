package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeacherSchedule (
    var event: String? = null,
    var day: Int? = null,
    var hour: Int? = null,
    var type: String? = null,
    var status: String? = null,
    var meetingId: Long? = null
){

}