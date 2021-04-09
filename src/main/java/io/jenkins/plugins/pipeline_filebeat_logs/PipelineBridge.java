package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Queue;
import hudson.model.Run;
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
