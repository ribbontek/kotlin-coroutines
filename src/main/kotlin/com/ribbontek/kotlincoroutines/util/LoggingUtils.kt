package com.ribbontek.kotlincoroutines.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T> T.logger(): Logger = LoggerFactory.getLogger(this!!::class.java)
inline fun <reified A : Any> logger(): Logger = LoggerFactory.getLogger(A::class.java)
