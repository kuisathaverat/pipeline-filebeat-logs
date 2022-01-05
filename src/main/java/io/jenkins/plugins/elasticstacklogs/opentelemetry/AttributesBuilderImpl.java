/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.opentelemetry;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

public class AttributesBuilderImpl implements AttributesBuilder {
  private Map<AttributeKey<?>, Object> data = new HashMap<>();

  public AttributesBuilderImpl() {
  }

  public AttributesBuilderImpl(Map<AttributeKey<?>, Object> data) {
    this.data = data;
  }

  @Override
  public Attributes build() {
    return null;
  }

  @Override
  public <T> AttributesBuilder put(@NonNull AttributeKey<Long> key, int value) {
    data.put(key, value);
    return this;
  }

  @Override
  public <T> AttributesBuilder put(@NonNull AttributeKey<T> key, @NonNull T value) {
    data.put(key, value);
    return this;
  }

  @Override
  public AttributesBuilder putAll(Attributes attributes) {
    attributes.forEach((k, v) -> data.put(k, v));
    return this;
  }
}
