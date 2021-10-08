/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.elasticstacklogs.input.Input;
import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
import net.sf.json.JSONObject;
import hudson.console.LineTransformationOutputStream;

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

  public OutputStream(@Nonnull BuildInfo buildInfo, @CheckForNull String nodeId)
    throws URISyntaxException, IOException {
    this.buildInfo = buildInfo;
    this.nodeId = nodeId;
    input = buildInfo.getInput().get();
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
        LOGGER.log(Level.FINER, "scheduled event @{0} from {1}/{2}#{3}",
                   new Object[] { now, buildInfo.toString(), nodeId });
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
