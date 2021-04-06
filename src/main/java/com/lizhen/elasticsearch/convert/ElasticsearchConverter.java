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
package com.lizhen.elasticsearch.convert;

import com.lizhen.elasticsearch.document.Document;
import com.lizhen.elasticsearch.mapping.ElasticsearchPersistentEntity;
import com.lizhen.elasticsearch.mapping.ElasticsearchPersistentProperty;
import com.lizhen.elasticsearch.query.CriteriaQuery;
import com.lizhen.elasticsearch.query.Query;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 * @author Sasch Woo
 * @author Roman Puchkovskiy
 */
public interface ElasticsearchConverter
		extends EntityConverter<ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty, Object, Document> {

	/**
	 * Get the configured {@link ProjectionFactory}. <br />
	 * <strong>NOTE</strong> Should be overwritten in implementation to make use of the type cache.
	 *
	 * @since 3.2
	 */
	default ProjectionFactory getProjectionFactory() {
		return new SpelAwareProxyProjectionFactory();
	}

	// region write
	/**
	 * Convert a given {@literal idValue} to its {@link String} representation taking potentially registered
	 * {@link org.springframework.core.convert.converter.Converter Converters} into account.
	 *
	 * @param idValue must not be {@literal null}.
	 * @return never {@literal null}.
	 * @since 3.2
	 */
	default String convertId(Object idValue) {

		Assert.notNull(idValue, "idValue must not be null!");

		if (!getConversionService().canConvert(idValue.getClass(), String.class)) {
			return idValue.toString();
		}

		return getConversionService().convert(idValue, String.class);
	}

	/**
	 * Map an object to a {@link Document}.
	 *
	 * @param source the object to map
	 * @return will not be {@literal null}.
	 */
	default Document mapObject(@Nullable Object source) {

		Document target = Document.create();

		if (source != null) {
			write(source, target);
		}
		return target;
	}
	// endregion

	// region query
	/**
	 * Updates a query by renaming the property names in the query to the correct mapped field names and the values to the
	 * converted values if the {@link ElasticsearchPersistentProperty} for a property has a
	 * {@link com.lizhen.elasticsearch.mapping.ElasticsearchPersistentPropertyConverter}. If
	 * domainClass is null, it's a noop; handling null here eliminates null checks in the caller.
	 * 
	 * @param query the query that is internally updated
	 * @param domainClass the class of the object that is searched with the query
	 */
	default void updateQuery(Query query, @Nullable Class<?> domainClass) {

		if (domainClass != null) {

			if (query instanceof CriteriaQuery) {
				updateCriteriaQuery((CriteriaQuery) query, domainClass);
			}
		}
	}

	/**
	 * Updates a {@link CriteriaQuery} by renaming the property names in the query to the correct mapped field names and
	 * the values to the converted values if the {@link ElasticsearchPersistentProperty} for a property has a
	 * {@link com.lizhen.elasticsearch.mapping.ElasticsearchPersistentPropertyConverter}.
	 *
	 * @param criteriaQuery the query that is internally updated
	 * @param domainClass the class of the object that is searched with the query
	 */
	void updateCriteriaQuery(CriteriaQuery criteriaQuery, Class<?> domainClass);
	// endregion
}
