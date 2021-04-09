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

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Stores the configuration of the Pipeline Filebeat plugin.
 */
@Symbol("filebeatLogs")
@Extension
public class FilebeatConfiguration extends GlobalConfiguration {
  private static final String ERROR_MALFORMED_URL = "The url is malformed.";
  private static final List<String> validSchemas = Arrays.asList("unix", "tcp", "udp", "file");

  @CheckForNull
  private String kibanaUrl;
  @CheckForNull
  private String elasticsearchUrl;
  @CheckForNull
  private String input;
  @CheckForNull
  private String credentialsId;
  @CheckForNull
  private String indexPattern = "filebeat-*";

  @NonNull
  @Override
  public String getDisplayName() {
    return "Filebeat Logs settings";
  }

  @CheckForNull
  public String getKibanaUrl() {
    return kibanaUrl;
  }

  @DataBoundSetter
  public void setKibanaUrl(String kibanaUrl) throws MalformedURLException {
    this.kibanaUrl = Util.fixNull(kibanaUrl);
  }

  @CheckForNull
  public String getInput() {
    return input;
  }

  @DataBoundSetter
  public void setInput(String input) {
    this.input = Util.fixNull(input);
  }

  @CheckForNull
  public String getElasticsearchUrl() {
    return elasticsearchUrl;
  }


  @DataBoundSetter
  public void setElasticsearchUrl(@CheckForNull String elasticsearchUrl) throws MalformedURLException {
    this.elasticsearchUrl = Util.fixNull(elasticsearchUrl);
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
  private UsernamePasswordCredentials getCredentials(String credentialsId) throws NoSuchElementException {
    Optional<Credentials> optionalCredentials = SystemCredentialsProvider.getInstance()
      .getCredentials()
      .stream()
      .filter(credentials -> (credentials instanceof UsernamePasswordCredentials) && ((IdCredentials) credentials).getId().equals(credentialsId))
      .findAny();
    return (UsernamePasswordCredentials) optionalCredentials.get();
  }

  @CheckForNull
  public String getIndexPattern() {
    return indexPattern;
  }

  @DataBoundSetter
  public void setIndexPattern(@CheckForNull String indexPattern) {
    this.indexPattern = indexPattern;
  }

  @Nonnull
  public static FilebeatConfiguration get(){
    return ExtensionList.lookupSingleton(FilebeatConfiguration.class);
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
  public FormValidation doCheckInput(@QueryParameter("input") String uri) {
    if (StringUtils.isEmpty(uri)) {
      return FormValidation.warning("The Filebeat input is required.");
    }
    URI checkUri = URI.create(uri);
    if (validSchemas.contains(checkUri.getScheme())) {
      return FormValidation.ok();
    }

    return FormValidation.error("The Filebeat input URI is not valid.");
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
  public ListBoxModel doFillCredentialsIdItems(Item context,
                                               @QueryParameter String credentialsId) {
    if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
      context != null && !context.hasPermission(context.CONFIGURE)) {
      return new StandardListBoxModel();
    }

    return new StandardListBoxModel().includeEmptyValue()
      .includeAs(ACL.SYSTEM, context, StandardUsernameCredentials.class)
      .includeCurrentValue(credentialsId);
  }

  @RequirePOST
  public FormValidation doCheckCredentialsId(Item context,
                                             @QueryParameter String credentialsId) {
    if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
      context != null && !context.hasPermission(context.CONFIGURE)) {
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
  public FormValidation doCheckIndexPattern(@QueryParameter String indexPattern) {
    if (StringUtils.isEmpty(indexPattern)) {
      return FormValidation.warning("The Filebeat index pattern is required.");
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public FormValidation doValidate(@QueryParameter String elasticsearchUrl,
                          @QueryParameter String credentialsId, @QueryParameter String indexPattern) {
    try {
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      UsernamePasswordCredentials jenkinsCredentials = getCredentials(credentialsId);
      org.apache.http.auth.UsernamePasswordCredentials credentials =
        new org.apache.http.auth.UsernamePasswordCredentials(jenkinsCredentials.getUsername(), jenkinsCredentials.getPassword().getPlainText());
      credentialsProvider.setCredentials(AuthScope.ANY, credentials);

      RestClientBuilder builder = RestClient.builder(HttpHost.create(elasticsearchUrl));
      builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(
          HttpAsyncClientBuilder httpClientBuilder) {
          return httpClientBuilder
            .setDefaultCredentialsProvider(credentialsProvider);
        }
      });

      try (RestHighLevelClient client = new RestHighLevelClient(builder)) {
        GetIndexRequest request = new GetIndexRequest(indexPattern);
        client.indices().exists(request, RequestOptions.DEFAULT);
      }
    } catch(NoSuchElementException e){
      return FormValidation.error("Invalid credentials");
    } catch(IllegalArgumentException e){
      return FormValidation.error("Invalid Elasticsearch host");
    } catch (IOException e) {
      return FormValidation.error("Unable to connect.");
    }
    return FormValidation.ok("success");
  }

}
