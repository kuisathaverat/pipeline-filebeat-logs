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

import hudson.console.LineTransformationOutputStream;
import io.jenkins.plugins.pipeline_filebeat_logs.input.Input;
import io.jenkins.plugins.pipeline_filebeat_logs.input.InputFactory;
import net.sf.json.JSONObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the output stream and send it to Filebeat.
 */
public class FilebeatOutputStream extends LineTransformationOutputStream {
  private static final Logger LOGGER = Logger.getLogger(FilebeatOutputStream.class.getName());

  /**
   * for example {@code https://jenkins.example.org/jenkins/job/jenkinsci/job/git-plugin/job/master}
   */
  @Nonnull
  private final String logStreamNameBase;
  /**
   * for example {@code 123}
   */
  @Nonnull
  private final String buildId;
  /**
   * for example {@code 7}
   */
  @CheckForNull
  private final String nodeId;
  /**
   * for example {@code jenkinsci/git-plugin/master}
   */
  @Nonnull
  private final String jobName;

  @CheckForNull
  private Input filebeatInput;

  public FilebeatOutputStream(@Nonnull String logStreamNameBase, @Nonnull String buildId, @Nonnull String jobName, @CheckForNull String nodeId) throws URISyntaxException {
    this.logStreamNameBase = logStreamNameBase;
    this.buildId = buildId;
    this.nodeId = nodeId;
    this.jobName = jobName;
    filebeatInput = InputFactory.createInput(new URI(FilebeatConfiguration.get().getInput()));
  }

  @Override
  protected void eol(byte[] b, int len) throws IOException {
    ZonedDateTime date = ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
    String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
    Map<String, Object> data = ConsoleNotes.parse(b, len);
    data.put("job.build", buildId);
    data.put("@timestamp", now);
    data.put("job.name", jobName);
    data.put("job.url", logStreamNameBase);
    if (nodeId != null) {
      data.put("job.node", nodeId);
    }
    try {
      if (writeOnFilebeat(JSONObject.fromObject(data).toString())) {
        LOGGER.log(Level.FINER, "scheduled event @{0} from {1}/{2}#{3}", new Object[]{now, logStreamNameBase, buildId, nodeId});
      } else {
        LOGGER.warning("Message buffer full, giving up");
      }
    } catch (Exception x) {
      LOGGER.log(Level.WARNING, "failed to send a message", x);
    }
  }

  private boolean writeOnFilebeat(String line) throws IOException {
    LOGGER.log(Level.FINER, "Pipeline line: {0}", line);
    return filebeatInput.write(line + "\n");
  }
}
