/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Queue;
import hudson.model.Run;
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

  private static final Logger LOGGER = Logger.getLogger(PipelineBridge.class.getName());
  private final Map<String, LogStorageImpl> impls = new ConcurrentHashMap<>();

  static PipelineBridge get() {
    return ExtensionList.lookupSingleton(PipelineBridge.class);
  }

  @Nullable
  @Override
  public LogStorage forBuild(@NonNull FlowExecutionOwner owner) {
    final String logStreamNameBase;
    final String buildId;
    if(StringUtils.isBlank(FilebeatConfiguration.get().getInput())){
      LOGGER.warning("There is no Filebeat input configured (Configure System/Filebeat settings).");
      return null;
    }
    try {
      Queue.Executable exec = owner.getExecutable();
      if (exec instanceof Run) {
        Run<?, ?> b = (Run<?, ?>) exec;
        // TODO add build and job info to the stream
        logStreamNameBase = b.getParent().getFullName();
        buildId = b.getId();
      } else {
        return null;
      }
    } catch (IOException x) {
      return new BrokenLogStorage(x);
    }
    return forIDs(logStreamNameBase, buildId);
  }

  void close(String logStreamNameBase, String buildId) {
    impls.remove(getKey(logStreamNameBase, buildId));
  }

  private String getKey(String logStreamNameBase, String buildId) {
    return logStreamNameBase + "#" + buildId;
  }

  LogStorage forIDs(String logStreamNameBase, String buildId) {
    return impls.computeIfAbsent(getKey(logStreamNameBase, buildId), k -> new LogStorageImpl(logStreamNameBase, buildId));
  }
}
