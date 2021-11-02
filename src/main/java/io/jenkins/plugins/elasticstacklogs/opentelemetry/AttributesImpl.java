/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.opentelemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

public class AttributesImpl implements Attributes {
  private final Map<AttributeKey<?>, Object> map = new HashMap<>();

  public AttributesImpl() {
  }

  @Override
  public <T> T get(@NonNull AttributeKey<T> key) {
    return (T) map.get(key);
  }

  @Override
  public void forEach(@NonNull BiConsumer<? super AttributeKey<?>, ? super Object> biConsumer) {
    map.forEach(biConsumer);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    return map;
  }

  @Override
  public AttributesBuilder toBuilder() {
    return new AttributesBuilderImpl(map);
  }
}
