# pipeline-filebeat-logs
Pipeline Logging using Filebeat Plugin

This plugins replaces the default logs storage for pipelines,
the new implementation stores the logs in Elasticsearch using Filebeat.

# Requirements

The plugin requires a Filebeat service up and running to connect to it.
This Filebeat service should expose an input of one of the following types:

[unix](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-unix.html)
[log](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html)
[filestream](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-filestream.html)
[tcp](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-tcp.html)
[udp](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-udp.html)

The plugin will use this input to send the events.
