package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StudentSchedule (
    var module: String,
    var day: Int,
    var hour: Int
){

}