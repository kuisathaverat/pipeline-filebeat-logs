package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class OpentelemetryLogsInput implements Input {
  private static final String INSTRUMENTATION_NAME = OpentelemetryLogsInput.class.getName();
  private final int port;
  @NonNull
  private final String host;

  private final OpenTelemetry openTelemetry;
  private final Tracer tracer;
  private final LongCounter counter;

  public OpentelemetryLogsInput(String host, int port) {
    this.port = port;
    this.host = host;
    this.openTelemetry = OpentelemetryLogsInput.initOpenTelemetry();
    tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    counter = GlobalMeterProvider.getMeter(INSTRUMENTATION_NAME).longCounterBuilder("work_done").build();
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    Span span = this.tracer.spanBuilder("log line").startSpan();
    span.addEvent(value);
    span.end();
    return false;
  }

  /**
   * Initializes an OpenTelemetry SDK with a logging exporter and a SimpleSpanProcessor.
   *
   * @return A ready-to-use {@link OpenTelemetry} instance.
   */
  public static OpenTelemetry initOpenTelemetry() {
    // Tracer provider configured to export spans with SimpleSpanProcessor using
    // the logging exporter.
    LoggingSpanExporter exporter = new LoggingSpanExporter();
    SdkTracerProvider tracerProvider =
      SdkTracerProvider.builder()
                       .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                       .build();
    return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();
  }
}
