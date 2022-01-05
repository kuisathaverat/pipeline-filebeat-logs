/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.AnnotatedLargeText;
import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.kohsuke.stapler.framework.io.ByteBuffer;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Retrieve the logs from Elasticsearch.
 */
public class Retriever {

  @NonNull
  private final BuildInfo buildInfo;

  public Retriever(@NonNull BuildInfo buildInfo) {
    this.buildInfo = buildInfo;
  }

  AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(FlowExecutionOwner.Executable build, boolean completed)
    throws IOException {
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
    ElasticStackConfiguration elasticStackConfiguration = ElasticStackConfiguration.get();
    UsernamePasswordCredentials creds = elasticStackConfiguration.getCredentials();
    String elasticsearchUrl = elasticStackConfiguration.getElasticsearchUrl();
    InputConfiguration inputConfiguration = InputConfiguration.get();
    String indexPattern = inputConfiguration.getIndexPattern();
    String username = creds.getUsername();
    String password = creds.getPassword().getPlainText();
    if(StringUtils.isBlank(elasticsearchUrl) || StringUtils.isBlank(indexPattern)){
      throw new IOException("some configuration parameters are incorrect, check the plugin configuration");
    }
    io.jenkins.plugins.elasticstacklogs.log.Retriever retriever = new io.jenkins.plugins.elasticstacklogs.log.Retriever(
      elasticsearchUrl, username, password,
      indexPattern
    );
    if(!retriever.indexExists()){
      try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        w.write("The index pattern configured does not exists\n");
        w.flush();
      }
      return;
    }
    try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      SearchResponse searchResponse = retriever.search(buildInfo.getKey(), nodeId);
      String scrollId = searchResponse.getScrollId();
      SearchHit[] searchHits = searchResponse.getHits().getHits();
      writeOutput(w, searchHits);

      while (searchHits.length > 0) {
        searchResponse = retriever.next(scrollId);
        scrollId = searchResponse.getScrollId();
        searchHits = searchResponse.getHits().getHits();
        writeOutput(w, searchHits);
      }

      if (searchResponse.getHits().getTotalHits().value != 0) {
        retriever.clear(scrollId);
      }
      w.flush();
    }
  }

  private void writeOutput(Writer w, SearchHit[] searchHits) throws IOException {
    for (SearchHit line : searchHits) {
      JSONObject json = JSONObject.fromObject(line.getSourceAsMap());
      ConsoleNotes.write(w, json);
    }
  }
}
