package com.ribbontek.kotlincoroutines.service

import com.ribbontek.kotlincoroutines.mapping.toFunctionData
import com.ribbontek.kotlincoroutines.util.logger
import com.ribbontek.kotlincoroutines.util.toPrettyJson
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

interface StatefulService {
    fun displayResults()
    fun reset()
    fun put(functionName: String, timeMillis: Long)
    fun printResults(fileNameSuffix: String = "-results")
}

@Service
class StatefulServiceImpl : StatefulService {

    private val functionTimings: ConcurrentHashMap<String, MutableList<Long>> = ConcurrentHashMap()
    private val log: Logger = logger()

    override fun displayResults() {
        log.info(displayWrapper(functionTimings.toFunctionData().toPrettyJson()))
    }

    override fun reset() {
        functionTimings.clear()
    }

    override fun put(functionName: String, timeMillis: Long) {
        functionTimings[functionName]?.add(timeMillis) ?: functionTimings.put(functionName, mutableListOf(timeMillis))
    }

    private fun displayWrapper(content: String): String {
        return """ |
            |***********************************************
            | Results: 
            | $content
            |***********************************************
            """.trimMargin()
    }

    override fun printResults(fileNameSuffix: String) {
        functionTimings.takeIf { it.isNotEmpty() }
            ?.also { results ->
                try {
                    val fileName = "${LocalDateTime.now().toString() + fileNameSuffix}.json"
                    val fileContent = results.toFunctionData().toPrettyJson().toByteArray(Charsets.UTF_8)
                    with(File("files")) { if (!exists()) mkdir() }
                    File("files/$fileName").writeBytes(fileContent)
                    log.info("Saved results to $fileName")
                } catch (ex: Exception) {
                    log.error("encountered ex: ", ex)
                }
            }
    }
}
