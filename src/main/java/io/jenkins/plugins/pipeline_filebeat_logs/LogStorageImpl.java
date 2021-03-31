package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.console.AnnotatedLargeText;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
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

public class LogStorageImpl implements LogStorage {
    private final String logStreamNameBase;
    private final String buildId;
    private static final Logger LOGGER = Logger.getLogger(LogStorageImpl.class.getName());

    public LogStorageImpl(String logStreamName, String buildId) {
        this.logStreamNameBase = logStreamName;
        this.buildId = buildId;
    }

    @NonNull
    @Override
    public BuildListener overallListener() throws IOException, InterruptedException {
        return new FilebeatSender(logStreamNameBase, buildId, null);
    }

    @NonNull
    @Override
    public TaskListener nodeListener(@NonNull FlowNode flowNode) throws IOException, InterruptedException {
        return new FilebeatSender(logStreamNameBase, buildId, flowNode.getId());
    }

    @NonNull
    @Override
    public AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(@NonNull FlowExecutionOwner.Executable build, boolean complete) {
        try {
            return new FilebeatRetriever(logStreamNameBase, buildId).overallLog(build, complete);
        } catch (Exception x) {
            return new BrokenLogStorage(x).overallLog(build, complete);
        }
    }

    @NonNull
    @Override
    public AnnotatedLargeText<FlowNode> stepLog(@NonNull FlowNode flowNode, boolean complete) {
        try {
            return new FilebeatRetriever(logStreamNameBase, buildId).stepLog(flowNode, complete);
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

    private FilebeatConfiguration getConfiguration(){
        return ExtensionList.lookupSingleton(FilebeatConfiguration.class);
    }
}
