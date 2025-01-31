package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true)
class Cycle(
    val id: Long? = null,
    val code: String? = null,
    val name: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val modules: Set<Module> = emptySet()
) {
}