/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import java.util.logging.Logger;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.grpc.ManagedChannelBuilder;
import io.jenkins.plugins.elasticstacklogs.opentelemetry.TestLogExporter;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.BatchLogProcessor;
import org.kohsuke.stapler.DataBoundConstructor;
import io.opentelemetry.sdk.logs.LogEmitter;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;

/**
 * This input send the log records to an OpenTelemetry service with support for logs.
 */
public class OpentelemetryLogsInput extends Input {
  public static final int BATCH_SIZE = 10;
  public static final int MAX_QUEUE_SIZE = 20;
  public static final int SCHEDULE_DELAY_MILLIS = 2000;
  public static final String HTTPS = "https://";
  public static final String HTTP = "http://";
  public static final String GRPC = "grpc://";
  public static final String PORT_SP = ":";
  public static final int HTTP_PORT = 80;
  public static final int HTTPS_PORT = 443;
  public static final int GRPC_PORT = 4317;
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInput.class.getName());
  public static final String EMITTER_NAME = "Jenkins";
  public static final String INSTRUMENTATION_VERSION = "0.1a";
  @NonNull
  private String endpoint;

  private final LogEmitter logEmitter;

  @DataBoundConstructor
  public OpentelemetryLogsInput(@NonNull String endpoint) {
    this.endpoint = endpoint;
    String protocol = getProtocol(endpoint);
    int port = getEndpointPort(endpoint);
    String host = getEndpointHost(endpoint);
    //TODO allow authenticate connections
    TestLogExporter exporter;
    BatchLogProcessor processor;
    if (protocol.equals(HTTPS)) {
      exporter = new TestLogExporter(ManagedChannelBuilder.forAddress(host, port).useTransportSecurity());
    } else {
      exporter = new TestLogExporter(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }
    exporter.setOnCall(() -> LOGGER.info("Batch Log processed"));
    processor = BatchLogProcessor.builder(exporter).setMaxExportBatchSize(BATCH_SIZE).setMaxQueueSize(MAX_QUEUE_SIZE)
                                 .setScheduleDelayMillis(SCHEDULE_DELAY_MILLIS).build();
    SdkLogEmitterProvider provider = SdkLogEmitterProvider.builder().addLogProcessor(processor).build();
    logEmitter = provider.logEmitterBuilder(EMITTER_NAME).setInstrumentationVersion(INSTRUMENTATION_VERSION).build();
  }

  private static int getEndpointPort(@NonNull String endpoint) {
    int port = GRPC_PORT;
    String protocol = getProtocol(endpoint);
    String endpointNoProtocol = endpoint.replace(protocol, "");
    if (endpointNoProtocol.contains(PORT_SP)) {
      port = Integer.parseInt(endpointNoProtocol.split(PORT_SP)[1]);
    } else if (protocol.equals(HTTP)) {
      port = HTTP_PORT;
    } else if (protocol.equals(HTTPS)) {
      port = HTTPS_PORT;
    } else if (protocol.equals(GRPC)) {
      port = GRPC_PORT;
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
    String ret = GRPC;
    if (endpoint.contains(HTTPS)) {
      ret = HTTPS;
    } else if (endpoint.contains(HTTP)) {
      ret = HTTP;
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
    logEmitter.logBuilder().setBody(value).emit();
    return true;
  }

  public long getCount() {
    return exporter.getCallCount();
  }
}
