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
package com.lizhen.elasticsearch.repository.support;

import com.lizhen.elasticsearch.core.ElasticsearchOperations;
import com.lizhen.elasticsearch.repository.support.ElasticsearchEntityInformation;
import com.lizhen.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import com.lizhen.elasticsearch.repository.support.SimpleElasticsearchRepository;

/**
 * Elasticsearch specific repository implementation. Likely to be used as target within
 * {@link ElasticsearchRepositoryFactory}
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Ryan Henszey
 * @author Sascha Woo
 * @author Peter-Josef Meisch
 * @deprecated since 4.1, derive from {@link SimpleElasticsearchRepository} instead
 */
@Deprecated
public abstract class AbstractElasticsearchRepository<T, ID> extends SimpleElasticsearchRepository<T, ID> {

	public AbstractElasticsearchRepository(ElasticsearchEntityInformation<T, ID> metadata,
			ElasticsearchOperations elasticsearchOperations) {
		super(metadata, elasticsearchOperations);
	}
}
