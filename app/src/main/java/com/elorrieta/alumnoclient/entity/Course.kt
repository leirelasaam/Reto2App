package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.sql.Date

@JsonIgnoreProperties(ignoreUnknown = true)
class Course(
    var id: Long,
    var name: String? =null,
    var date: Date? = null,
    var contact: String? = null,
    var description: String? = null,
    var schedule: String? = null,
    var latitude: Float? = null,
    var longitude: Float? = null
): Serializable
//Es serializable porque es necesario para pasarlo por un intent