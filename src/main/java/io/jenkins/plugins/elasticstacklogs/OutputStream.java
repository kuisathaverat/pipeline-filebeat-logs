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

package io.jenkins.plugins.elasticstacklogs;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.LineTransformationOutputStream;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import io.jenkins.plugins.elasticstacklogs.input.InputFactory;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
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
public class OutputStream extends LineTransformationOutputStream {
  private static final Logger LOGGER = Logger.getLogger(OutputStream.class.getName());
  @NonNull
  private final BuildInfo buildInfo;
  @NonNull
  private final String nodeId;
  @CheckForNull
  private Input input;

  public OutputStream(@Nonnull BuildInfo buildInfo, @CheckForNull String nodeId) throws URISyntaxException {
    this.buildInfo = buildInfo;
    this.nodeId = nodeId;
    input = InputFactory.createInput(new URI(buildInfo.getInput()));
  }

  @Override
  protected void eol(byte[] b, int len) throws IOException {
    String now = Retriever.now();
    Map<String, Object> data = ConsoleNotes.parse(b, len);
    data.put(Retriever.JOB_BUILD, buildInfo.getBuildId());
    data.put(Retriever.TIMESTAMP, now);
    data.put(Retriever.JOB_NAME, buildInfo.getJobName());
    data.put(Retriever.JOB_URL, buildInfo.getJobUrl());
    data.put(Retriever.JOB_ID, buildInfo.getKey());
    if (nodeId != null) {
      data.put(Retriever.JOB_NODE, nodeId);
    }
    //TODO add Otel data trace_id=%X{trace_id} span_id=%X{span_id} trace_flags=%X{trace_flags}
    try {
      if (writeOnInput(JSONObject.fromObject(data).toString())) {
        LOGGER.log(Level.FINER, "scheduled event @{0} from {1}/{2}#{3}", new Object[]{now, buildInfo.toString(), nodeId});
      } else {
        LOGGER.warning("Message buffer full, giving up");
      }
    } catch (Exception x) {
      LOGGER.log(Level.WARNING, "failed to send a message", x);
    }
  }

  private boolean writeOnInput(String line) throws IOException {
    LOGGER.log(Level.FINER, "Pipeline line: {0}", line);
    return input.write(line + "\n");
  }
}
