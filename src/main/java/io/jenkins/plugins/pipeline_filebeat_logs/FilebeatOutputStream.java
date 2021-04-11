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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process the output stream and send it to Filebeat.
 */
public class FilebeatOutputStream extends LineTransformationOutputStream {
  private static final Logger LOGGER = Logger.getLogger(FilebeatOutputStream.class.getName());

  /**
   * for example {@code jenkinsci/git-plugin/master}
   */
  @Nonnull
  protected final
  String logStreamNameBase;
  /**
   * for example {@code 123}
   */
  @Nonnull
  protected final
  String buildId;
  /**
   * for example {@code 7}
   */
  @CheckForNull
  protected final
  String nodeId;

  @CheckForNull
  private Input filebeatInput;

  public FilebeatOutputStream(@Nonnull String logStreamNameBase, @Nonnull String buildId, @CheckForNull String nodeId) throws URISyntaxException {
    this.logStreamNameBase = logStreamNameBase;
    this.buildId = buildId;
    this.nodeId = nodeId;
    filebeatInput = InputFactory.createInput(new URI(FilebeatConfiguration.get().getInput()));
  }

  @Override
  protected void eol(byte[] b, int len) throws IOException {
    Map<String, Object> data = ConsoleNotes.parse(b, len);
    long now = System.currentTimeMillis();
    data.put("build", buildId);
    data.put("timestamp", now);
    if (nodeId != null) {
      data.put("node", nodeId);
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

  private boolean writeOnFilebeat(String line) {
    LOGGER.log(Level.FINER, "Pipeline line: {0}", line);
    //TODO implement it.
    return true;
  }
}
