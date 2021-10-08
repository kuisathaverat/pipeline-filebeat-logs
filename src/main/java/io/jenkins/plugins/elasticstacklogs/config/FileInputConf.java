/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.input.FileInput;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;

/**
 * Configuration for the log input of filebeat.
 */
public class FileInputConf extends InputConf {
  static final long serialVersionUID = 1L;

  @NonNull
  private String filePath;

  @DataBoundConstructor
  public FileInputConf(@NonNull String filePath) {
    this.filePath = filePath;
  }

  @Override
  public Input get() throws IOException {
    return new FileInput(filePath);
  }

  @NonNull
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(@NonNull String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FileInputConf that = (FileInputConf) o;

    return new EqualsBuilder().append(filePath, that.filePath).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(filePath).toHashCode();
  }

  @Override
  public String toString() {
    return "FileInputConf{" + "filePath='" + filePath + '\'' + '}';
  }

  @Symbol("fileInput")
  @Extension
  public static class FileInputDescriptor extends InputConfDescriptor {
    @Override
    public String getDisplayName() {
      return "Filebeat file type input";
    }
  }
}
