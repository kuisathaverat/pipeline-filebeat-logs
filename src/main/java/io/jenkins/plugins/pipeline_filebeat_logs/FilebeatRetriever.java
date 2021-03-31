package io.jenkins.plugins.pipeline_filebeat_logs;

import hudson.console.AnnotatedLargeText;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.kohsuke.stapler.framework.io.ByteBuffer;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class FilebeatRetriever {

    private static final Logger LOGGER = Logger.getLogger(FilebeatRetriever.class.getName());

    public FilebeatRetriever(String logStreamNameBase, String buildId) {

    }

    AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(FlowExecutionOwner.Executable build, boolean completed) throws IOException, InterruptedException {
        ByteBuffer buf = new ByteBuffer();
        stream(buf, null, null);
        return new AnnotatedLargeText<>(buf, StandardCharsets.UTF_8, completed, null);
    }

    AnnotatedLargeText<FlowNode> stepLog(FlowNode node, boolean completed) throws IOException {
        ByteBuffer buf = new ByteBuffer();
        stream(buf, node.getId(), null);
        return new AnnotatedLargeText<>(buf, StandardCharsets.UTF_8, completed, node);
    }

    /**
     * Gather the log text for one node or the entire build.
     * @param os where to send output
     * @param nodeId if defined, limit output to that coming from this node
     * @param idsByLine if defined, add a node ID or null per line printed
     */
    private void stream(OutputStream os, @CheckForNull String nodeId, @CheckForNull List<String> idsByLine) throws IOException {
        //TODO implement it.
        os.write("Empty".getBytes("UTF-8"));
        os.flush();
    }

}
