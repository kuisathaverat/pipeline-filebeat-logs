# pipeline-filebeat-logs
Pipeline Logging using Filebeat Plugin

This plugins replaces the default logs storage for pipelines,
the new implementation stores the logs in Elasticsearch using Filebeat.

# Requirements

The plugin requires a Filebeat service up and running to connect to it.
This Filebeat service should expose an input of one of the following types:

* [log](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html)
* [filestream](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-filestream.html)
* [tcp](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-tcp.html)
* [udp](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-udp.html)

The plugin will use this input to send the events.

# Configure

At Configure System/Filebeat settings you can set the Filebeat, Elasticsearch, and Kibana settigns

![configuration](docs/images/configuration.png)

* Filebeat Input (Required): Filebeat input to send the Jenkins events.
This input is an URI to the resource like <b>schema://path or host:port</b>
The *schemas* supported are :
  * tcp://host_or_ip:port
  * udp://host_or_ip:port
  * file://path_to_file/file
* Kibana URL: URL to access Kibana, will be show in the header logs to redirect
you to the logs in Kibana for advanced search (https://kibana.example.com:5601).
* Elasticsearch URL: URL to access Elasticsearch, it will be used to
retrieve the logs (https://es.example.com:9200).
* Filebeat Index pattern: Index pattern used to retrieve the logs from Elasticsearch.
  The default values is *filebeat-\**
* Elasticsearch credentials: Credentials to access to Elasticsearch, the user
has to have access to the Filebeat indices.

# Validate the Elasticsearch configuration

The configuration  has a button to validate the Elasticsearch configuration,
This button will try to connect to Elasticsearch
and checks if the Filebeat index pattern exists.

These are the possible errors:

When you did not set credentials.

![](docs/images/error_invalid_cred.png)

When the URL os Elasticsearch is not valid.

![](docs/images/error_invalid_es_url.png)

When for other reasing the connection fails, in this case,
you have to check the Jenkins logs for more details.

![](docs/images/error_unable_to_connect.png)
