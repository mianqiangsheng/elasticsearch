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
package com.lizhen.elasticsearch.core;

import java.util.Objects;

import com.lizhen.elasticsearch.core.DocumentOperations;
import com.lizhen.elasticsearch.core.IndexOperations;
import com.lizhen.elasticsearch.core.SearchOperations;
import com.lizhen.elasticsearch.core.convert.ElasticsearchConverter;
import com.lizhen.elasticsearch.core.mapping.IndexCoordinates;
import com.lizhen.elasticsearch.core.routing.RoutingResolver;
import org.springframework.lang.Nullable;

/**
 * ElasticsearchOperations. Since 4.0 this interface only contains common helper functions, the other methods have been
 * moved to the different interfaces that are extended by ElasticsearchOperations. The interfaces now reflect the
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html">REST API structure</a> of
 * Elasticsearch.
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Kevin Leturc
 * @author Zetang Zeng
 * @author Dmitriy Yakovlev
 * @author Peter-Josef Meisch
 */
public interface ElasticsearchOperations extends DocumentOperations, SearchOperations {

	/**
	 * get an {@link IndexOperations} that is bound to the given class
	 *
	 * @return IndexOperations
	 */
	IndexOperations indexOps(Class<?> clazz);

	/**
	 * get an {@link IndexOperations} that is bound to the given index
	 * 
	 * @return IndexOperations
	 */
	IndexOperations indexOps(IndexCoordinates index);

	ElasticsearchConverter getElasticsearchConverter();

	IndexCoordinates getIndexCoordinatesFor(Class<?> clazz);

	/**
	 * gets the routing for an entity which might be defined by a join-type relation
	 * 
	 * @param entity the entity
	 * @return the routing, may be null if not set.
	 * @since 4.1
	 */
	@Nullable
	String getEntityRouting(Object entity);

	// region helper
	/**
	 * gets the String representation for an id.
	 * 
	 * @param id
	 * @return
	 * @since 4.0
	 */
	@Nullable
	default String stringIdRepresentation(@Nullable Object id) {
		return Objects.toString(id, null);
	}
	// endregion

	// region routing
	/**
	 * Returns a copy of this instance with the same configuration, but that uses a different {@link RoutingResolver} to
	 * obtain routing information.
	 *
	 * @param routingResolver the {@link RoutingResolver} value, must not be {@literal null}.
	 * @return DocumentOperations instance
	 * @since 4.2
	 */
	com.lizhen.elasticsearch.core.ElasticsearchOperations withRouting(RoutingResolver routingResolver);
	// endregion
}
