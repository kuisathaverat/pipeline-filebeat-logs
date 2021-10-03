/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.FormValidation;
import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.DockerClientFactory;

import java.io.IOException;

import static io.jenkins.plugins.elasticstacklogs.ElasticsearchContainer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * test the validation of the settings against a Elasticsearch instance.
 */
public class InputConfigurationITTest {

  public static final String CRED_ID = "credID";
  @Rule
  public JenkinsRule r = new JenkinsRule();
  @Rule
  public ElasticsearchContainer esContainer = new ElasticsearchContainer();
  private ElasticStackConfiguration elasticStackConfiguration;
  private InputConfiguration inputConfiguration;

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws Exception {
    elasticStackConfiguration = ElasticStackConfiguration.get();
    inputConfiguration = InputConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CRED_ID, "", USER_NAME, PASSWORD));
    elasticStackConfiguration.setCredentialsId(CRED_ID);
    elasticStackConfiguration.setElasticsearchUrl(esContainer.getUrl());
    esContainer.createFilebeatIndex();
  }

  @Test
  public void testDoValidate() throws IOException {
    assertEquals(inputConfiguration.doValidate(CRED_ID, esContainer.getUrl(), INDEX_PATTERN).kind, FormValidation.Kind.OK);

    assertEquals(inputConfiguration.doValidate(CRED_ID, esContainer.getUrl(), "pattern").kind, FormValidation.Kind.ERROR);
    assertEquals(inputConfiguration.doValidate(CRED_ID, esContainer.getUrl(), "").kind, FormValidation.Kind.ERROR);

    assertEquals(inputConfiguration.doValidate(CRED_ID, "", "pattern").kind, FormValidation.Kind.ERROR);
  }
}
