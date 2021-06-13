package io.jenkins.plugins.elasticstacklogs.opentelemetry;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.logging.data.LogRecord;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogRecordConverter {

  private final Function<io.opentelemetry.proto.logs.v1.LogRecord, LogRecord> fromDto = t -> new LogRecordImpl(t);

  private final Function<LogRecord, io.opentelemetry.proto.logs.v1.LogRecord> fromEntity = new Function<LogRecord, io.opentelemetry.proto.logs.v1.LogRecord>() {
    @Override
    public io.opentelemetry.proto.logs.v1.LogRecord apply(LogRecord u) {
      io.opentelemetry.proto.logs.v1.LogRecord.Builder builder = io.opentelemetry.proto.logs.v1.LogRecord.newBuilder();
      builder.setName(u.getName())
        .setBody(io.opentelemetry.proto.common.v1.AnyValue.newBuilder().setStringValue(u.getBody().getStringValue()))
        .setFlags(u.getFlags())
        .setSeverityText(u.getSeverityText())
        .setSpanId(ByteString.copyFrom(u.getSpanId().getBytes()))
        .setTimeUnixNano(u.getTimeUnixNano())
        .setTraceId(ByteString.copyFrom(u.getTraceId().getBytes()));
      u.getAttributes().forEach((k, v) -> builder.addAttributes(
          KeyValue.newBuilder()
            .setKey(k.getKey())
            .setValue(io.opentelemetry.proto.common.v1.AnyValue.newBuilder().setStringValue(String.valueOf(k)))
            .build()
      ));
      return null;
    }
  };

  public LogRecordConverter() {
  }

  public final LogRecord convertFromDto(final io.opentelemetry.proto.logs.v1.LogRecord dto) {
    return fromDto.apply(dto);
  }

  public final io.opentelemetry.proto.logs.v1.LogRecord convertFromEntity(final LogRecord entity) {
    return fromEntity.apply(entity);
  }

  public final List<LogRecord> createFromDtos(final Collection<io.opentelemetry.proto.logs.v1.LogRecord> dtos) {
    return dtos.stream().map(this::convertFromDto).collect(Collectors.toList());
  }

  public final List<io.opentelemetry.proto.logs.v1.LogRecord> createFromEntities(final Collection<LogRecord> entities) {
    return entities.stream().map(this::convertFromEntity).collect(Collectors.toList());
  }
}
