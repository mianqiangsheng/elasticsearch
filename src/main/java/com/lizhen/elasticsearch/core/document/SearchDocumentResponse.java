/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lizhen.elasticsearch.core.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import com.lizhen.elasticsearch.core.document.DocumentAdapters;
import com.lizhen.elasticsearch.core.document.SearchDocument;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * This represents the complete search response from Elasticsearch, including the returned documents. Instances must be
 * created with the {@link #from(SearchResponse)} method.
 * 
 * @author Peter-Josef Meisch
 * @since 4.0
 */
public class SearchDocumentResponse {

	private final long totalHits;
	private final String totalHitsRelation;
	private final float maxScore;
	private final String scrollId;
	private final List<SearchDocument> searchDocuments;
	private final Aggregations aggregations;

	private SearchDocumentResponse(long totalHits, String totalHitsRelation, float maxScore, String scrollId,
			List<SearchDocument> searchDocuments, Aggregations aggregations) {
		this.totalHits = totalHits;
		this.totalHitsRelation = totalHitsRelation;
		this.maxScore = maxScore;
		this.scrollId = scrollId;
		this.searchDocuments = searchDocuments;
		this.aggregations = aggregations;
	}

	public long getTotalHits() {
		return totalHits;
	}

	public String getTotalHitsRelation() {
		return totalHitsRelation;
	}

	public float getMaxScore() {
		return maxScore;
	}

	public String getScrollId() {
		return scrollId;
	}

	public List<SearchDocument> getSearchDocuments() {
		return searchDocuments;
	}

	public Aggregations getAggregations() {
		return aggregations;
	}

	/**
	 * creates a SearchDocumentResponse from the {@link SearchResponse}
	 *
	 * @param searchResponse must not be {@literal null}
	 * @return the SearchDocumentResponse
	 */
	public static com.lizhen.elasticsearch.core.document.SearchDocumentResponse from(SearchResponse searchResponse) {

		Assert.notNull(searchResponse, "searchResponse must not be null");

		Aggregations aggregations = searchResponse.getAggregations();
		String scrollId = searchResponse.getScrollId();

		SearchHits searchHits = searchResponse.getHits();

		com.lizhen.elasticsearch.core.document.SearchDocumentResponse searchDocumentResponse = from(searchHits, scrollId, aggregations);
		return searchDocumentResponse;
	}

	/**
	 * creates a {@link com.lizhen.elasticsearch.core.document.SearchDocumentResponse} from {@link SearchHits} with the given scrollId and aggregations
	 * 
	 * @param searchHits the {@link SearchHits} to process
	 * @param scrollId scrollId
	 * @param aggregations aggregations
	 * @return the {@link com.lizhen.elasticsearch.core.document.SearchDocumentResponse}
	 * @since 4.1
	 */
	public static com.lizhen.elasticsearch.core.document.SearchDocumentResponse from(SearchHits searchHits, @Nullable String scrollId,
                                                                                                   @Nullable Aggregations aggregations) {
		TotalHits responseTotalHits = searchHits.getTotalHits();

		long totalHits;
		String totalHitsRelation;

		if (responseTotalHits != null) {
			totalHits = responseTotalHits.value;
			totalHitsRelation = responseTotalHits.relation.name();
		} else {
			totalHits = searchHits.getHits().length;
			totalHitsRelation = "OFF";
		}

		float maxScore = searchHits.getMaxScore();

		List<SearchDocument> searchDocuments = new ArrayList<>();
		for (SearchHit searchHit : searchHits) {
			if (searchHit != null) {
				searchDocuments.add(DocumentAdapters.from(searchHit));
			}
		}

		return new com.lizhen.elasticsearch.core.document.SearchDocumentResponse(totalHits, totalHitsRelation, maxScore, scrollId, searchDocuments, aggregations);
	}

}
