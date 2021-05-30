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

package io.jenkins.plugins.elasticstacklogs;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.console.AnnotatedLargeText;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.log.BrokenLogStorage;
import org.jenkinsci.plugins.workflow.log.LogStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Replaces the logs storage implementation with a custom one
 * that uses Filebeat and Elasticsearch to store and retrieve the logs.
 */
public class LogStorageImpl implements LogStorage {
  private static final Logger LOGGER = Logger.getLogger(LogStorageImpl.class.getName());
  @NonNull
  private final BuildInfo buildInfo;

  public LogStorageImpl(@NonNull BuildInfo buildInfo) throws IOException {
    this.buildInfo = buildInfo;
  }

  @NonNull
  @Override
  public BuildListener overallListener() throws IOException, InterruptedException {
    return new Sender(buildInfo, null);
  }

  @NonNull
  @Override
  public TaskListener nodeListener(@NonNull FlowNode flowNode) throws IOException, InterruptedException {
    return new Sender(buildInfo, flowNode.getId());
  }

  @NonNull
  @Override
  public AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(@NonNull FlowExecutionOwner.Executable build, boolean complete) {
    try {
      return new Retriever(buildInfo).overallLog(build, complete);
    } catch (Exception x) {
      return new BrokenLogStorage(x).overallLog(build, complete);
    }
  }

  @NonNull
  @Override
  public AnnotatedLargeText<FlowNode> stepLog(@NonNull FlowNode flowNode, boolean complete) {
    try {
      return new Retriever(buildInfo).stepLog(flowNode, complete);
    } catch (Exception x) {
      return new BrokenLogStorage(x).stepLog(flowNode, complete);
    }
  }

  @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "forBuild only accepts Run")
  @Deprecated
  @Override
  public File getLogFile(FlowExecutionOwner.Executable build, boolean complete) {
    AnnotatedLargeText<FlowExecutionOwner.Executable> logText = overallLog(build, complete);
    // Not creating a temp file since it would be too expensive to have multiples:
    File f = new File(((Run) build).getRootDir(), "log");
    f.deleteOnExit();
    try (OutputStream os = new FileOutputStream(f)) {
      // Similar to Run#writeWholeLogTo but terminates even if !complete:
      long pos = 0;
      while (true) {
        long pos2 = logText.writeRawLogTo(pos, os);
        if (pos2 <= pos) {
          break;
        }
        pos = pos2;
      }
    } catch (Exception x) {
      LOGGER.log(Level.WARNING, null, x);
    }
    return f;
  }

  private InputConfiguration getConfiguration() {
    return ExtensionList.lookupSingleton(InputConfiguration.class);
  }
}
