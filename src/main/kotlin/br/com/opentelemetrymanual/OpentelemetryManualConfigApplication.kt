package br.com.opentelemetrymanual

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
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
        .addSpanProcessor(
            BatchSpanProcessor.builder(
                OtlpHttpSpanExporter.builder().setEndpoint("https://otlp.nr-data.net:4318/v1/traces")
                    .addHeader("api-key", "c775892ce0ef53d2063f627439233e41FFFFNRAL").build()
            ).build()
        )
        .setResource(resource)
        .build()

    val sdkMeterProvider = SdkMeterProvider.builder()
        .registerMetricReader(
            PeriodicMetricReader.builder(
                OtlpHttpMetricExporter.builder().setEndpoint("https://otlp.nr-data.net:4318/v1/metrics")
                    .addHeader("api-key", "c775892ce0ef53d2063f627439233e41FFFFNRAL").build()
            ).build()
        )
        .setResource(resource)
        .build()

    //    val sdkLoggerProvider = SdkLoggerProvider.builder()
//        .addLogRecordProcessor(
//            BatchLogRecordProcessor.builder(  // https://opentelemetry.io/docs/languages/java/instrumentation/#logrecord-processor
//                OtlpHttpLogRecordExporter.builder().setEndpoint("https://otlp.nr-data.net")
//                    .addHeader("api-key", "c775892ce0ef53d2063f627439233e41FFFFNRAL").build()
//            ).build()
//        )
//        .setResource(resource)
//        .build()

    val sdkLoggerProvider = SdkLoggerProvider.builder()
        .addLogRecordProcessor(BatchLogRecordProcessor.builder(SystemOutLogRecordExporter.create()).build())
        .setResource(resource)
        .build()

    val openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setMeterProvider(sdkMeterProvider)
        .setLoggerProvider(sdkLoggerProvider)
        .setPropagators(
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance()
                )
            )
        )
        .buildAndRegisterGlobal()

    return openTelemetry
}

// exporters: https://opentelemetry.io/docs/languages/java/exporters/