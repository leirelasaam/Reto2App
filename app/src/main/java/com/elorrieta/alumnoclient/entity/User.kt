package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.sql.Timestamp


@JsonIgnoreProperties(ignoreUnknown = true)
data class  User(
    var id: Long? = null,
    var role: Role? = null,
    var name: String? = null,
    var email: String? = null,
    var emailVerifiedAt: Timestamp? = null,
    var password: String? = null,
    var rememberToken: String? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var deletedAt: Timestamp? = null,
    var lastname: String? = null,
    var pin: String? = null,
    var address: String? = null,
    var phone1: String? = null,
    var phone2: String? = null,
    var photo: ByteArray? = null,
    var intensive: Boolean = false,
    var registered: Boolean = false,
    var modules: Set<Module> = mutableSetOf(),
    var enrollments: Set<Enrollment> = mutableSetOf(),
    var meetings: Set<Meeting> = mutableSetOf()
): Serializable



