/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Implementation for the UDP input of filebeat.
 */
public class UDPInput extends Input {
  private final int port;
  @NonNull
  private final String host;

  @DataBoundConstructor
  public UDPInput(@NonNull String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    try (DatagramSocket socket = new DatagramSocket()) {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(host), port);
      socket.send(packet);
    }
    return true;
  }
}
