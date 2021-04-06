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

import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.lizhen.elasticsearch.repository.support.ElasticsearchEntityInformation;
import com.lizhen.elasticsearch.repository.support.ElasticsearchEntityInformationCreator;
import com.lizhen.elasticsearch.repository.support.MappingElasticsearchEntityInformation;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

/**
 * ElasticsearchEntityInformationCreatorImpl
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Christoph Strobl
 */
public class ElasticsearchEntityInformationCreatorImpl implements ElasticsearchEntityInformationCreator {

	private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

	public ElasticsearchEntityInformationCreatorImpl(
			MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {

		Assert.notNull(mappingContext, "MappingContext must not be null!");

		this.mappingContext = mappingContext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {

		ElasticsearchPersistentEntity<T> persistentEntity = (ElasticsearchPersistentEntity<T>) mappingContext
				.getRequiredPersistentEntity(domainClass);

		Assert.notNull(persistentEntity, String.format("Unable to obtain mapping metadata for %s!", domainClass));
		Assert.notNull(persistentEntity.getIdProperty(), String.format("No id property found for %s!", domainClass));

		return new MappingElasticsearchEntityInformation<>(persistentEntity);
	}
}
