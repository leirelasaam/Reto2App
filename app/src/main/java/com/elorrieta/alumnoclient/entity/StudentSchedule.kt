package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StudentSchedule (
    var module: String? = null,
    var day: Int? = null,
    var hour: Int? = null,
){

}