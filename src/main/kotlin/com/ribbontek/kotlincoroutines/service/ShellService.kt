package com.ribbontek.kotlincoroutines.service

import com.ribbontek.kotlincoroutines.process.KotlinCoroutineProcess
import com.ribbontek.kotlincoroutines.util.logger
import org.slf4j.Logger
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

interface ShellService {
    fun run()
}

@Service
class ShellServiceImpl(
    private val statefulService: StatefulService,
    private val kotlinCoroutineProcess: KotlinCoroutineProcess
) : ShellService {

    private val log: Logger = logger()

    private val displayMenu: String =
        """ |
            |***********************************************
            |Menu: [1,...]
            |    1.    Test Coroutines 
            |    exit. Exit
            |***********************************************
            """.trimMargin()

    override fun run() {
        log.info(displayMenu)
        when (val command = readLine()) {
            "1" -> {
                kotlinCoroutineProcess.retrieveTodos()
                kotlinCoroutineProcess.retrieveTodosAsync()
                kotlinCoroutineProcess.retrieveTodosAsyncFlow()
                kotlinCoroutineProcess.retrieveTodosAsyncChannel()
            }
            "exit" -> {
                println(">>>>>>>> stopped shell runner")
                exitProcess(1)
            }
            else -> {
                println(">>>>>>>> invalid input with $command")
            }
        }
        statefulService.displayResults()
        statefulService.printResults()
        statefulService.reset()
        run()
    }
}
