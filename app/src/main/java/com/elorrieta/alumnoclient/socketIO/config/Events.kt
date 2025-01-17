package com.elorrieta.socketsio.sockets.config

/**
 * The events our client is willing to listen or able to send. It is
 * the same class as in the Java Server
 */
enum class Events(val value: String) {
    ON_LOGIN ("onLogin"),
    ON_GET_ALL ("onGetAll"),
    ON_LOGOUT ("onLogout"),
    ON_LOGIN_ANSWER ("onLoginAnswer"),
    ON_GET_ALL_ANSWER ("onGetAllAnswer");
}