/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.jenkins.plugins.elasticstacklogs;

import io.jenkins.plugins.elasticstacklogs.log.BuildInfo;
import io.jenkins.plugins.elasticstacklogs.log.Retriever;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the class to retrieve the logs from Elasticsearch.
 */
public class RetrieverTest {

  @Rule
  public ElasticsearchContainer esContainer = new ElasticsearchContainer();

  @Before
  public void setUp() throws Exception {
    esContainer.createFilebeatIndex();
  }

  @Test
  public void testRetrieve() throws IOException {
    Retriever retriever = new Retriever(esContainer.getUrl(), ElasticsearchContainer.USER_NAME, ElasticsearchContainer.PASSWORD, ElasticsearchContainer.INDEX);
    SearchResponse searchResponse = retriever.search(BuildInfo.getKey(ElasticsearchContainer.JOB_URL_VALUE, "2"));
    String scrollId = searchResponse.getScrollId();
    SearchHit[] searchHits = searchResponse.getHits().getHits();
    int counter = searchHits.length;

    while (searchHits != null && searchHits.length > 0) {
      searchResponse = retriever.next(scrollId);
      scrollId = searchResponse.getScrollId();
      searchHits = searchResponse.getHits().getHits();
      counter += searchHits.length;
    }

    ClearScrollResponse clearScrollResponse = retriever.clear(scrollId);
    assertTrue(clearScrollResponse.isSucceeded());
    assertEquals(counter, 100);
  }

  @Test
  public void testRetrieveNodeId() throws IOException {
    Retriever retriever = new Retriever(esContainer.getUrl(), ElasticsearchContainer.USER_NAME, ElasticsearchContainer.PASSWORD, ElasticsearchContainer.INDEX);
    SearchResponse searchResponse = retriever.search(BuildInfo.getKey(ElasticsearchContainer.JOB_URL_VALUE, "2"), "1");
    String scrollId = searchResponse.getScrollId();
    SearchHit[] searchHits = searchResponse.getHits().getHits();
    int counter = searchHits.length;

    while (searchHits != null && searchHits.length > 0) {
      searchResponse = retriever.next(scrollId);
      scrollId = searchResponse.getScrollId();
      searchHits = searchResponse.getHits().getHits();
      counter += searchHits.length;
    }

    ClearScrollResponse clearScrollResponse = retriever.clear(scrollId);
    assertTrue(clearScrollResponse.isSucceeded());
    assertEquals(counter, 50);
  }

}
