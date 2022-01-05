/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.IOException;
import java.util.logging.Logger;
import io.jenkins.plugins.elasticstacklogs.input.OpentelemetryLogsInput;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class OpentelemetryLogsInputTest {
  private static final Logger LOGGER = Logger.getLogger(OpentelemetryLogsInputTest.class.getName());

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Test
  public void testLog() throws IOException, InterruptedException {
    OpentelemetryLogsInput input = new OpentelemetryLogsInput("test://inMemoryExporter:4317");
    input.write("foo");
    Thread.sleep(5000);
    assertEquals(1, input.getCount());
    input.write("foo");
    input.write("foo");
    input.write("foo");
    input.write("foo");
    Thread.sleep(5000);
    assertEquals(5, input.getCount());
  }
}
