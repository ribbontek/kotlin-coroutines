package com.ribbontek.kotlincoroutines.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

val objectMapper: ObjectMapper = createObjectMapper()

inline fun <reified T : Any> String.fromJson() = objectMapper.readValue<T>(this)

fun Any.toJson(): String = objectMapper.writeValueAsString(this)

fun Any.toPrettyJson(): String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

fun createObjectMapper(): ObjectMapper = with(ObjectMapper()) {
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    registerModule(JavaTimeModule())
    registerModule(KotlinModule())
}
