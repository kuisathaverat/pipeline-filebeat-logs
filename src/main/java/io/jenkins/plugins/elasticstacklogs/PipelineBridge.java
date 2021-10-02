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
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Queue;
import hudson.model.Run;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.log.BrokenLogStorage;
import org.jenkinsci.plugins.workflow.log.LogStorage;
import org.jenkinsci.plugins.workflow.log.LogStorageFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles the logs storage for the pipelines,
 * for each new pipeline it creates a new {@link LogStorageImpl} class.
 */
@Extension
public class PipelineBridge implements LogStorageFactory {

  static {
    // Make sure JENKINS-52165 is enabled, or performance will be awful for remote shell steps.
    System.setProperty("org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep.USE_WATCHING", "true");
  }

  private static final Logger LOGGER = Logger.getLogger(PipelineBridge.class.getName());
  private final Map<String, LogStorageImpl> impls = new ConcurrentHashMap<>();

  static PipelineBridge get() {
    return ExtensionList.lookupSingleton(PipelineBridge.class);
  }

  @Nullable
  @Override
  public LogStorage forBuild(@NonNull FlowExecutionOwner owner) {
    if (StringUtils.isBlank(InputConfiguration.get().getInput())) {
      LOGGER.warning("There is no Filebeat input configured (Configure System/Filebeat settings).");
      return null;
    }
    try {
      Queue.Executable exec = owner.getExecutable();
      if (exec instanceof Run) {
        return getLogStorageForId(new BuildInfo((Run<?, ?>) exec));
      } else {
        return null;
      }
    } catch (IOException x) {
      return new BrokenLogStorage(x);
    }
  }

  void close(String logsKey) {
    impls.remove(logsKey);
  }

  LogStorage getLogStorageForId(BuildInfo buildInfo) throws IOException {
    return impls.computeIfAbsent(buildInfo.getKey(), k -> {
      try {
        return new LogStorageImpl(buildInfo);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    });
  }
}
