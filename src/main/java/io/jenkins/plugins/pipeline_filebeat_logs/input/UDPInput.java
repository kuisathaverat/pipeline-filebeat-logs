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
package io.jenkins.plugins.pipeline_filebeat_logs.input;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Implementation for the UDP input of filebeat.
 */
public class UDPInput implements Input {
  private final int port;
  @NonNull
  private final String host;

  public UDPInput(@NonNull String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    try (DatagramSocket socket = new DatagramSocket()) {
      byte[] bytes = value.getBytes("UTF-8");
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(host), port);
      socket.send(packet);
    }
    return true;
  }
}
