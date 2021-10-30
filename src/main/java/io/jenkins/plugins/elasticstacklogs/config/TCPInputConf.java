/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import io.jenkins.plugins.elasticstacklogs.input.TCPInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;

/**
 * Configuration for the TCP input of filebeat.
 */
public class TCPInputConf extends InputConf {
  static final long serialVersionUID = 1L;
  private int port;
  @NonNull
  private String host;

  @DataBoundConstructor
  public TCPInputConf(@NonNull String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public Input get() throws IOException {
    return new TCPInput(host, port);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TCPInputConf that = (TCPInputConf) o;

    return new EqualsBuilder().append(port, that.port).append(host, that.host).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(port).append(host).toHashCode();
  }

  @Override
  public String toString() {
    return "TCPInputConf{" + "port=" + port + ", host='" + host + '\'' + '}';
  }

  @Symbol("tcpInput")
  @Extension
  public static class TCPInputDescriptor extends InputConfDescriptor {
    @Override
    public String getDisplayName() {
      return "Filebeat TCP type input";
    }
  }
}
