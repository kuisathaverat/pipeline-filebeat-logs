/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.IOException;
import javax.annotation.CheckForNull;
import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import org.apache.commons.lang.StringUtils;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class ElasticsearchLogsLinkAction implements RunAction2 {

  private final BuildInfo buildInfo;
  private transient Run<?, ?> run;

  public ElasticsearchLogsLinkAction(BuildInfo buildInfo) {
    this.buildInfo = buildInfo;
  }

  public String linkLogs() throws IOException {
    return buildLogsURL("");
  }

  public String linkDiscover() throws IOException {
    return buildDiscoverURL("");
  }

  private String buildLogsURL(@CheckForNull String nodeId) throws IOException {
    String kibanaUrl = ElasticStackConfiguration.get().getKibanaUrl();
    return kibanaUrl + "/app/logs/stream?" + "flyoutOptions=(" + "flyoutId:!n," + "flyoutVisibility:hidden,"
           + "surroundingLogsId:!n)" + "&logPosition=(" + "end:now," + "start:%27" + buildInfo.getStartTime() + "%27,"
           + "streamLive:!f)" + "&logFilter=(" + "expression:" + buildQuery(nodeId) + ",kind:kuery)&f=1";
  }

  private String buildDiscoverURL(@CheckForNull String nodeId) throws IOException {
    String kibanaUrl = ElasticStackConfiguration.get().getKibanaUrl();
    return kibanaUrl + "/app/discover#/?" + "_g=(" + "time:(from:%27" + buildInfo.getStartTime() + "%27,to:now)" + ")"
           + "&_a=(" + "index:%27" + InputConfiguration.get().getIndexPattern() + "%27," + "query:(" + "language:kuery,"
           + "query:" + buildQuery(nodeId) + ")" + ")&f=1";
  }

  private String buildQuery(@CheckForNull String nodeId) throws IOException {
    String nodeQuery = "";
    if (StringUtils.isNotBlank(nodeId)) {
      nodeQuery = "%20AND%20job.node:%27" + nodeId + "%27";
    }
    return "%27job.id:" + buildInfo.getKey() + nodeQuery + "%27";
  }

  @Override
  public String getIconFileName() {
    return "/plugin/elastic-stack-logs/images/elastic_stack.png";
  }

  @Override
  public String getDisplayName() {
    return "Elasticsearch logs";
  }

  @Override
  public String getUrlName() {
    return "elasticsearchlogs";
  }

  @Override
  public void onAttached(Run<?, ?> r) {
    this.run = r;
  }

  @Override
  public void onLoad(Run<?, ?> r) {
    this.run = r;
  }

  public Run<?, ?> getRun() {
    return run;
  }
}
