package io.jenkins.plugins.pipeline_filebeat_logs;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.AnnotatedLargeText;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.kohsuke.stapler.framework.io.ByteBuffer;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Retrieve the logs from Elasticsearch.
 */
public class FilebeatRetriever {

  private static final Logger LOGGER = Logger.getLogger(FilebeatRetriever.class.getName());

  public FilebeatRetriever(String logStreamNameBase, String buildId) {

  }

  AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(FlowExecutionOwner.Executable build, boolean completed) throws IOException, InterruptedException {
    ByteBuffer buf = new ByteBuffer();
    stream(buf, null);
    return new AnnotatedLargeText<>(buf, StandardCharsets.UTF_8, completed, build);
  }

  AnnotatedLargeText<FlowNode> stepLog(FlowNode node, boolean completed) throws IOException {
    ByteBuffer buf = new ByteBuffer();
    stream(buf, node.getId());
    return new AnnotatedLargeText<>(buf, StandardCharsets.UTF_8, completed, node);
  }

  /**
   * Gather the log text for one node or the entire build.
   *
   * @param os     where to send output
   * @param nodeId if defined, limit output to that coming from this node
   */
  private void stream(@NonNull OutputStream os, @CheckForNull String nodeId) throws IOException {
    //TODO implement it.
    String url = "https://kibana.example.com";
    try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      w.write("view in " + url + "\n");
      w.write("Empty\n");
      w.flush();
    }
  }

}
