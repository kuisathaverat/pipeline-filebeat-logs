/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import io.jenkins.plugins.elasticstacklogs.input.OpentelemetryLogsInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;

/**
 * Configuration for the OpenTelemetry log input.
 */
public class OpentelemetryLogsInputConf extends InputConf {
  static final long serialVersionUID = 1L;
  @NonNull
  private String endpoint;

  @DataBoundConstructor
  public OpentelemetryLogsInputConf(@NonNull String endpoint) {
    this.endpoint = endpoint;
  }

  @NonNull
  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(@NonNull String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public Input get() throws IOException {
    return new OpentelemetryLogsInput(endpoint);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OpentelemetryLogsInputConf that = (OpentelemetryLogsInputConf) o;

    return new EqualsBuilder().append(endpoint, that.endpoint).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(endpoint).toHashCode();
  }

  @Override
  public String toString() {
    return "OpentelemetryLogsInputConf{" + "endpoint='" + endpoint + '\'' + '}';
  }

  @Symbol("opeTelemetryInput")
  @Extension
  public static class OpentelemetryLogsInputDescriptor extends InputConfDescriptor {
    @Override
    public String getDisplayName() {
      return "OpenTelemetry input";
    }
  }
}
