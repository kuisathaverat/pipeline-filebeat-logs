/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.util.FormValidation;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * tests related to form validations that needs a Jenkins instance.
 */
public class ElasticStackConfigurationTest {

  public static final String CRED_ID = "credID";
  @Rule
  public JenkinsRule r = new JenkinsRule();
  private ElasticStackConfiguration configuration;

  @Before
  public void setUp() throws Exception {
    configuration = ElasticStackConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(
      new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CRED_ID, "", "elastic", "changeme"));
  }

  @Test
  public void testDoFillCredentialsIdItems() {
    assertFalse(configuration.doFillCredentialsIdItems(null, CRED_ID).isEmpty());
  }

  @Test
  public void testDoCheckCredentialsId() {
    assertEquals(configuration.doCheckCredentialsId(null, CRED_ID).kind, FormValidation.Kind.OK);
    assertEquals(configuration.doCheckCredentialsId(null, "foo").kind, FormValidation.Kind.WARNING);
  }
}
