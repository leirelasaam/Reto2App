package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true, value = ["hibernateLazyInitializer"])
data class Role(
    var id: Long? = null,
    var role: String? = null,
    var description: String? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var users: Set<User> = mutableSetOf()
) {
}