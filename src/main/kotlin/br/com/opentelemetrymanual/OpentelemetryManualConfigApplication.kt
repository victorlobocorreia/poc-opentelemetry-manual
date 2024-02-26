package br.com.opentelemetrymanual

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpentelemetryManualConfigApplication

fun main(args: Array<String>) {
	runApplication<OpentelemetryManualConfigApplication>(*args)
}
