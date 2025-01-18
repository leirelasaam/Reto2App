package com.elorrieta.alumnoclient.socketIO.model

data class MessageLogin(var login: String, val password: String){
    init {
        login = login.lowercase()
    }
}