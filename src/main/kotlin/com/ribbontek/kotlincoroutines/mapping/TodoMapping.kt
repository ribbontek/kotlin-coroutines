package com.ribbontek.kotlincoroutines.mapping

import com.ribbontek.kotlincoroutines.model.BasicTodo
import com.ribbontek.kotlincoroutines.model.Todo

fun Todo.toBasicTodo(): BasicTodo =
    BasicTodo(
        id = id,
        title = title
    )
