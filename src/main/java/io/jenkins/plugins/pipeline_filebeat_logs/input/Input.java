package io.jenkins.plugins.pipeline_filebeat_logs.input;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;

public interface Input {
  void write(@NonNull String value) throws IOException;
}
