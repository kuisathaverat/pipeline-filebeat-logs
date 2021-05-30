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
import hudson.util.FormValidation;
import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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
    SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CRED_ID, "", "elastic", "changeme"));
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
