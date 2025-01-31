package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true)
data class Module(
    val id: Long? = null,
    val cycle: Cycle? = null,
    val user: User? = null,
    val code: String? = null,
    val name: String? = null,
    val hours: Int? = null,
    val course: Byte? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val enrollments: Set<Enrollment> = emptySet(),
    val schedules: Set<Schedule> = emptySet(),
    val documents: Set<Document> = emptySet()
) {
}
