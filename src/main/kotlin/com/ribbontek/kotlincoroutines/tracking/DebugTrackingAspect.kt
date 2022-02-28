package com.ribbontek.kotlincoroutines.tracking

import com.ribbontek.kotlincoroutines.service.StatefulService
import com.ribbontek.kotlincoroutines.util.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DebugTracking(
    val name: String = ""
)

interface DebugTrackingAspect {
    fun trackExecutionTime(joinPoint: ProceedingJoinPoint, debugTracking: DebugTracking): Any?
}

@Aspect
@Component
class DebugTrackingAspectImpl(
    private val statefulService: StatefulService
) : DebugTrackingAspect {

    private val log: Logger = logger()

    @Around("execution(* *(..)) && @annotation(debugTracking)")
    @Throws(Throwable::class)
    override fun trackExecutionTime(joinPoint: ProceedingJoinPoint, debugTracking: DebugTracking): Any? {
        val methodName = debugTracking.name.takeIf { it.isNotBlank() } ?: joinPoint.signature.name
        val stopWatch = StopWatch()
        stopWatch.start(methodName)
        var exceptionThrown: Exception? = null
        return try {
            // Execute the joint point as usual
            joinPoint.proceed()
        } catch (ex: Exception) {
            exceptionThrown = ex
            throw ex
        } finally {
            stopWatch.stop()
            log.info("${stopWatch.lastTaskName} took ${stopWatch.lastTaskTimeMillis}ms")
            statefulService.put(methodName, stopWatch.lastTaskTimeMillis)
            exceptionThrown?.run {
                log.info(String.format("Exception thrown: %s", this.message))
                this.printStackTrace()
            }
        }
    }
}
