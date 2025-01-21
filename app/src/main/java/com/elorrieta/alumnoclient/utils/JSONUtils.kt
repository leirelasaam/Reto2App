package com.elorrieta.alumnoclient.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

object JSONUtils {

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Throws(Exception::class)
    fun <T> getMessageFromJson(json: String, clase: Class<T>): T {
        val rootNode: JsonNode = objectMapper.readTree(json)
        val messageNode = rootNode.get("message")
        return objectMapper.treeToValue(messageNode, clase)
    }
}