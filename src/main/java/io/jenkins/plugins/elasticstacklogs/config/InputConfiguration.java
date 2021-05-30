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


package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.util.FormValidation;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

/**
 * Stores the configuration of the Pipeline Filebeat plugin.
 */
@Symbol("filebeatLogs")
@Extension
public class FilebeatConfiguration extends AbstractElasticStackGlobalConfiguration {
  private static final List<String> validSchemas = Arrays.asList("tcp", "udp", "file");

  @CheckForNull
  private String input;
  @CheckForNull
  private String indexPattern = "filebeat-*";

  @DataBoundConstructor
  public FilebeatConfiguration() {
    load();
  }

  /**
   * Testing only
   */
  public FilebeatConfiguration(boolean test) {
  }

  @Nonnull
  public static FilebeatConfiguration get() {
    return ExtensionList.lookupSingleton(FilebeatConfiguration.class);
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
    return "Filebeat Logs settings";
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
  public String getIndexPattern() {
    return indexPattern;
  }

  @DataBoundSetter
  public void setIndexPattern(@CheckForNull String indexPattern) {
    this.indexPattern = indexPattern;
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
  public FormValidation doCheckIndexPattern(@QueryParameter String indexPattern) {
    if (StringUtils.isEmpty(indexPattern)) {
      return FormValidation.warning("The Filebeat index pattern is required.");
    }
    return FormValidation.ok();
  }

  @RequirePOST
  public FormValidation doValidate(@QueryParameter String credentialsId,
                                   @QueryParameter String elasticsearchUrl, @QueryParameter String indexPattern) {
    FormValidation elasticsearchUrlValidation =
      ElasticStackConfiguration.get().doCheckElasticsearchUrl(elasticsearchUrl);
    if(elasticsearchUrlValidation.kind != FormValidation.Kind.OK){
      return elasticsearchUrlValidation;
    }

    try {
      UsernamePasswordCredentials jenkinsCredentials = ElasticStackConfiguration.get().getCredentials(credentialsId);
      Retriever retriever = new Retriever(
        elasticsearchUrl,
        jenkinsCredentials.getUsername(), jenkinsCredentials.getPassword().getPlainText(),
        indexPattern);
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
    return "FilebeatConfiguration{" +
      ", input='" + input + '\'' +
      ", indexPattern='" + indexPattern + '\'' +
      '}';
  }
}
