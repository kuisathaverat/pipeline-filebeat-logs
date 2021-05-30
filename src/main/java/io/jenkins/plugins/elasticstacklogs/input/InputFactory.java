/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
