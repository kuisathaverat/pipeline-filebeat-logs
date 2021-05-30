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

import io.jenkins.plugins.elasticstacklogs.config.FilebeatConfiguration;
import org.junit.Test;
import hudson.util.FormValidation;
import static org.junit.Assert.assertEquals;

/**
 * tests related to form validations that not need a Jenkins instance.
 */
public class FilebeatConfigurationFormTest {

  @Test
  public void testDoCheckInput() {
    FilebeatConfiguration config = new FilebeatConfiguration(true);
    assertEquals(config.doCheckInput("http://example.com:1000").kind, FormValidation.Kind.ERROR);
    assertEquals(config.doCheckInput("").kind, FormValidation.Kind.WARNING);
    assertEquals(config.doCheckInput("file://dir/dir/file").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("udp:192.168.1.100:1000").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("tcp:192.168.1.100:100").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckInput("foo").kind, FormValidation.Kind.ERROR);
    assertEquals(config.doCheckInput("foo://bar").kind, FormValidation.Kind.ERROR);
  }

  @Test
  public void testDoCheckIndexPattern() {
    FilebeatConfiguration config = new FilebeatConfiguration(true);
    assertEquals(config.doCheckIndexPattern("foo").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckIndexPattern("").kind, FormValidation.Kind.WARNING);
  }
}
