/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jenkins.plugins.elasticstacklogs.input.OpentelemetryLogsInput;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class OpentelemetryLogsInputTest {
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInputTest.class.getName());

  private static final File workdir = new File("/tmp");
  private Server server;

  private class LogsService extends LogsServiceGrpc.LogsServiceImplBase {

    @Override
    public void export(ExportLogsServiceRequest request, StreamObserver<ExportLogsServiceResponse> responseObserver) {
      LOGGER.info("[Server] Log received :" + request.toString());
      responseObserver.onNext(ExportLogsServiceResponse.newBuilder().build());
      responseObserver.onCompleted();
    }
  }

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws IOException {
    server = ServerBuilder.forPort(4317).addService(new LogsService())
                          .build();
    server.start();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOGGER.info("[Server] shutting down gRPC server since JVM is shutting down");
        try {
          if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
          }
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        LOGGER.info("[Server] server shut down");
      }
    });
  }

  @Test
  public void testLog() throws IOException, InterruptedException {
    //OpentelemetryLogsInput input = new OpentelemetryLogsInput("127.0.0.1", otelCollector.getMappedPort(4317));
    OpentelemetryLogsInput input = new OpentelemetryLogsInput("grpc://127.0.0.1:4317");
    input.write("foo");
    Thread.sleep(5000);
    assertEquals(1, input.getCount());
    input.write("foo");
    input.write("foo");
    input.write("foo");
    input.write("foo");
    Thread.sleep(5000);
    assertEquals(2, input.getCount());
    for(int i=0; i<20; i++){
      input.write("foo");
    }
    Thread.sleep(5000);
    assertEquals(4, input.getCount());
  }

}
