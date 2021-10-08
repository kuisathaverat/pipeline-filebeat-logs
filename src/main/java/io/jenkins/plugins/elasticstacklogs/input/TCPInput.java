/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import java.net.Socket;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Implementation for the TCP input of filebeat.
 */
public class TCPInput extends Input {
  private final int port;
  @NonNull
  private final String host;

  @DataBoundConstructor
  public TCPInput(@NonNull String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    try (Socket socket = new Socket(host, port)) {
      IOUtils.write(value, socket.getOutputStream(), "UTF-8");
    }
    return true;
  }
}
