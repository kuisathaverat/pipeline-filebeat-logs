/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import hudson.model.BuildListener;
import jenkins.util.JenkinsJVM;

/**
 * Initialized a {@link OutputStream} to send the events that happen during a build.
 */
public class Sender implements BuildListener, Closeable {

  private static final Logger LOGGER = Logger.getLogger(Sender.class.getName());

  private static final long serialVersionUID = 1;

  /**
   * for example {@code 7}
   */
  @CheckForNull
  private final String nodeId;
  @NonNull
  private final BuildInfo buildInfo;

  private transient @CheckForNull
  PrintStream logger;

  public Sender(@NonNull BuildInfo buildInfo, @CheckForNull String nodeId) {
    this.buildInfo = buildInfo;
    this.nodeId = nodeId;
  }

  @NonNull
  @Override
  public PrintStream getLogger() {
    if (logger == null) {
      try {
        logger = new PrintStream(new OutputStream(buildInfo, nodeId), false, "UTF-8");
      } catch (URISyntaxException | IOException x) {
        throw new AssertionError(x);
      }
    }
    return logger;
  }

  @Override
  public void close() throws IOException {
    if (logger != null) {
      LOGGER.log(Level.FINE, "closing {0}#{2}", new Object[] { buildInfo.toString(), nodeId });
      logger = null;
    }
    if (nodeId != null && JenkinsJVM.isJenkinsJVM()) {
      // Note that this does not necessarily shut down the AWSLogs client; that is shared across builds.
      PipelineBridge.get().close(buildInfo.getKey());
    }
  }

}
