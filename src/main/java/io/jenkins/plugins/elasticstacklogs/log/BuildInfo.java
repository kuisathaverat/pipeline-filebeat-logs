/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.log;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import io.jenkins.plugins.elasticstacklogs.config.InputConf;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;
import hudson.model.Run;

public class BuildInfo implements Serializable {

  private static final long serialVersionUID = 1;

  /**
   * Full URL to access to the job.
   * for example {@code https://jenkins.example.org/jenkins/job/jenkinsci/job/git-plugin/job/master}
   */
  @Nonnull
  private final String jobUrl;
  /**
   * Number of the build.
   * for example {@code 123}
   */
  @Nonnull
  private final String buildId;
  /**
   * Job name Path.
   * for example {@code jenkinsci/git-plugin/master}
   */
  @Nonnull
  private final String jobName;

  /**
   * Start time of the build.
   */
  @Nonnull
  private final String startTime;

  /**
   * Filebeat input to send the logs.
   */
  @Nonnull
  private final InputConf input;

  public BuildInfo(@Nonnull Run<?, ?> build) {
    this.jobUrl = build.getParent().getAbsoluteUrl();
    this.jobName = build.getParent().getFullDisplayName();
    this.buildId = build.getId();
    this.startTime = timeStampToString(build.getStartTimeInMillis());
    InputConf input = InputConfiguration.get().getInput();
    if(input == null){
      throw new NullPointerException("the plugin configuration is incorrect, you must set an input");
    }
    this.input = InputConfiguration.get().getInput();
  }

  public static String getKey(String jobUrl, String buildId) throws IOException {
    String s = jobUrl + "#" + buildId;
    return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  @Nonnull
  public String getJobUrl() {
    return jobUrl;
  }

  @Nonnull
  public String getBuildId() {
    return buildId;
  }

  @Nonnull
  public String getJobName() {
    return jobName;
  }

  @Nonnull
  public String getStartTime() {
    return startTime;
  }

  private String timeStampToString(long millis) {
    ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();
    ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), utc);
    return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
  }

  @Nonnull
  public InputConf getInput() {
    return input;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildInfo buildInfo = (BuildInfo) o;
    return jobUrl.equals(buildInfo.jobUrl) && buildId.equals(buildInfo.buildId) && jobName.equals(buildInfo.jobName)
           && input.equals(buildInfo.input);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jobUrl, buildId, jobName);
  }

  public String getKey() throws IOException {
    return BuildInfo.getKey(jobUrl, buildId);
  }

  @Override
  public String toString() {
    return "BuildInfo{" + "logStreamNameBase='" + jobUrl + '\'' + ", buildId='" + buildId + '\'' + ", jobName='"
           + jobName + '\'' + ", input='" + input + '\'' + '}';
  }
}
