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

import hudson.util.FormValidation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * tests related to form validations that not need a Jenkins instance.
 */
public class FilebeatConfigurationFormTest {

  @Test
  public void testDoCheckKibanaUrl() {
    FilebeatConfiguration config = new FilebeatConfiguration();
    assertEquals(config.doCheckKibanaUrl("http://example.com:100").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckKibanaUrl("").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckKibanaUrl("foo").kind, FormValidation.Kind.ERROR);
  }

  @Test
  public void testDoCheckInput() {
    FilebeatConfiguration config = new FilebeatConfiguration();
    assertEquals(config.doCheckInput("http://example.com:1000").kind, FormValidation.Kind.ERROR);
    assertEquals(config.doCheckInput("").kind, FormValidation.Kind.WARNING);
    assertEquals(config.doCheckInput("file://dir/dir/file").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("udp:192.168.1.100:1000").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("tcp:192.168.1.100:100").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("unix://dir/dir/sock").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("foo").kind, FormValidation.Kind.ERROR);
    assertEquals(config.doCheckInput("foo://bar").kind, FormValidation.Kind.ERROR);
  }

  @Test
  public void testDoCheckElasticsearchUrl() {
    FilebeatConfiguration config = new FilebeatConfiguration();
    assertEquals(config.doCheckElasticsearchUrl("http://example.com:1000").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckElasticsearchUrl("").kind, FormValidation.Kind.WARNING);
    assertEquals(config.doCheckElasticsearchUrl("foo").kind, FormValidation.Kind.ERROR);
  }

  @Test
  public void testDoCheckIndexPattern() {
    FilebeatConfiguration config = new FilebeatConfiguration();
    assertEquals(config.doCheckIndexPattern("foo").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckIndexPattern("").kind, FormValidation.Kind.WARNING);
  }
}
