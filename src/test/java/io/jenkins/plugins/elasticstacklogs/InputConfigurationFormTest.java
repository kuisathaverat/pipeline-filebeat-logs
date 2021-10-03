/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import org.junit.Test;
import hudson.util.FormValidation;
import static org.junit.Assert.assertEquals;

/**
 * tests related to form validations that not need a Jenkins instance.
 */
public class InputConfigurationFormTest {

  @Test
  public void testDoCheckInput() {
    InputConfiguration config = new InputConfiguration(true);
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
    InputConfiguration config = new InputConfiguration(true);
    assertEquals(config.doCheckIndexPattern("foo").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckIndexPattern("").kind, FormValidation.Kind.WARNING);
  }
}
