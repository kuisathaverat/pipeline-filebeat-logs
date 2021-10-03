/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.elasticstacklogs.config;

import java.util.logging.Logger;

import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;

public abstract class AbstractElasticStackGlobalConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(AbstractElasticStackGlobalConfiguration.class.getName());

    protected AbstractElasticStackGlobalConfiguration() {}

    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(ElasticStackGlobalConfigurationCategory.class);
    }

}
