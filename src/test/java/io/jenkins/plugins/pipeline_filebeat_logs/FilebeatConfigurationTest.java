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
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * tests related to form validations that needs a Jenkins instance.
 */
public class FilebeatConfigurationTest {

  @Rule
  public JenkinsRule r = new JenkinsRule();
  private String credentialsId = "credID";
  private FilebeatConfiguration configuration;

  @Before
  public void setUp() throws Exception {
    configuration = FilebeatConfiguration.get();
    SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsId, "", "elastic", "changeme"));
  }

  @Test
  public void testDoFillCredentialsIdItems() {
    assertFalse(configuration.doFillCredentialsIdItems(null, credentialsId).isEmpty());
  }

  @Test
  public void testDoCheckCredentialsId() {
    assertEquals(configuration.doCheckCredentialsId(null, credentialsId).kind, FormValidation.Kind.OK);
    assertEquals(configuration.doCheckCredentialsId(null, "foo").kind, FormValidation.Kind.WARNING);
  }

  @Test
  public void testDoValidate() {
    assertEquals(configuration.doValidate("url", credentialsId, "pattern").kind, FormValidation.Kind.ERROR);
    assertEquals(configuration.doValidate("", credentialsId, "pattern").kind, FormValidation.Kind.ERROR);
    assertEquals(configuration.doValidate("url", credentialsId, "").kind, FormValidation.Kind.ERROR);
  }
}
