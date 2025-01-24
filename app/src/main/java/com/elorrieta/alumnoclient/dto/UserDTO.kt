package com.elorrieta.alumnoclient.dto

data class UserDTO (
    var id: Long,
    var role: String? = null,
    var name: String,
    var email: String,
    var password: String,
    var lastname: String,
    var pin: String,
    var address: String,
    var phone1: String,
    var phone2: String? = null,
    var photo: ByteArray? = null,
    var intensive: Boolean,
    var registered: Boolean
)