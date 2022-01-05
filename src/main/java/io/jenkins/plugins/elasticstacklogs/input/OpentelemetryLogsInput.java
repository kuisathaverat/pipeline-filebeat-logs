/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.InMemoryLogExporter;
import io.opentelemetry.sdk.logs.export.LogExporter;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This input send the log records to an OpenTelemetry service with support for logs.
 */
public class OpentelemetryLogsInput extends Input {
  public static final int BATCH_SIZE = 10;
  public static final int MAX_QUEUE_SIZE = 20;
  public static final int SCHEDULE_DELAY_MILLIS = 2000;
  public static final String HTTPS = "https://";
  public static final String HTTP = "http://";
  public static final String TEST = "test://";
  public static final String PORT_SP = ":";
  public static final int HTTP_PORT = 80;
  public static final int HTTPS_PORT = 443;
  public static final int GRPC_PORT = 4317;
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInput.class.getName());
  @NonNull
  private String endpoint;

  private BatchLogProcessor processor;
  private LogExporter exporter;
  private SdkLogEmitterProvider logEmitterProvider;
  private final AtomicLong count = new AtomicLong(0);

  @DataBoundConstructor
  public OpentelemetryLogsInput(@NonNull String endpoint) {
    this.endpoint = endpoint;
    String protocol = getProtocol(endpoint);
    int port = getEndpointPort(endpoint);
    String host = getEndpointHost(endpoint);
    //TODO allow authenticate connections
    if (protocol.equals(TEST)){
      exporter = InMemoryLogExporter.create();
    } else {
      exporter = OtlpGrpcLogExporter.builder()
                                    .addHeader("key","value")
                                    .setEndpoint(endpoint)
                                    .build();
    }
    processor = BatchLogProcessor.builder(exporter)
                                 .setMaxExportBatchSize(BATCH_SIZE)
                                 .setMaxQueueSize(MAX_QUEUE_SIZE)
                                 .setScheduleDelay(SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                                 .build();
    logEmitterProvider = SdkLogEmitterProvider.builder().addLogProcessor(processor).build();
  }

  private static int getEndpointPort(@NonNull String endpoint) {
    int port = HTTP_PORT;
    String protocol = getProtocol(endpoint);
    String endpointNoProtocol = endpoint.replace(protocol, "");
    if (endpointNoProtocol.contains(PORT_SP)) {
      port = Integer.parseInt(endpointNoProtocol.split(PORT_SP)[1]);
    } else if (protocol.equals(HTTP)) {
      port = HTTP_PORT;
    } else if (protocol.equals(HTTPS)) {
      port = HTTPS_PORT;
    }
    return port;
  }

  private static String getEndpointHost(@NonNull String endpoint) {
    String protocol = getProtocol(endpoint);
    String endpointNoProtocol = endpoint.replace(protocol, "");
    String host = endpointNoProtocol;
    if (endpointNoProtocol.contains(PORT_SP)) {
      host = endpointNoProtocol.split(PORT_SP)[0];
    }
    return host;
  }

  private static String getProtocol(String endpoint) {
    String ret = HTTP;
    if (endpoint.contains(HTTPS)) {
      ret = HTTPS;
    } else if (endpoint.contains(HTTP)) {
      ret = HTTP;
    } else if (endpoint.contains(TEST)) {
      ret = TEST;
    }
    return ret;
  }

  @NonNull
  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(@NonNull String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    logEmitterProvider
      .logEmitterBuilder(getClass().getName())
      .build()
      .logBuilder()
      .setName(getClass().getName())
      .setBody(value)
      .emit();
    count.incrementAndGet();
    return true;
  }

  public long getCount() {
    return count.get();
  }
}
