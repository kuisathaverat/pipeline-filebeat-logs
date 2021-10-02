/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import java.util.logging.Logger;

/**
 * Retrieve the logs from Elasticsearch.
 */
public class Retriever {

  private static final Logger LOGGER = Logger.getLogger(Retriever.class.getName());

  @NonNull
  private final BuildInfo buildInfo;

  public Retriever(@NonNull BuildInfo buildInfo) {
    this.buildInfo = buildInfo;
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
    UsernamePasswordCredentials creds = ElasticStackConfiguration.get().getCredentials();
    io.jenkins.plugins.elasticstacklogs.log.Retriever retriever = new io.jenkins.plugins.elasticstacklogs.log.Retriever(
      ElasticStackConfiguration.get().getElasticsearchUrl(),
      creds.getUsername(),
      creds.getPassword().getPlainText(),
      InputConfiguration.get().getIndexPattern()
    );
    try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      String kibanaUrl = ElasticStackConfiguration.get().getKibanaUrl();
      if (StringUtils.isNotBlank(kibanaUrl)) {
        // TODO build a proper Kibana URL with a filter.
        w.write("[view in <a href=\"" + buildLogsURL(nodeId) + "\">Kibana Logs</a>]\n");
        w.write("[view in <a href=\"" + buildDiscoverURL(nodeId) + "\">Kibana Discover</a>]\n");
      }

      SearchResponse searchResponse = retriever.search(buildInfo.getKey(), nodeId);
      String scrollId = searchResponse.getScrollId();
      SearchHit[] searchHits = searchResponse.getHits().getHits();
      writeOutput(w, searchHits);

      while (searchHits != null && searchHits.length > 0) {
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

  private String buildLogsURL(@CheckForNull String nodeId) throws IOException {
    String kibanaUrl = ElasticStackConfiguration.get().getKibanaUrl();
    return kibanaUrl + "/app/logs/stream?" +
      "flyoutOptions=(" +
      "flyoutId:!n," +
      "flyoutVisibility:hidden," +
      "surroundingLogsId:!n)" +
      "&logPosition=(" +
      "end:now," +
      "start:%27" + buildInfo.getStartTime() + "%27," +
      "streamLive:!f)" +
      "&logFilter=(" +
      "expression:" + buildKuery(nodeId) +
      ",kind:kuery)&f=1";
  }

  private String buildDiscoverURL(@CheckForNull String nodeId) throws IOException {
    String kibanaUrl = ElasticStackConfiguration.get().getKibanaUrl();
    return kibanaUrl + "/app/discover#/?" +
           "_g=(" +
           "time:(from:%27" + buildInfo.getStartTime() + "%27,to:now)" +
           ")" +
           "&_a=(" +
           "index:%27" + InputConfiguration.get().getIndexPattern() + "%27," +
           "query:(" +
           "language:kuery," +
           "query:" + buildKuery(nodeId) +
           ")" +
           ")&f=1";
  }

  private String buildKuery(@CheckForNull String nodeId) throws IOException {
    String nodeQuery = "";
    if (StringUtils.isNotBlank(nodeId)) {
      nodeQuery = "%20AND%20job.node:%27" + nodeId + "%27";
    }
    return "%27job.id:" + buildInfo.getKey() + nodeQuery + "%27";
  }

  private void writeOutput(Writer w, SearchHit[] searchHits) throws IOException {
    for (SearchHit line : searchHits) {
      JSONObject json = JSONObject.fromObject(line.getSourceAsMap());
      ConsoleNotes.write(w, json);
    }
  }
}
