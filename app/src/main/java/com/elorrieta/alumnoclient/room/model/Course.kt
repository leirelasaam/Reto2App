package com.elorrieta.alumnoclient.room.model

data class Course (
    val name: String,
    val date: String,
    val schedule: String,
    val contact: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)