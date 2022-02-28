package com.ribbontek.kotlincoroutines

import com.ribbontek.kotlincoroutines.service.ShellService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy
class KotlinCoroutinesApplication(
    @Autowired private val shellService: ShellService
) : CommandLineRunner {
    override fun run(vararg args: String) {
        shellService.run()
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinCoroutinesApplication>(*args) {
        setLogStartupInfo(true)
        webApplicationType = WebApplicationType.NONE
    }
}
