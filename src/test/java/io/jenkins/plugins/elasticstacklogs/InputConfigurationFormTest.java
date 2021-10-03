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
  public void testDoCheckIndexPattern() {
    InputConfiguration config = new InputConfiguration(true);
    assertEquals(config.doCheckIndexPattern("foo").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckIndexPattern("").kind, FormValidation.Kind.WARNING);
  }
}
