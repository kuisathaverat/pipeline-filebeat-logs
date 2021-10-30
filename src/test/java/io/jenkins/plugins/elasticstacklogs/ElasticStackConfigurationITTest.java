/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.DockerClientFactory;
import hudson.util.FormValidation;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class ElasticStackConfigurationITTest {

  public static final String WRONG_CREDS = "wrongCreds";
  public static final String CRED_ID = "credID";
  @Rule
  public JenkinsRule r = new JenkinsRule();
  @Rule
  public ElasticsearchContainer esContainer = new ElasticsearchContainer();
  private ElasticStackConfiguration elasticStackConfiguration;

  @BeforeClass
  public static void requiresDocker() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable());
  }

  @Before
  public void setUp() throws Exception {
    elasticStackConfiguration = ElasticStackConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(
      new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CRED_ID, "", ElasticsearchContainer.USER_NAME,
                                          ElasticsearchContainer.PASSWORD
      ));
    SystemCredentialsProvider.getInstance().getCredentials().add(
      new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, WRONG_CREDS, "", "foo", "bar"));
    elasticStackConfiguration.setCredentialsId(CRED_ID);
    elasticStackConfiguration.setElasticsearchUrl(esContainer.getUrl());
    esContainer.createFilebeatIndex();
  }

  @Test
  public void testDoValidate() {
    assertEquals(elasticStackConfiguration.doValidate(CRED_ID, esContainer.getUrl()).kind, FormValidation.Kind.OK);

    assertEquals(elasticStackConfiguration.doValidate(WRONG_CREDS, esContainer.getUrl()).kind,
                 FormValidation.Kind.ERROR
                );
    assertEquals(elasticStackConfiguration.doValidate(CRED_ID, "nowhere").kind, FormValidation.Kind.ERROR);
  }
}
