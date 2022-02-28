package com.ribbontek.kotlincoroutines.client

import com.ribbontek.kotlincoroutines.model.Todo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(
    value = "todo-client",
    url = "https://jsonplaceholder.typicode.com"
)
interface TodoClient {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/todos"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTodos(): List<Todo>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/todos/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTodoById(@PathVariable("id") id: Long): Todo?
}
