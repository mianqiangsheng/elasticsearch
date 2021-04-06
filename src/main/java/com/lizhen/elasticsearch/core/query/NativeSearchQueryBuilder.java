/*
 * Copyright 2013-2021 the original author or authors.
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
package com.lizhen.elasticsearch.core.query;

import static org.springframework.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;
import com.lizhen.elasticsearch.core.query.IndexBoost;
import com.lizhen.elasticsearch.core.query.NativeSearchQuery;
import com.lizhen.elasticsearch.core.query.ScriptField;
import com.lizhen.elasticsearch.core.query.SourceFilter;
import org.springframework.lang.Nullable;

/**
 * NativeSearchQuery
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Artur Konczak
 * @author Mark Paluch
 * @author Alen Turkovic
 * @author Sascha Woo
 * @author Jean-Baptiste Nizet
 * @author Martin Choraine
 * @author Farid Azaza
 * @author Peter-Josef Meisch
 * @author Peer Mueller
 */
public class NativeSearchQueryBuilder {

	@Nullable private QueryBuilder queryBuilder;
	@Nullable private QueryBuilder filterBuilder;
	private final List<ScriptField> scriptFields = new ArrayList<>();
	private final List<SortBuilder<?>> sortBuilders = new ArrayList<>();
	private final List<AbstractAggregationBuilder<?>> aggregationBuilders = new ArrayList<>();
	@Nullable private HighlightBuilder highlightBuilder;
	@Nullable private HighlightBuilder.Field[] highlightFields;
	private Pageable pageable = Pageable.unpaged();
	@Nullable private String[] fields;
	@Nullable private SourceFilter sourceFilter;
	@Nullable private CollapseBuilder collapseBuilder;
	@Nullable private List<IndexBoost> indicesBoost;
	@Nullable private SearchTemplateRequestBuilder searchTemplateBuilder;
	private float minScore;
	private boolean trackScores;
	@Nullable private Collection<String> ids;
	@Nullable private String route;
	@Nullable private SearchType searchType;
	@Nullable private IndicesOptions indicesOptions;
	@Nullable private String preference;
	@Nullable private Integer maxResults;
	@Nullable private Boolean trackTotalHits;
	@Nullable private TimeValue timeout;
	private final List<RescorerQuery> rescorerQueries = new ArrayList<>();

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withQuery(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withFilter(QueryBuilder filterBuilder) {
		this.filterBuilder = filterBuilder;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withSort(SortBuilder<?> sortBuilder) {
		this.sortBuilders.add(sortBuilder);
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withScriptField(ScriptField scriptField) {
		this.scriptFields.add(scriptField);
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withCollapseField(String collapseField) {
		this.collapseBuilder = new CollapseBuilder(collapseField);
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder addAggregation(AbstractAggregationBuilder<?> aggregationBuilder) {
		this.aggregationBuilders.add(aggregationBuilder);
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withHighlightBuilder(HighlightBuilder highlightBuilder) {
		this.highlightBuilder = highlightBuilder;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withHighlightFields(HighlightBuilder.Field... highlightFields) {
		this.highlightFields = highlightFields;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withIndicesBoost(List<IndexBoost> indicesBoost) {
		this.indicesBoost = indicesBoost;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withSearchTemplate(SearchTemplateRequestBuilder searchTemplateBuilder) {
		this.searchTemplateBuilder = searchTemplateBuilder;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withPageable(Pageable pageable) {
		this.pageable = pageable;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withFields(String... fields) {
		this.fields = fields;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withSourceFilter(SourceFilter sourceFilter) {
		this.sourceFilter = sourceFilter;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withMinScore(float minScore) {
		this.minScore = minScore;
		return this;
	}

	/**
	 * @param trackScores whether to track scores.
	 * @return this object
	 * @since 3.1
	 */
	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withTrackScores(boolean trackScores) {
		this.trackScores = trackScores;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withIds(Collection<String> ids) {
		this.ids = ids;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withRoute(String route) {
		this.route = route;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withSearchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withIndicesOptions(IndicesOptions indicesOptions) {
		this.indicesOptions = indicesOptions;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withPreference(String preference) {
		this.preference = preference;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * @since 4.2
	 */
	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withTrackTotalHits(Boolean trackTotalHits) {
		this.trackTotalHits = trackTotalHits;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withTimeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder withRescorerQuery(RescorerQuery rescorerQuery) {
		this.rescorerQueries.add(rescorerQuery);
		return this;
	}

	public NativeSearchQuery build() {

		NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder, filterBuilder, sortBuilders,
				highlightBuilder, highlightFields);

		nativeSearchQuery.setPageable(pageable);
		nativeSearchQuery.setTrackScores(trackScores);

		if (fields != null) {
			nativeSearchQuery.addFields(fields);
		}

		if (sourceFilter != null) {
			nativeSearchQuery.addSourceFilter(sourceFilter);
		}

		if (indicesBoost != null) {
			nativeSearchQuery.setIndicesBoost(indicesBoost);
		}

		if (searchTemplateBuilder != null) {
			nativeSearchQuery.setSearchTemplate(searchTemplateBuilder);
		}

		if (!isEmpty(scriptFields)) {
			nativeSearchQuery.setScriptFields(scriptFields);
		}

		if (collapseBuilder != null) {
			nativeSearchQuery.setCollapseBuilder(collapseBuilder);
		}

		if (!isEmpty(aggregationBuilders)) {
			nativeSearchQuery.setAggregations(aggregationBuilders);
		}

		if (minScore > 0) {
			nativeSearchQuery.setMinScore(minScore);
		}

		if (ids != null) {
			nativeSearchQuery.setIds(ids);
		}

		if (route != null) {
			nativeSearchQuery.setRoute(route);
		}

		if (searchType != null) {
			nativeSearchQuery.setSearchType(searchType);
		}

		if (indicesOptions != null) {
			nativeSearchQuery.setIndicesOptions(indicesOptions);
		}

		if (preference != null) {
			nativeSearchQuery.setPreference(preference);
		}

		if (maxResults != null) {
			nativeSearchQuery.setMaxResults(maxResults);
		}

		nativeSearchQuery.setTrackTotalHits(trackTotalHits);

		if (timeout != null) {
			nativeSearchQuery.setTimeout(timeout);
		}

		if (!isEmpty(rescorerQueries)) {
			nativeSearchQuery.setRescorerQueries(rescorerQueries);
		}

		return nativeSearchQuery;
	}
}
