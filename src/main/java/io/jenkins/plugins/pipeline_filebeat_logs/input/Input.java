package io.jenkins.plugins.pipeline_filebeat_logs.input;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;

/**
 * Interface for access to Filebeat inputs.
 */
public interface Input {

  /**
   * Writes a string in the Filebeat input.
   *
   * @param value value to write in the Filebeat input.
   * @return true it success.
   * @throws IOException
   */
  boolean write(@NonNull String value) throws IOException;
}
