package com.elorrieta.alumnoclient.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
    val id: Long? = null,
    val module: Module? = null,
    val name: String? = null,
    val extension: String? = null,
    val route: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val file: ByteArray? = null
) {
}