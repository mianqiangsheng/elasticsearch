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
package com.lizhen.elasticsearch.repository.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.PageRequest;
import com.lizhen.elasticsearch.core.ElasticsearchOperations;
import com.lizhen.elasticsearch.core.SearchHitSupport;
import com.lizhen.elasticsearch.core.SearchHits;
import com.lizhen.elasticsearch.core.convert.DateTimeConverters;
import com.lizhen.elasticsearch.core.mapping.IndexCoordinates;
import com.lizhen.elasticsearch.core.query.StringQuery;
import com.lizhen.elasticsearch.repository.query.AbstractElasticsearchRepositoryQuery;
import com.lizhen.elasticsearch.repository.query.ElasticsearchQueryMethod;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;

/**
 * ElasticsearchStringQuery
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 * @author Taylor Ono
 * @author Peter-Josef Meisch
 */
public class ElasticsearchStringQuery extends AbstractElasticsearchRepositoryQuery {

	private static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
	private String query;

	private final GenericConversionService conversionService = new GenericConversionService();

	{
		if (!conversionService.canConvert(java.util.Date.class, String.class)) {
			conversionService.addConverter(DateTimeConverters.JavaDateConverter.INSTANCE);
		}
		if (ClassUtils.isPresent("org.joda.time.DateTimeZone", com.lizhen.elasticsearch.repository.query.ElasticsearchStringQuery.class.getClassLoader())) {
			if (!conversionService.canConvert(org.joda.time.ReadableInstant.class, String.class)) {
				conversionService.addConverter(DateTimeConverters.JodaDateTimeConverter.INSTANCE);
			}
			if (!conversionService.canConvert(org.joda.time.LocalDateTime.class, String.class)) {
				conversionService.addConverter(DateTimeConverters.JodaLocalDateTimeConverter.INSTANCE);
			}
		}
	}

	public ElasticsearchStringQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchOperations elasticsearchOperations,
			String query) {
		super(queryMethod, elasticsearchOperations);
		Assert.notNull(query, "Query cannot be empty");
		this.query = query;
	}

	@Override
	public boolean isCountQuery() {
		return queryMethod.hasCountQueryAnnotation();
	}

	@Override
	public Object execute(Object[] parameters) {

		Class<?> clazz = queryMethod.getResultProcessor().getReturnedType().getDomainType();
		ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);

		StringQuery stringQuery = createQuery(accessor);

		Assert.notNull(stringQuery, "unsupported query");

		if (queryMethod.hasAnnotatedHighlight()) {
			stringQuery.setHighlightQuery(queryMethod.getAnnotatedHighlightQuery());
		}

		IndexCoordinates index = elasticsearchOperations.getIndexCoordinatesFor(clazz);

		Object result = null;

		if (isCountQuery()) {
			result = elasticsearchOperations.count(stringQuery, clazz, index);
		} else if (queryMethod.isPageQuery()) {
			stringQuery.setPageable(accessor.getPageable());
			SearchHits<?> searchHits = elasticsearchOperations.search(stringQuery, clazz, index);
			result = SearchHitSupport.searchPageFor(searchHits, stringQuery.getPageable());
		} else if (queryMethod.isStreamQuery()) {
			if (accessor.getPageable().isUnpaged()) {
				stringQuery.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
			} else {
				stringQuery.setPageable(accessor.getPageable());
			}
			result = StreamUtils.createStreamFromIterator(elasticsearchOperations.searchForStream(stringQuery, clazz, index));
		} else if (queryMethod.isCollectionQuery()) {
			if (accessor.getPageable().isPaged()) {
				stringQuery.setPageable(accessor.getPageable());
			}
			result = elasticsearchOperations.search(stringQuery, clazz, index);
		} else {
			result = elasticsearchOperations.searchOne(stringQuery, clazz, index);
		}

		return queryMethod.isNotSearchHitMethod() ? SearchHitSupport.unwrapSearchHits(result) : result;
	}

	protected StringQuery createQuery(ParametersParameterAccessor parameterAccessor) {
		String queryString = replacePlaceholders(this.query, parameterAccessor);
		return new StringQuery(queryString);
	}

	private String replacePlaceholders(String input, ParametersParameterAccessor accessor) {

		Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
		String result = input;
		while (matcher.find()) {

			String placeholder = Pattern.quote(matcher.group()) + "(?!\\d+)";
			int index = NumberUtils.parseNumber(matcher.group(1), Integer.class);
			result = result.replaceAll(placeholder, getParameterWithIndex(accessor, index));
		}
		return result;
	}

	private String getParameterWithIndex(ParametersParameterAccessor accessor, int index) {
		Object parameter = accessor.getBindableValue(index);
		if (parameter == null) {
			return "null";
		}
		if (conversionService.canConvert(parameter.getClass(), String.class)) {
			return conversionService.convert(parameter, String.class);
		}
		return parameter.toString();
	}
}
