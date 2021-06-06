package io.jenkins.plugins.elasticstacklogs;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import io.jenkins.plugins.elasticstacklogs.input.OpentelemetryLogsInput;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class OpentelemetryLogsInputTest {
  private static final File workdir = new File("/tmp");

  @Rule
  public GenericContainer otelCollector = new GenericContainer("otel/opentelemetry-collector:latest")
    .withClasspathResourceMapping("otel-collector.yml", "/otel-collector.yml", BindMode.READ_ONLY)
    .withFileSystemBind(workdir.getAbsolutePath(), "/tmp", BindMode.READ_WRITE)
    .withCommand("--config /otel-collector.yml")
    .withStartupTimeout(Duration.ofMinutes(1));

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() {
  }

  @Test
  public void testLog() throws IOException, InterruptedException {
    OpentelemetryLogsInput input = new OpentelemetryLogsInput("127.0.0.1", otelCollector.getMappedPort(4317));
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
