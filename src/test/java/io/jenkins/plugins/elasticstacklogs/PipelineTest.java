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

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
import org.elasticsearch.action.search.SearchResponse;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static org.junit.Assume.assumeTrue;

public class PipelineTest {

  public static final String CRED_ID = "credID";
  @ClassRule
  public static DockerComposeContainer environment =
    new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"));
  @Rule
  public JenkinsRule r = new JenkinsRule();
  private ElasticStackConfiguration elasticStackConfiguration;
  private InputConfiguration inputConfiguration;
  private Retriever retriever;

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws Exception {
    elasticStackConfiguration = ElasticStackConfiguration.get();
    inputConfiguration = InputConfiguration.get();
    SystemCredentialsProvider.getInstance()
      .getCredentials()
      .add(new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        CRED_ID, "",
        ElasticsearchContainer.USER_NAME, ElasticsearchContainer.PASSWORD));
    inputConfiguration.setInput("tcp://localhost:9000");
    elasticStackConfiguration.setElasticsearchUrl("http://localhost:9200");
    elasticStackConfiguration.setKibanaUrl("http://localhost:5601");
    elasticStackConfiguration.setCredentialsId(CRED_ID);
    inputConfiguration.setIndexPattern(ElasticsearchContainer.INDEX_PATTERN);

    retriever = new Retriever(
      ElasticStackConfiguration.get().getElasticsearchUrl(),
      ElasticsearchContainer.USER_NAME,
      ElasticsearchContainer.PASSWORD,
      InputConfiguration.get().getIndexPattern()
    );
  }

  @Test
  public void test() throws Exception {
    r.createSlave("remote", null, null);
    WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
    p.setDefinition(new CpsFlowDefinition(
      "node('remote') {\n" +
        "  echo 'Hello'\n" +
        "}", true));
    WorkflowRun run = r.buildAndAssertSuccess(p);
    waitForLogs(run);
    r.assertLogContains("Hello", run);
  }

  private void waitForLogs(WorkflowRun run) {
    BuildInfo buildInfo = new BuildInfo(run);
    long counter = 0;
    do {
      try {
        SearchResponse searchResponse = retriever.search(buildInfo.getKey());
        counter = searchResponse.getHits().getTotalHits().value;
      } catch (Throwable e) {
        //NOOP
      }
    } while (counter < 5);
  }
}
