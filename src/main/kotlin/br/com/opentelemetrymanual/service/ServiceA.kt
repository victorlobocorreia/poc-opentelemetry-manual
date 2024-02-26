package br.com.opentelemetrymanual.service

import org.springframework.stereotype.Service

@Service
class ServiceA {
    fun execute(): String {
        Thread.sleep(3000)
        return "ok"
    }
}