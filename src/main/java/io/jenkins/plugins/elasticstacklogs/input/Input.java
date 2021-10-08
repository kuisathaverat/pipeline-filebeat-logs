/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.input;

import java.io.IOException;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Interface for implement log inputs.
 */
public abstract class Input {

  /**
   * Writes a string in the Filebeat input.
   *
   * @param value value to write in the Filebeat input.
   * @return true it success.
   * @throws IOException
   */
  public abstract boolean write(@NonNull String value) throws IOException;

}
