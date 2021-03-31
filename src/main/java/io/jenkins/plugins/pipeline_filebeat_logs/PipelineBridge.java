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

@Extension
public class PipelineBridge  implements LogStorageFactory {

    private final Map<String, LogStorageImpl> impls = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(PipelineBridge.class.getName());

    @Nullable
    @Override
    public LogStorage forBuild(@NonNull FlowExecutionOwner owner) {
        final String logStreamNameBase;
        final String buildId;
        try {
            Queue.Executable exec = owner.getExecutable();
            if (exec instanceof Run) {
                Run<?, ?> b = (Run<?, ?>) exec;
                // TODO escape [:*@%] in job names using %XX URL encoding
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

    static PipelineBridge get() {
        return ExtensionList.lookupSingleton(PipelineBridge.class);
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
