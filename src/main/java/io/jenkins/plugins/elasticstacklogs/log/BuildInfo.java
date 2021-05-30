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

package io.jenkins.plugins.elasticstacklogs.log;

import hudson.model.Run;
import io.jenkins.plugins.elasticstacklogs.config.InputConfiguration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.TimeZone;

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
  private final String input;

  public BuildInfo(@Nonnull Run<?, ?> build) {
    this.jobUrl = build.getParent().getAbsoluteUrl();
    this.jobName = build.getParent().getFullDisplayName();
    this.buildId = build.getId();
    this.startTime = timeStampToString(build.getStartTimeInMillis());
    this.input = InputConfiguration.get().getInput();
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

  @Nonnull
  public String getInput() {
    return input;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BuildInfo buildInfo = (BuildInfo) o;
    return jobUrl.equals(buildInfo.jobUrl) &&
      buildId.equals(buildInfo.buildId) &&
      jobName.equals(buildInfo.jobName) &&
      input.equals(buildInfo.input);
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
      ", input='" + input + '\'' +
      '}';
  }
}
