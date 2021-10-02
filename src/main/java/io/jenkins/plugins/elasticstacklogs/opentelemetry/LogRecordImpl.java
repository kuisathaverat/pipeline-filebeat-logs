package io.jenkins.plugins.elasticstacklogs.opentelemetry;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.logging.data.AnyValue;
import io.opentelemetry.sdk.logging.data.LogRecord;

public class LogRecordImpl extends LogRecord {

  private io.opentelemetry.proto.logs.v1.LogRecord record;
  public LogRecordImpl(io.opentelemetry.proto.logs.v1.LogRecord record){
  this.record = record;
  }

  @Override
  public long getTimeUnixNano() {
    return record.getTimeUnixNano();
  }

  @Override
  public String getTraceId() {
    return record.getTraceId().toStringUtf8();
  }

  @Override
  public String getSpanId() {
    return record.getSpanId().toStringUtf8();
  }

  @Override
  public int getFlags() {
    return record.getFlags();
  }

  @Override
  public Severity getSeverity() {
    return Severity.values()[record.getSeverityNumberValue()];
  }

  @Nullable
  @Override
  public String getSeverityText() {
    return record.getSeverityText();
  }

  @Nullable
  @Override
  public String getName() {
    return record.getName();
  }

  @Override
  public AnyValue getBody() {
    return AnyValue.stringAnyValue(record.getBody().getStringValue());
  }

  @Override
  public Attributes getAttributes() {
    AttributesBuilderImpl builder = new AttributesBuilderImpl();
    record.getAttributesList().stream().forEach(i -> builder.put(AttributeKey.stringKey(i.getKey()), i.getValue().getStringValue()));
    return builder.build();
  }
}
