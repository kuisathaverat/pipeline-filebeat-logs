/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hudson.console.ConsoleNote;

/**
 * Utilities for extracting and reinserting {@link ConsoleNote}s.
 * copied from https://github.com/jenkinsci/pipeline-cloudwatch-logs-plugin
 */
class ConsoleNotes {

  private static final String MESSAGE_KEY = "message";
  private static final String ANNOTATIONS_KEY = "annotations";
  private static final String POSITION_KEY = "position";
  private static final String NOTE_KEY = "note";

  private ConsoleNotes() {
  }

  static Map<String, Object> parse(byte[] b, int len) {
    assert len > 0 && len <= b.length;
    Map<String, Object> data = new LinkedHashMap<>();
    int eol = len;
    while (eol > 0) {
      byte c = b[eol - 1];
      if (c == '\n' || c == '\r') {
        eol--;
      } else {
        break;
      }
    }
    String line = new String(b, 0, eol, StandardCharsets.UTF_8);
    // Would be more efficient to do searches at the byte[] level, but too much bother for now,
    // especially since there is no standard library method to do offset searches like String has.
    if (!line.contains(ConsoleNote.PREAMBLE_STR)) {
      // Shortcut for the common case that we have no notes.
      data.put(MESSAGE_KEY, line);
    } else {
      StringBuilder buf = new StringBuilder();
      List<Map<String, Object>> annotations = new ArrayList<>();
      int pos = 0;
      while (true) {
        int preamble = line.indexOf(ConsoleNote.PREAMBLE_STR, pos);
        if (preamble == -1) {
          break;
        }
        int endOfPreamble = preamble + ConsoleNote.PREAMBLE_STR.length();
        int postamble = line.indexOf(ConsoleNote.POSTAMBLE_STR, endOfPreamble);
        if (postamble == -1) {
          // Malformed; stop here.
          break;
        }
        buf.append(line, pos, preamble);
        annotations.add(
          ImmutableMap.of(POSITION_KEY, buf.length(), NOTE_KEY, line.substring(endOfPreamble, postamble)));
        pos = postamble + ConsoleNote.POSTAMBLE_STR.length();
      }
      buf.append(line, pos, line.length()); // append tail
      data.put(MESSAGE_KEY, buf.toString());
      data.put(ANNOTATIONS_KEY, annotations);
    }
    return data;
  }

  static void write(Writer w, JSONObject json) throws IOException {
    String message = json.getString(MESSAGE_KEY);
    JSONArray annotations = json.optJSONArray(ANNOTATIONS_KEY);
    if (annotations == null) {
      w.write(message);
    } else {
      int pos = 0;
      for (Object o : annotations) {
        JSONObject annotation = (JSONObject) o;
        int position = annotation.getInt(POSITION_KEY);
        String note = annotation.getString(NOTE_KEY);
        w.write(message, pos, position - pos);
        w.write(ConsoleNote.PREAMBLE_STR);
        w.write(note);
        w.write(ConsoleNote.POSTAMBLE_STR);
        pos = position;
      }
      w.write(message, pos, message.length() - pos);
    }
    w.write('\n');
  }

}
