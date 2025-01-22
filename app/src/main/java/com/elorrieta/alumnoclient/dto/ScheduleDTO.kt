package com.elorrieta.alumnoclient.dto

data class ScheduleDTO (
    var id: Long,
    var module: String,
    var hour: Int,
    var day: Int,
)