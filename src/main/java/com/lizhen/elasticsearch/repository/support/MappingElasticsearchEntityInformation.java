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

import org.elasticsearch.index.VersionType;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.lizhen.elasticsearch.core.mapping.IndexCoordinates;
import com.lizhen.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

/**
 * Elasticsearch specific implementation of
 * {@link org.springframework.data.repository.core.support.AbstractEntityInformation}
 *
 * @param <T>
 * @param <ID>
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Ryan Henszey
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Ivan Greene
 * @author Sylvain Laurent
 * @author Peter-Josef Meisch
 */
public class MappingElasticsearchEntityInformation<T, ID> extends PersistentEntityInformation<T, ID>
		implements ElasticsearchEntityInformation<T, ID> {

	private final ElasticsearchPersistentEntity<T> persistentEntity;

	public MappingElasticsearchEntityInformation(ElasticsearchPersistentEntity<T> persistentEntity) {
		super(persistentEntity);
		this.persistentEntity = persistentEntity;
	}

	@Override
	public String getIdAttribute() {
		return persistentEntity.getRequiredIdProperty().getFieldName();
	}

	@Override
	public IndexCoordinates getIndexCoordinates() {
		return persistentEntity.getIndexCoordinates();
	}

	@Override
	public Long getVersion(T entity) {

		ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();
		try {
			return versionProperty != null ? (Long) persistentEntity.getPropertyAccessor(entity).getProperty(versionProperty)
					: null;
		} catch (Exception e) {
			throw new IllegalStateException("failed to load version field", e);
		}
	}

	@Override
	public VersionType getVersionType() {
		return persistentEntity.getVersionType();
	}

	@Deprecated
	@Override
	public String getParentId(T entity) {

		ElasticsearchPersistentProperty parentProperty = persistentEntity.getParentIdProperty();
		try {
			return parentProperty != null ? (String) persistentEntity.getPropertyAccessor(entity).getProperty(parentProperty)
					: null;
		} catch (Exception e) {
			throw new IllegalStateException("failed to load parent ID: " + e, e);
		}
	}
}
