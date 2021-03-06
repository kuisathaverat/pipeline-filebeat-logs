/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.elasticstacklogs.log;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * This class retrieve logs from Elasticsearch.
 */
public class Retriever {
  public static final String JOB_BUILD = "job.build";
  public static final String TIMESTAMP = "@timestamp";
  public static final String JOB_NAME = "job.name";
  public static final String JOB_URL = "job.url";
  public static final String JOB_ID = "job.id";
  public static final String JOB_NODE = "job.node";
  public static final TimeValue DEFAULT_TIMEVALUE = TimeValue.timeValueSeconds(30);
  public static final int PAGE_SIZE = 1000;
  public static final String MESSAGE = "message";
  @Nonnull
  private final CredentialsProvider credentialsProvider;
  @Nonnull
  private final String url;
  @Nonnull
  private final String index;

  /**
   * @param url      Elasticsearch URL.
   * @param username Username to access Elasticsearch.
   * @param password password to access Elasticsearch.
   * @param index    index or index pattern where the logs are.
   */
  public Retriever(@Nonnull String url, @Nonnull String username, @Nonnull String password, @Nonnull String index) {
    this.url = url;
    this.index = index;
    this.credentialsProvider = new BasicCredentialsProvider();
    org.apache.http.auth.UsernamePasswordCredentials credentials = new org.apache.http.auth.UsernamePasswordCredentials(
      username, password);
    this.credentialsProvider.setCredentials(AuthScope.ANY, credentials);
  }

  /**
   * @return the current timestamp on a valid format to Elasticsearch.
   */
  public static final String now() {
    ZonedDateTime date = ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
    return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
  }

  /**
   * @return the RestClientBuilder to create the Elasticsearch REST client.
   */
  public RestClientBuilder getBuilder() {
    RestClientBuilder builder = RestClient.builder(HttpHost.create(url));
    builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
      @Override
      public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
    });
    return builder;
  }

  /**
   * Search the log lines of a build ID.
   *
   * @param buildID build ID to search for the logs.
   * @return A page with log lines the results of the search. The object contains the scrollID to use in {@link #next(String)} requests.
   * @throws IOException
   */
  public SearchResponse search(@Nonnull String buildID) throws IOException {
    return search(buildID, null);
  }

  /**
   * Search the log lines of a build ID and Node ID.
   *
   * @param buildID build ID to search for the logs.
   * @param nodeID  A page with log lines the results of the search.
   * @return A page with log lines the results of the search. The object contains the scrollID to use in {@link #next(String)} requests.
   * @throws IOException
   */
  public SearchResponse search(@Nonnull String buildID, @CheckForNull String nodeID) throws IOException {
    try (RestHighLevelClient client = new RestHighLevelClient(getBuilder())) {
      final Scroll scroll = new Scroll(DEFAULT_TIMEVALUE);
      SearchRequest searchRequest = new SearchRequest(index);
      searchRequest.scroll(scroll);
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.size(PAGE_SIZE);
      searchSourceBuilder.sort(new FieldSortBuilder(TIMESTAMP).order(SortOrder.ASC));
      if (StringUtils.isBlank(nodeID)) {
        searchSourceBuilder.query(matchQuery(JOB_ID, buildID));
      } else {
        searchSourceBuilder.query(boolQuery().must(matchQuery(JOB_ID, buildID)).must(matchQuery(JOB_NODE, nodeID)));
      }
      searchRequest.source(searchSourceBuilder);

      return client.search(searchRequest, RequestOptions.DEFAULT);
    }
  }

  /**
   * Request the next page of a scroll search.
   *
   * @param scrollId Scroll ID to request the next page of log lines. see {@link #search(String)}
   * @return A page with log lines the results of the search. The object contains the scrollID to use in {@link #next(String)} requests.
   * @throws IOException
   */
  public SearchResponse next(@Nonnull String scrollId) throws IOException {
    return next(scrollId, DEFAULT_TIMEVALUE);
  }

  /**
   * Request the next page of a scroll search.
   *
   * @param scrollId         Scroll ID to request the next page of log lines. see {@link #search(String)}
   * @param timeValueSeconds seconds that will control how long to keep the scrolling resources open.
   * @return A page with log lines the results of the search. The object contains the scrollID to use in {@link #next(String)} requests.
   * @throws IOException
   */
  public SearchResponse next(@Nonnull String scrollId, @Nonnull TimeValue timeValueSeconds) throws IOException {
    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
    scrollRequest.scroll(timeValueSeconds);
    try (RestHighLevelClient client = new RestHighLevelClient(getBuilder())) {
      return client.scroll(scrollRequest, RequestOptions.DEFAULT);
    }
  }

  /**
   * Clears one or more scroll ids using the Clear Scroll API.
   *
   * @param scrollId Scroll ID to request the next page of log lines. see {@link #search(String)}
   * @return the object with the result.
   * @throws IOException
   */
  public ClearScrollResponse clear(@Nonnull String scrollId) throws IOException {
    ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
    clearScrollRequest.addScrollId(scrollId);
    try (RestHighLevelClient client = new RestHighLevelClient(getBuilder())) {
      return client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    }
  }

  /**
   * check if an index exists.
   *
   * @return true if the index exists.
   * @throws IOException
   */
  public boolean indexExists() throws IOException {
    boolean ret = false;
    if (StringUtils.isNotBlank(index)) {
      try (RestHighLevelClient client = new RestHighLevelClient(getBuilder())) {
        GetIndexRequest request = new GetIndexRequest(index);
        ret = client.indices().exists(request, RequestOptions.DEFAULT);
      }
    }
    return ret;
  }
}
