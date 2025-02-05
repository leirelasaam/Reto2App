package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp


@JsonIgnoreProperties(ignoreUnknown = true)
class Meeting(

    val id: Long? = null,
    val user: User? = null,
    val day: Byte = 0,
    val time: Byte = 0,
    val week: Byte = 0,
    val status: String? = null,
    val title: String? = null,
    val room: Byte? = null,
    val subject: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val participants: Set<Participant> = emptySet()
)