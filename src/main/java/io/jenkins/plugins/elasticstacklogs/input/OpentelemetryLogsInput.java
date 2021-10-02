package io.jenkins.plugins.elasticstacklogs.input;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.grpc.ManagedChannelBuilder;
import io.jenkins.plugins.elasticstacklogs.opentelemetry.TestLogExporter;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.BatchLogProcessor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This input send the log records to an OpenTelemetry service with support for logs.
 */
public class OpentelemetryLogsInput implements Input {
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInput.class.getName());
  public static final int BATCH_SIZE = 10;
  public static final int MAX_QUEUE_SIZE = 20;
  public static final int SCHEDULE_DELAY_MILLIS = 2000;
  private final int port;
  @NonNull
  private final String host;

  private final BatchLogProcessor processor;
  private final TestLogExporter exporter;

  public OpentelemetryLogsInput(String host, int port) {
    this.port = port;
    this.host = host;
    //TODO allow TLS connections
    //TODO allow authenticate connections
    exporter = new TestLogExporter(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    exporter.setOnCall(()->{
      LOGGER.info("Batch Log processed");
    });
    processor = BatchLogProcessor.builder(exporter)
                                 .setMaxExportBatchSize(BATCH_SIZE)
                                 .setMaxQueueSize(MAX_QUEUE_SIZE)
                                 .setScheduleDelayMillis(SCHEDULE_DELAY_MILLIS)
                                 .build();
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    LogRecord record = LogRecord.builder().setName(this.getClass().getName()).setBody(value).build();
    processor.addLogRecord(record);
    return true;
  }

  public long getCount(){
    return exporter.getCallCount();
  }
}
