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

package io.jenkins.plugins.pipeline_filebeat_logs;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.DockerClientFactory;

import java.io.IOException;

import static io.jenkins.plugins.pipeline_filebeat_logs.ElasticsearchContainer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * test the validation of the settings against a Elasticsearch instance.
 */
public class FilebeatConfigurationITTest {

  @Rule
  public JenkinsRule r = new JenkinsRule();
  @Rule
  public ElasticsearchContainer esContainer = new ElasticsearchContainer();
  private String credentialsId = "credID";
  private FilebeatConfiguration configuration;

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws Exception {
    configuration = FilebeatConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsId, "", USER_NAME, PASSWORD));
    configuration.setCredentialsId(credentialsId);
    esContainer.createFilebeatIndex();
  }

  @Test
  public void testDoValidate() throws IOException {
    assertEquals(configuration.doValidate(esContainer.getUrl(), credentialsId, INDEX_PATTERN).kind, FormValidation.Kind.OK);
  }
}
