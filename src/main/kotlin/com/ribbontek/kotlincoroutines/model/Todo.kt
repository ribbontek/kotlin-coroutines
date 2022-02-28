package com.ribbontek.kotlincoroutines.model

import java.math.BigDecimal

data class Todo(
    val userId: Long,
    val id: Long,
    val title: String,
    val completed: Boolean
)

data class BasicTodo(
    val id: Long,
    val title: String
)

data class TodoSummary(
    val userWithTotalTodoCount: Map<Long, Long>,
    val userWithCompletedTodoRatio: Map<Long, BigDecimal>
)
