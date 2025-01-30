package com.elorrieta.alumnoclient.socketIO.model

data class MessageRegister(var login: String){
    init {
        login = login.lowercase()
    }
}