/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Implementation for the log input of filebeat.
 */
public class FileInput implements Input {

  @NonNull
  private final String filePath;

  public FileInput(@NonNull String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean write(@NonNull String value) throws IOException {
    FileUtils.writeStringToFile(Paths.get(filePath).toFile(), value, "UTF-8", true);
    return true;
  }
}
