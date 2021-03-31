package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.BuildListener;
import jenkins.util.JenkinsJVM;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilebeatSender implements BuildListener, Closeable {

    private static final Logger LOGGER = Logger.getLogger(FilebeatSender.class.getName());

    private static final long serialVersionUID = 1;

    /** for example {@code jenkinsci/git-plugin/master} */
    protected final @Nonnull String logStreamNameBase;
    /** for example {@code 123} */
    protected final @Nonnull String buildId;
    /** for example {@code 7} */
    protected final @CheckForNull
    String nodeId;
    private transient @CheckForNull PrintStream logger;

    public FilebeatSender(@Nonnull String logStreamNameBase, @Nonnull String buildId, @CheckForNull String nodeId) {
        this.logStreamNameBase = logStreamNameBase;
        this.buildId = buildId;
        this.nodeId = nodeId;
    }

    @NonNull
    @Override
    public PrintStream getLogger() {
        if (logger == null) {
            try {
                logger = new PrintStream(new FilebeatOutputStream(logStreamNameBase, buildId, nodeId), false, "UTF-8");
            } catch (UnsupportedEncodingException x) {
                throw new AssertionError(x);
            }
        }
        return logger;
    }

    @Override
    public void close() throws IOException {
        if (logger != null) {
            LOGGER.log(Level.FINE, "closing {0}/{1}#{2}", new Object[] {logStreamNameBase, buildId, nodeId});
            logger = null;
        }
        if (nodeId != null && JenkinsJVM.isJenkinsJVM()) {
            // Note that this does not necessarily shut down the AWSLogs client; that is shared across builds.
            PipelineBridge.get().close(logStreamNameBase, buildId);
        }
    }
}
