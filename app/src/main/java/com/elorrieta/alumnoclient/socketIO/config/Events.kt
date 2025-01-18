package com.elorrieta.socketsio.sockets.config

/**
 * The events our client is willing to listen or able to send. It is
 * the same class as in the Java Server
 */
enum class Events(val value: String) {
    ON_LOGIN ("onLogin"),
    ON_LOGOUT ("onLogout"),
    ON_LOGIN_ANSWER ("onLoginAnswer"),
    ON_RESET_PASS_EMAIL ("onResetPassEmail"),
    ON_RESET_PASS_EMAIL_ANSWER ("onResetPassEmailAnswer");
}