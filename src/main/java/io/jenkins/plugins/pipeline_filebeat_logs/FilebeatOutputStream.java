package io.jenkins.plugins.pipeline_filebeat_logs;

import hudson.console.LineTransformationOutputStream;
import net.sf.json.JSONObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilebeatOutputStream extends LineTransformationOutputStream {
    private static final Logger LOGGER = Logger.getLogger(FilebeatOutputStream.class.getName());

    /** for example {@code jenkinsci/git-plugin/master} */
    protected final @Nonnull
    String logStreamNameBase;
    /** for example {@code 123} */
    protected final @Nonnull String buildId;
    /** for example {@code 7} */
    protected final @CheckForNull
    String nodeId;

    public FilebeatOutputStream(@Nonnull String logStreamNameBase, @Nonnull String buildId, @CheckForNull String nodeId) {
        this.logStreamNameBase = logStreamNameBase;
        this.buildId = buildId;
        this.nodeId = nodeId;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        Map<String, Object> data = ConsoleNotes.parse(b, len);
        long now = System.currentTimeMillis();
        data.put("build", buildId);
        data.put("timestamp", now);
        if (nodeId != null) {
            data.put("node", nodeId);
        }
        try {

            if (writeOnFilebeat(JSONObject.fromObject(data).toString())) {
                LOGGER.log(Level.FINER, "scheduled event @{0} from {1}/{2}#{3}", new Object[] {now, logStreamNameBase, buildId, nodeId});
            } else {
                LOGGER.warning("Message buffer full, giving up");
            }
        } catch (Exception x) {
            LOGGER.log(Level.WARNING, "failed to send a message", x);
        }
    }

    private boolean writeOnFilebeat(String toString) {
        LOGGER.log(Level.INFO, "Pipeline line: " + toString);
        //TODO implement it.
        return true;
    }
}
