/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Stores the configuration of the Inputs.
 */
@Symbol("inputLogs")
@Extension
public class InputConfiguration extends AbstractElasticStackGlobalConfiguration {

  @CheckForNull
  private InputConf input;
  @CheckForNull
  private String indexPattern = "logs-*";

  @DataBoundConstructor
  public InputConfiguration() {
    load();
  }

  /**
   * Testing only
   */
  public InputConfiguration(boolean test) {
  }

  @Nonnull
  public static InputConfiguration get() {
    return ExtensionList.lookupSingleton(InputConfiguration.class);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    req.bindJSON(this, json);
    save();
    return true;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return "Logs settings";
  }

  @CheckForNull
  public InputConf getInput() {
    return input;
  }

  public void setInput(@CheckForNull InputConf input) {
    this.input = input;
  }

  @CheckForNull
  public String getIndexPattern() {
    return indexPattern;
  }

  @DataBoundSetter
  public void setIndexPattern(@CheckForNull String indexPattern) {
    this.indexPattern = indexPattern;
  }

  @RequirePOST
  public FormValidation doCheckIndexPattern(@QueryParameter String indexPattern) {
    if (StringUtils.isEmpty(indexPattern)) {
      return FormValidation.warning("The index pattern is required.");
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public FormValidation doValidate(
    @QueryParameter String credentialsId, @QueryParameter String elasticsearchUrl,
    @QueryParameter String indexPattern) {
    FormValidation elasticsearchUrlValidation = ElasticStackConfiguration.get()
                                                                         .doCheckElasticsearchUrl(elasticsearchUrl);
    if (elasticsearchUrlValidation.kind != FormValidation.Kind.OK) {
      return elasticsearchUrlValidation;
    }

    try {
      UsernamePasswordCredentials jenkinsCredentials = ElasticStackConfiguration.get().getCredentials(credentialsId);
      Retriever retriever = new Retriever(elasticsearchUrl, jenkinsCredentials.getUsername(),
                                          jenkinsCredentials.getPassword().getPlainText(), indexPattern
      );
      if (retriever.indexExists()) {
        return FormValidation.ok("success");
      }
    } catch (NoSuchElementException e) {
      return FormValidation.error("Invalid credentials.");
    } catch (IllegalArgumentException e) {
      return FormValidation.error(e, "Invalid Argument.");
    } catch (IOException e) {
      return FormValidation.error(e, "Unable to connect.");
    } catch (Exception e) {
      return FormValidation.error(e, e.getMessage());
    }
    return FormValidation.error("Index pattern not found.");
  }

  @Override
  public String toString() {
    return "InputConfiguration{" + ", input='" + (input != null ? input.getClass().getName() : "None") + '\''
           + ", indexPattern='" + (indexPattern != null ? indexPattern : "None") + '\'' + '}';
  }
}
