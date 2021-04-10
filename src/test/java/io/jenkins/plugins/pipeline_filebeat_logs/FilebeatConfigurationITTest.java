
/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.jenkins.plugins.pipeline_filebeat_logs;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.FormValidation;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.time.Duration;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * test the validation of the settings against a Elasticsearch instance.
 */
public class FilebeatConfigurationITTest {

  public static final String INDEX_PATTERN = "filebeat-*";
  public static final String USER_NAME = "elastic";
  public static final String PASSWORD = "changeme";
  @Rule
  public JenkinsRule r = new JenkinsRule();
  private String credentialsId = "credID";
  private FilebeatConfiguration configuration;

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Rule
  public GenericContainer esContainer = new GenericContainer("docker.elastic.co/elasticsearch/elasticsearch:7.12.0")
    .withExposedPorts(9200)
    .withEnv("ES_JAVA_OPTS","-Xms512m -Xmx512m")
    .withEnv("discovery.type","single-node")
    .withEnv("bootstrap.memory_lock","true")
    .withEnv("ELASTIC_PASSWORD",PASSWORD)
    .withEnv("xpack.security.enabled","true")
    .withStartupTimeout(Duration.ofMinutes(3));

  @Before public void setUp() throws Exception {
    configuration =  FilebeatConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsId, "", USER_NAME, PASSWORD));
    configuration.setCredentialsId(credentialsId);
  }

  @Test
  public void testDoValidate() throws IOException {
    String url = "http://" + esContainer.getContainerIpAddress() + ":" + esContainer.getMappedPort(9200);
    createFilebeatIndex(url);
    assertEquals(configuration.doValidate(url, credentialsId, INDEX_PATTERN).kind, FormValidation.Kind.OK);
  }

  private void createFilebeatIndex(String url) throws IOException {
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    org.apache.http.auth.UsernamePasswordCredentials credentials =
      new org.apache.http.auth.UsernamePasswordCredentials(USER_NAME, PASSWORD);
    credentialsProvider.setCredentials(AuthScope.ANY, credentials);

    RestClientBuilder builder = RestClient.builder(HttpHost.create(url));
    builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
      @Override
      public HttpAsyncClientBuilder customizeHttpClient(
        HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder
          .setDefaultCredentialsProvider(credentialsProvider);
      }
    });

    try (RestHighLevelClient client = new RestHighLevelClient(builder)) {
      CreateIndexRequest request = new CreateIndexRequest("filebeat-001");
      client.indices().create(request, RequestOptions.DEFAULT);
    }
  }
}
