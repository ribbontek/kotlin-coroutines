package com.ribbontek.kotlincoroutines.process

import com.ribbontek.kotlincoroutines.client.TodoClient
import com.ribbontek.kotlincoroutines.mapping.toBasicTodo
import com.ribbontek.kotlincoroutines.model.BasicTodo
import com.ribbontek.kotlincoroutines.model.Todo
import com.ribbontek.kotlincoroutines.tracking.DebugTracking
import com.ribbontek.kotlincoroutines.util.logger
import feign.FeignException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

interface KotlinCoroutineProcess {
    fun retrieveTodos(): List<BasicTodo>
    fun retrieveTodosAsync(): List<BasicTodo>
    fun retrieveTodosAsyncFlow(): List<BasicTodo>
    fun retrieveTodosAsyncChannel(): List<BasicTodo>
}

@Component
class KotlinCoroutineProcessImpl(
    private val todoClient: TodoClient
) : KotlinCoroutineProcess {

    private val log = logger()

    @DebugTracking
    override fun retrieveTodos(): List<BasicTodo> = getTodos().map { it.processTodosWithSleep() }

    @DebugTracking
    override fun retrieveTodosAsync(): List<BasicTodo> = runBlocking {
        retrieveTodosAsyncFromClient().mapAsync { it.processTodosWithDelay() }
    }

    @DebugTracking
    override fun retrieveTodosAsyncFlow(): List<BasicTodo> = runBlocking {
        retrieveTodosFlowFromClient().buffer()
            .catch { ex -> log.error("encountered exception: ", ex) }
            .transform { todos ->
                todos.mapAsyncFlow { it.processTodosWithDelayFlow() }.also { emit(it) }
            }
            .single()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @DebugTracking
    override fun retrieveTodosAsyncChannel(): List<BasicTodo> = runBlocking {
        async { retrieveTodosChannelFromClient().tryReceive().getOrThrow() }.await()
            .mapAsync { it.processTodosWithDelayChannel().tryReceive().getOrThrow() }
    }

    private fun getTodos(): List<Todo> {
        return try {
            todoClient.getTodos().also {
                log.info("successfully retrieved ${it.size} todos")
            }
        } catch (ex: FeignException) {
            log.error("encountered exception: ", ex)
            emptyList()
        }
    }

    private fun Todo.processTodosWithSleep(): BasicTodo {
        log.info("start processing todo: $id")
        Thread.sleep(100)
        return toBasicTodo().also { log.info("finished processing todo: $id") }
    }

    private suspend fun retrieveTodosAsyncFromClient(): List<Todo> = withContext(Dispatchers.IO) { getTodos() }

    private suspend fun Todo.processTodosWithDelay(): BasicTodo = withContext(Dispatchers.Default) {
        log.info("start processing todo: $id")
        delay(100)
        toBasicTodo().also { log.info("finished processing todo: $id") }
    }

    private suspend fun retrieveTodosFlowFromClient(): Flow<List<Todo>> =
        flow { getTodos().also { emit(it) } }.flowOn(Dispatchers.IO)

    private suspend fun Todo.processTodosWithDelayFlow(): Flow<BasicTodo> = flow {
        log.info("start processing todo: $id")
        delay(100)
        toBasicTodo().also { log.info("finished processing todo: $id"); emit(it) }
    }.flowOn(Dispatchers.Default)

    @ExperimentalCoroutinesApi
    private suspend fun retrieveTodosChannelFromClient(): ReceiveChannel<List<Todo>> = withContext(Dispatchers.IO) {
        produce(capacity = UNLIMITED) { getTodos().also { send(it) } }
    }

    @ExperimentalCoroutinesApi
    private suspend fun Todo.processTodosWithDelayChannel(): ReceiveChannel<BasicTodo> =
        withContext(Dispatchers.Default) {
            produce(capacity = UNLIMITED) {
                log.info("start processing todo: $id")
                delay(100)
                toBasicTodo().also { log.info("finished processing todo: $id"); send(it) }
            }
        }

    private suspend fun <T, R> Iterable<T>.mapAsync(transform: suspend (T) -> R): List<R> = coroutineScope {
        map { async { transform(it) } }.map { it.await() }
    }

    private suspend fun <T, R> Iterable<T>.mapAsyncFlow(transform: suspend (T) -> Flow<R>): List<R> = coroutineScope {
        mapAsync { transform(it).single() }
    }
}
