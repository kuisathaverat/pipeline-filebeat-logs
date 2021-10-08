package io.jenkins.plugins.elasticstacklogs.config;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

/**
 * Interface for configure inputs.
 */
public abstract class InputConf implements Describable<InputConf>, Serializable {

  public abstract Input get() throws IOException;

  @Override
  public InputConfDescriptor getDescriptor() {
    return (InputConfDescriptor) Jenkins.get().getDescriptorOrDie(getClass());
  }

  public static abstract class InputConfDescriptor extends Descriptor<InputConf> {

    @RequirePOST
    public FormValidation doCheckHost(@QueryParameter String value) {
      FormValidation ret = FormValidation.ok();
      if (StringUtils.isEmpty(value)) {
        return FormValidation.error("The host cannot be empty.");
      }
      return ret;
    }

    @RequirePOST
    public FormValidation doCheckPort(@QueryParameter String value) {
      if (StringUtils.isEmpty(value)) {
        return FormValidation.error("The port value cannot be empty.");
      }
      try {
        int portValue = Integer.parseInt(value);
        if (portValue <= 0) {
          return FormValidation.error("The port must be a positive number.");
        }
        if (portValue >= 65536) {
          return FormValidation.error("The port number must be lower or equal than 65536");
        }
        return FormValidation.ok();
      } catch (NumberFormatException e) {
        return FormValidation.error(e, "the port must be a number.");
      }
    }

    @RequirePOST
    public FormValidation doCheckEndpoint(@QueryParameter String value) {
      FormValidation ret = FormValidation.ok();
      if (StringUtils.isEmpty(value)) {
        return FormValidation.error("The endpoint cannot be empty.");
      }
      try {
        new URL(value);
      } catch (MalformedURLException e) {
        return FormValidation.error("The endpoint is not a valid URL.", e);
      }
      return ret;
    }
  }
}
