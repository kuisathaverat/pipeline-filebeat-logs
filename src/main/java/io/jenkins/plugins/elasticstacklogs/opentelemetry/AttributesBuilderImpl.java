package io.jenkins.plugins.elasticstacklogs.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.HashMap;
import java.util.Map;

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
  public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
    data.put(key, value);
    return this;
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
    data.put(key, value);
    return this;
  }

  @Override
  public AttributesBuilder putAll(Attributes attributes) {
    attributes.forEach( (k,v) -> data.put(k,v));
    return this;
  }
}
