/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import io.jenkins.plugins.elasticstacklogs.input.FileInput;
import io.jenkins.plugins.elasticstacklogs.input.TCPInput;
import io.jenkins.plugins.elasticstacklogs.input.UDPInput;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class FilebeatInputTest {

  private static final File workdir = new File("/tmp");

  @Rule
  public GenericContainer filebeatContainer = new GenericContainer("docker.elastic.co/beats/filebeat:7.15.0")
    .withExposedPorts(9000)
    .withClasspathResourceMapping("filebeat.yml", "/usr/share/filebeat/filebeat.yml", BindMode.READ_ONLY)
    .withFileSystemBind(workdir.getAbsolutePath(), "/tmp", BindMode.READ_WRITE).withCommand("--strict.perms=false");

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
    Path filebeatInput = Paths.get(workdir.getAbsolutePath(), "filebeat_input");
    Path filebeatSock = Paths.get(workdir.getAbsolutePath(), "filebeat.sock");
    FileUtils.deleteQuietly(filebeatInput.toFile());
    FileUtils.deleteQuietly(filebeatSock.toFile());
  }

  @Before
  public void setUp() {
  }

  @Test
  public void testLog() throws IOException, InterruptedException {
    FileInput input = new FileInput(workdir.getAbsolutePath() + "/filebeat_input");
    input.write("foo\n");
    Thread.sleep(20000);
    assumeTrue(filebeatContainer.getLogs().contains("\"message\": \"foo\","));
  }

  @Test
  public void testTCP() throws IOException, InterruptedException {
    TCPInput input = new TCPInput(filebeatContainer.getHost(), filebeatContainer.getMappedPort(9000));
    input.write("foo\n");
    Thread.sleep(20000);
    assumeTrue(filebeatContainer.getLogs().contains("\"message\": \"foo\","));
  }

  /*
    TODO test against a Docker container
    https://github.com/testcontainers/testcontainers-java/issues/2532
    https://github.com/testcontainers/testcontainers-java/pull/2989
   */
  @Test
  public void testUDP() throws IOException, InterruptedException {
    DatagramSocket udpSocket = new DatagramSocket(0, InetAddress.getByName("localhost"));
    UDPInput input = new UDPInput("localhost", udpSocket.getLocalPort());
    input.write("foo\n");
    Thread.sleep(10000);
    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
    udpSocket.receive(packet);
    byte[] msgBuffer = packet.getData();
    int length = packet.getLength();
    int offset = packet.getOffset();
    assertEquals(new String(msgBuffer, offset, length), "foo\n");
  }
}
