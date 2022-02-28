package com.ribbontek.kotlincoroutines.mapping

import com.ribbontek.kotlincoroutines.model.FunctionData
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToLong

fun ConcurrentHashMap<String, MutableList<Long>>.toFunctionData(): List<FunctionData> {
    return map {
        FunctionData(
            name = it.key,
            average = it.value.average().roundToLong(),
            minValue = it.value.minOrNull() ?: 0,
            maxValue = it.value.maxOrNull() ?: 0
        )
    }
}
