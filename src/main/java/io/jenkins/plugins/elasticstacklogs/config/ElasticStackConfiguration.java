/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

/**
 * Stores the configuration of the Elastic Stack.
 */
@Symbol("elasticStack")
@Extension
public class ElasticStackConfiguration extends AbstractElasticStackGlobalConfiguration {
  private static final String ERROR_MALFORMED_URL = "The url is malformed.";

  @CheckForNull
  private String kibanaUrl;
  @CheckForNull
  private String elasticsearchUrl;
  @CheckForNull
  private String credentialsId;

  @DataBoundConstructor
  public ElasticStackConfiguration() {
    load();
  }

  /**
   * Testing only
   */
  public ElasticStackConfiguration(boolean test) {
  }

  @Nonnull
  public static ElasticStackConfiguration get() {
    return ExtensionList.lookupSingleton(ElasticStackConfiguration.class);
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
    return "Elastic Stack settings";
  }

  @CheckForNull
  public String getKibanaUrl() {
    return kibanaUrl;
  }

  @DataBoundSetter
  public void setKibanaUrl(String kibanaUrl) {
    this.kibanaUrl = Util.fixNull(kibanaUrl);
  }

  @CheckForNull
  public String getCredentialsId() {
    return credentialsId;
  }

  @DataBoundSetter
  public void setCredentialsId(@CheckForNull String credentialsId) {
    this.credentialsId = credentialsId;
  }

  @NonNull
  public UsernamePasswordCredentials getCredentials() throws NoSuchElementException {
    return getCredentials(credentialsId);
  }

  @NonNull
  public UsernamePasswordCredentials getCredentials(String credentialsId) throws NoSuchElementException {
    Optional<Credentials> optionalCredentials = SystemCredentialsProvider.getInstance().getCredentials().stream()
                                                                         .filter(credentials ->
                                                                                   (credentials instanceof UsernamePasswordCredentials)
                                                                                   && ((IdCredentials) credentials).getId()
                                                                                                                   .equals(
                                                                                                                     credentialsId))
                                                                         .findAny();
    return (UsernamePasswordCredentials) optionalCredentials.get();
  }

  @CheckForNull
  public String getElasticsearchUrl() {
    return elasticsearchUrl;
  }

  @DataBoundSetter
  public void setElasticsearchUrl(@CheckForNull String elasticsearchUrl) {
    this.elasticsearchUrl = Util.fixNull(elasticsearchUrl);
  }

  @RequirePOST
  public FormValidation doCheckKibanaUrl(@QueryParameter("kibanaUrl") String url) {
    if (StringUtils.isEmpty(url)) {
      return FormValidation.ok();
    }
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      return FormValidation.error(ERROR_MALFORMED_URL, e);
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public FormValidation doCheckElasticsearchUrl(@QueryParameter("elasticsearchUrl") String url) {
    if (StringUtils.isEmpty(url)) {
      return FormValidation.warning("The Elasticsearch URL is required.");
    }
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      return FormValidation.error(ERROR_MALFORMED_URL, e);
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public ListBoxModel doFillCredentialsIdItems(Item context, @QueryParameter String credentialsId) {
    if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
        || context != null && !context.hasPermission(context.CONFIGURE)) {
      return new StandardListBoxModel();
    }

    return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, context,
                                                                    StandardUsernameCredentials.class)
                                     .includeCurrentValue(credentialsId);
  }

  @RequirePOST
  public FormValidation doCheckCredentialsId(Item context, @QueryParameter String credentialsId) {
    if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
        || context != null && !context.hasPermission(context.CONFIGURE)) {
      return FormValidation.ok();
    }

    try {
      getCredentials(credentialsId);
    } catch (NoSuchElementException e) {
      return FormValidation.warning("The credentials are not valid.");
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public FormValidation doValidate(@QueryParameter String credentialsId, @QueryParameter String elasticsearchUrl) {
    FormValidation elasticsearchUrlValidation = doCheckElasticsearchUrl(elasticsearchUrl);
    if (elasticsearchUrlValidation.kind != FormValidation.Kind.OK) {
      return elasticsearchUrlValidation;
    }

    try {
      UsernamePasswordCredentials jenkinsCredentials = getCredentials(credentialsId);
      BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      org.apache.http.auth.UsernamePasswordCredentials credentials = new org.apache.http.auth.UsernamePasswordCredentials(
        jenkinsCredentials.getUsername(), jenkinsCredentials.getPassword().getPlainText());
      credentialsProvider.setCredentials(AuthScope.ANY, credentials);

      RestClientBuilder builder = RestClient.builder(HttpHost.create(elasticsearchUrl));
      builder.setHttpClientConfigCallback(
        httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

      try (RestHighLevelClient client = new RestHighLevelClient(builder)) {
        MainResponse response = client.info(RequestOptions.DEFAULT);
        if (StringUtils.isNotBlank(response.getVersion().getNumber())) {
          return FormValidation.ok("success connected to " + response.getVersion().getNumber());
        }
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
    return "ElasticStackConfiguration{" + "kibanaUrl='" + kibanaUrl + '\'' + ", elasticsearchUrl='" + elasticsearchUrl
           + '\'' + ", credentialsId='" + credentialsId + '\'' + '}';
  }
}
