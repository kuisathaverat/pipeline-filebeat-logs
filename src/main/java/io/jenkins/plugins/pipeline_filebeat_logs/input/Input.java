package io.jenkins.plugins.pipeline_filebeat_logs.input;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;

public interface Input {
  boolean write(@NonNull String value) throws IOException;
}
