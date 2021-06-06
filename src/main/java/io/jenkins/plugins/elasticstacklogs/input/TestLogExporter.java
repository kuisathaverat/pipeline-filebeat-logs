/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.elasticstacklogs.input;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.LogExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class TestLogExporter implements LogExporter {

  private final ArrayList<LogRecord> records = new ArrayList<>();
  private final ManagedChannel channel;
  private final LogsServiceGrpc.LogsServiceBlockingStub blockingStub;
  private final LogsServiceGrpc.LogsServiceStub asyncStub;
  @Nullable private Runnable onCall = null;
  private int callCount = 0;

  public TestLogExporter(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = LogsServiceGrpc.newBlockingStub(channel);
    asyncStub = LogsServiceGrpc.newStub(channel);
  }

  @Override
  public synchronized CompletableResultCode export(Collection<LogRecord> records) {
    this.records.addAll(records);
    callCount++;
    if (onCall != null) {
      onCall.run();
    }
    InstrumentationLibraryLogs.Builder instrumentationLibraryLogs = InstrumentationLibraryLogs.newBuilder().addAllLogs(
      convertToProto(this.records));
    ResourceLogs resourceLogs =
      ResourceLogs.newBuilder().addInstrumentationLibraryLogs(instrumentationLibraryLogs).build();
    ExportLogsServiceRequest request = ExportLogsServiceRequest.newBuilder().addResourceLogs(resourceLogs).build();
    ExportLogsServiceResponse response = blockingStub.export(request);
    //asyncStub.export(request, responseObserver);
    return null;
  }

  private Iterable<? extends io.opentelemetry.proto.logs.v1.LogRecord> convertToProto(ArrayList<LogRecord> records) {
    return records.stream().map(r -> toProtoLogRecord(r)).collect(Collectors.toList());
  }

  private io.opentelemetry.proto.logs.v1.LogRecord toProtoLogRecord(LogRecord r) {
    //TODO copy attributes between objects.
    return io.opentelemetry.proto.logs.v1.LogRecord.newBuilder().build();
  }

  @Override
  public CompletableResultCode shutdown() {
    return new CompletableResultCode().succeed();
  }

  public synchronized ArrayList<LogRecord> getRecords() {
    return records;
  }

  public synchronized void setOnCall(@Nullable Runnable onCall) {
    this.onCall = onCall;
  }

  public synchronized int getCallCount() {
    return callCount;
  }
}
