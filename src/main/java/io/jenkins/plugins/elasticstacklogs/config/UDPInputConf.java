/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import io.jenkins.plugins.elasticstacklogs.input.UDPInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;

/**
 * Configuration for the UDP input of filebeat.
 */
public class UDPInputConf extends InputConf {
  static final long serialVersionUID = 1L;
  private int port;
  @NonNull
  private String host;

  @DataBoundConstructor
  public UDPInputConf(@NonNull String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public Input get() throws IOException {
    return new UDPInput(host, port);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    UDPInputConf that = (UDPInputConf) o;

    return new EqualsBuilder().append(port, that.port).append(host, that.host).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(port).append(host).toHashCode();
  }

  @Override
  public String toString() {
    return "UDPInputConf{" + "port=" + port + ", host='" + host + '\'' + '}';
  }

  @Symbol("udpInput")
  @Extension
  public static class TCPInputDescriptor extends InputConfDescriptor {
    @Override
    public String getDisplayName() {
      return "Filebeat UDP type input";
    }
  }
}
