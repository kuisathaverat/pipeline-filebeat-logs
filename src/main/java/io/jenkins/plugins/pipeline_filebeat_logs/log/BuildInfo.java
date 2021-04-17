package io.jenkins.plugins.pipeline_filebeat_logs.log;

import hudson.model.Run;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.TimeZone;

public class BuildInfo {
  /**
   * for example {@code https://jenkins.example.org/jenkins/job/jenkinsci/job/git-plugin/job/master}
   */
  @Nonnull
  private final String jobUrl;
  /**
   * for example {@code 123}
   */
  @Nonnull
  private final String buildId;
  /**
   * for example {@code jenkinsci/git-plugin/master}
   */
  @Nonnull
  private final String jobName;

  @Nonnull
  private final String startTime;

  public BuildInfo(@Nonnull Run<?, ?> build) {
    this.jobUrl = build.getParent().getAbsoluteUrl();
    this.jobName = build.getParent().getFullDisplayName();
    this.buildId = build.getId();
    this.startTime = timeStampToString(build.getStartTimeInMillis());
  }

  public static final String getKey(String jobUrl, String buildId) throws IOException {
    String s = jobUrl + "#" + buildId;
    return Base64.getEncoder().encodeToString(s.getBytes("UTF-8"));
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BuildInfo buildInfo = (BuildInfo) o;
    return jobUrl.equals(buildInfo.jobUrl) &&
      buildId.equals(buildInfo.buildId) &&
      jobName.equals(buildInfo.jobName);
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
    return "BuildInfo{" +
      "logStreamNameBase='" + jobUrl + '\'' +
      ", buildId='" + buildId + '\'' +
      ", jobName='" + jobName + '\'' +
      '}';
  }
}
