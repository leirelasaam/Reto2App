package com.elorrieta.alumnoclient.entity

import java.sql.Timestamp

/**
 * The entity Alumno, used to manipulate data as objects instead of JSON
 */
data class User(
    var name: String,
    var email: String,
)