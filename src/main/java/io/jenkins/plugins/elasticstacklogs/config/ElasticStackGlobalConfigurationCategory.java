/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import hudson.Extension;
import jenkins.model.GlobalConfigurationCategory;
import org.jenkinsci.Symbol;

/**
 * Category to group Elastic Stack configurations.
 */
@Symbol("elasticStackCategory")
@Extension
public class ElasticStackGlobalConfigurationCategory extends GlobalConfigurationCategory {

    @Override
    public String getDisplayName() {
        return "Elastic Stack Configuration";
    }
    @Override
    public String getShortDescription() {
        return "Elastic Stack Configuration";
    }
}
