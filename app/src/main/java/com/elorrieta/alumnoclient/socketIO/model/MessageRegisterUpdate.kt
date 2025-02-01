package com.elorrieta.alumnoclient.socketIO.model

data class MessageRegisterUpdate(
    var name: String,
    var email: String,
    var password: String,
    var lastname: String,
    var pin: String,
    var address: String,
    var phone1: String,
    var phone2: String? = null,
    var photo: ByteArray? = null,
    var registered: Boolean,
)