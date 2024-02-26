package br.com.opentelemetrymanual

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.logging.LoggingMetricExporter
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class OpentelemetryManualConfigApplication

fun main(args: Array<String>) {
    runApplication<OpentelemetryManualConfigApplication>(*args)
}

@Bean
fun openTelemetry(): OpenTelemetry {
    val resource = Resource.getDefault().toBuilder()
		.put(ResourceAttributes.SERVICE_NAME, "poc-opentelemetry-manual")
        .put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build()

	val sdkTracerProvider = SdkTracerProvider.builder()
		.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
		.setResource(resource)
		.build()

	val sdkMeterProvider = SdkMeterProvider.builder()
		.registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
		.setResource(resource)
		.build()

	val sdkLoggerProvider = SdkLoggerProvider.builder()
		.addLogRecordProcessor(BatchLogRecordProcessor.builder(SystemOutLogRecordExporter.create()).build())
		.setResource(resource)
		.build()

	val openTelemetry = OpenTelemetrySdk.builder()
		.setTracerProvider(sdkTracerProvider)
		.setMeterProvider(sdkMeterProvider)
		.setLoggerProvider(sdkLoggerProvider)
		.setPropagators(ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
		.buildAndRegisterGlobal()

	return openTelemetry
}
