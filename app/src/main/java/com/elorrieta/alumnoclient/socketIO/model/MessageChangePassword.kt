package com.elorrieta.alumnoclient.socketIO.model

data class MessageChangePassword(

    val email: String,
    val oldPassword: String,
    val newPassword: String
)
