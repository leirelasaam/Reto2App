package com.elorrieta.alumnoclient.entity

object User {
    /**
     * The entity, used to manipulate data as objects instead of JSON
     */
    data class User (private val email: String, private val password: String)
}