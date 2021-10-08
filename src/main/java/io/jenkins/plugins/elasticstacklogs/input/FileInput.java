/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import java.nio.file.Paths;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Implementation for the log input of filebeat.
 */
public class FileInput extends Input {

  @NonNull
  private final String filePath;

  @DataBoundConstructor
  public FileInput(@NonNull String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    FileUtils.writeStringToFile(Paths.get(filePath).toFile(), value, "UTF-8", true);
    return true;
  }
}
