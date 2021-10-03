/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;
import hudson.BulkChange;
import hudson.Extension;
import hudson.Functions;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import jenkins.model.Jenkins;

/**
 * Link to the Elastic Stack configuration on Manage Configuration.
 */
@Extension
@Symbol("elasticStack")
public class ElasticStackManagementLink extends ManagementLink implements Describable<ElasticStackManagementLink> {

  @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
  public static Predicate<Descriptor> FILTER = input -> input.getCategory() instanceof ElasticStackGlobalConfigurationCategory;

  private static Logger LOGGER = Logger.getLogger(ElasticStackManagementLink.class.getName());

  @CheckForNull
  @Override
  public String getUrlName() {
    return "elasticStack";
  }

  @CheckForNull
  @Override
  public String getDisplayName() {
    return "Elastic Stack";
  }

  @Override
  public String getIconFileName() {
    return "/plugin/pipeline-filebeat-logs/images/elastic_stack.png";
  }

  @Override
  public String getDescription() {
    return "Elastic Stack Configuration";
  }

  @Override
  public Category getCategory() {
    return Category.CONFIGURATION;
  }

  @Override
  public Descriptor<ElasticStackManagementLink> getDescriptor() {
    return Jenkins.get().getDescriptorOrDie(ElasticStackManagementLink.class);
  }

  @POST
  public synchronized void doConfigure(StaplerRequest req, StaplerResponse rsp)
    throws IOException, ServletException, Descriptor.FormException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    // for compatibility reasons, the actual value is stored in Jenkins
    BulkChange bc = new BulkChange(Jenkins.get());
    try {
      boolean result = configure(req, req.getSubmittedForm());
      LOGGER.log(Level.FINE, "Elastic Stack saved: " + result);
      Jenkins.get().save();
      FormApply.success(req.getContextPath() + "/manage").generateResponse(req, rsp, null);
    } finally {
      bc.commit();
    }
  }

  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    boolean result = true;
    for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigByDescriptor(FILTER)) {
      result &= configureDescriptor(req, json, d);
    }

    return result;
  }

  private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d)
    throws Descriptor.FormException {
    // collapse the structure to remain backward compatible with the JSON structure before 1.
    String name = d.getJsonSafeClassName();
    JSONObject js = json.has(name)
                    ? json.getJSONObject(name)
                    : new JSONObject(); // if it doesn't have the property, the method returns invalid null object.
    json.putAll(js);
    return d.configure(req, js);
  }

  @Extension
  @Symbol("elasticStack")
  public static final class DescriptorImpl extends Descriptor<ElasticStackManagementLink> {
    @Override
    public String getDisplayName() {
      return "Elastic Stack";
    }
  }
}
