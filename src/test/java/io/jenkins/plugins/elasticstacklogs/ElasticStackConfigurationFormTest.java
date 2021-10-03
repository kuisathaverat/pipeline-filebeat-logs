/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import io.jenkins.plugins.elasticstacklogs.config.ElasticStackConfiguration;
import org.junit.Test;
import hudson.util.FormValidation;
import static org.junit.Assert.assertEquals;

public class ElasticStackConfigurationFormTest {

  @Test
  public void testDoCheckKibanaUrl() {
    ElasticStackConfiguration config = new ElasticStackConfiguration(true);
    assertEquals(config.doCheckKibanaUrl("http://example.com:100").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckKibanaUrl("").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckKibanaUrl("foo").kind, FormValidation.Kind.ERROR);
  }

  @Test
  public void testDoCheckElasticsearchUrl() {
    ElasticStackConfiguration config = new ElasticStackConfiguration(true);
    assertEquals(config.doCheckElasticsearchUrl("http://example.com:1000").kind, FormValidation.Kind.OK);
    assertEquals(config.doCheckElasticsearchUrl("").kind, FormValidation.Kind.WARNING);
    assertEquals(config.doCheckElasticsearchUrl("foo").kind, FormValidation.Kind.ERROR);
  }
}
