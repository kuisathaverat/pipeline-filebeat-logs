/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
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
