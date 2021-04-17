/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.BuildListener;
import io.jenkins.plugins.pipeline_filebeat_logs.log.BuildInfo;
import jenkins.util.JenkinsJVM;

import javax.annotation.CheckForNull;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initialized a {@link FilebeatOutputStream} to send the events that happen during a build.
 */
public class FilebeatSender implements BuildListener, Closeable {

  private static final Logger LOGGER = Logger.getLogger(FilebeatSender.class.getName());

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

  public FilebeatSender(@NonNull BuildInfo buildInfo, @CheckForNull String nodeId) {
    this.buildInfo = buildInfo;
    this.nodeId = nodeId;
  }

  @NonNull
  @Override
  public PrintStream getLogger() {
    if (logger == null) {
      try {
        logger = new PrintStream(new FilebeatOutputStream(buildInfo, nodeId), false, "UTF-8");
      } catch (UnsupportedEncodingException | URISyntaxException x) {
        throw new AssertionError(x);
      }
    }
    return logger;
  }

  @Override
  public void close() throws IOException {
    if (logger != null) {
      LOGGER.log(Level.FINE, "closing {0}#{2}", new Object[]{buildInfo.toString(), nodeId});
      logger = null;
    }
    if (nodeId != null && JenkinsJVM.isJenkinsJVM()) {
      // Note that this does not necessarily shut down the AWSLogs client; that is shared across builds.
      PipelineBridge.get().close(buildInfo.getKey());
    }
  }
}
