package br.com.opentelemetrymanual.controller

import br.com.opentelemetrymanual.openTelemetry
import br.com.opentelemetrymanual.service.ServiceA
import br.com.opentelemetrymanual.service.ServiceB
import io.opentelemetry.api.trace.Tracer
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController(
    private val serviceA: ServiceA,
    private val serviceB: ServiceB,
    private val tracer: Tracer = openTelemetry().getTracer("TesteController")
) {
    @GetMapping()
    fun testOpenTelemetry(@RequestParam(required = false) service: String): Any {
        val span = tracer.spanBuilder("teste").startSpan()
        try {
            span.makeCurrent()
            if (service == "A") {
                return serviceA.execute()
            }

            if (service == "B") {
                return serviceB.execute()
            }

            return HttpStatus.OK
        } catch (t: Throwable) {
            span.recordException(t)
            throw t
        } finally {
            span.end()
        }
    }
}