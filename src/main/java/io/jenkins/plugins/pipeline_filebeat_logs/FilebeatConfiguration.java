package io.jenkins.plugins.pipeline_filebeat_logs;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;

import javax.annotation.Nonnull;

@Symbol("filebeatLogs")
@Extension
public class FilebeatConfiguration extends GlobalConfiguration {

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Filebeat Logs settings";
    }
}
