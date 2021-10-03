/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.net.URI;

/**
 * Factory to create Filebeat input from an URI.
 */
public class InputFactory {

  /**
   * @param uri URI to access to the Filebeat input.
   * @return returns the correct implementation of the Filebeat input based on the URI.
   */
  public static final Input createInput(URI uri) {
    Input ret = null;
    switch (uri.getScheme()) {
      case "tcp":
        ret = createTCPInput(uri);
        break;
      case "udp":
        ret = createUDPInput(uri);
        break;
      case "file":
        ret = createFileInput(uri);
        break;
      case "otel":
        ret = createOpentelemetryInput(uri);
        break;
    }
    return ret;
  }

  private static Input createOpentelemetryInput(URI uri) {
    return new OpentelemetryLogsInput(uri.getHost(), uri.getPort());
  }

  public static final Input createTCPInput(URI uri) {
    return new TCPInput(uri.getHost(), uri.getPort());
  }

  public static final Input createUDPInput(URI uri) {
    return new UDPInput(uri.getHost(), uri.getPort());
  }

  public static final Input createFileInput(URI uri) {
    return new FileInput(uri.getSchemeSpecificPart().replaceAll("//", "/"));
  }
}
