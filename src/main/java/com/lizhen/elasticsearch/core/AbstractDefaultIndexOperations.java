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
package com.lizhen.elasticsearch.core;

import static org.springframework.util.StringUtils.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import com.lizhen.elasticsearch.UncategorizedElasticsearchException;
import com.lizhen.elasticsearch.annotations.Mapping;
import com.lizhen.elasticsearch.core.IndexOperations;
import com.lizhen.elasticsearch.core.ResourceUtil;
import com.lizhen.elasticsearch.core.convert.ElasticsearchConverter;
import com.lizhen.elasticsearch.core.document.Document;
import com.lizhen.elasticsearch.core.index.AliasData;
import com.lizhen.elasticsearch.core.index.MappingBuilder;
import com.lizhen.elasticsearch.core.index.Settings;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.lizhen.elasticsearch.core.mapping.IndexCoordinates;
import com.lizhen.elasticsearch.core.query.AliasQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Base implementation of {@link IndexOperations} common to Transport and Rest based Implementations of IndexOperations.
 *
 * @author Peter-Josef Meisch
 * @author Sascha Woo
 * @since 4.0
 */
abstract class AbstractDefaultIndexOperations implements IndexOperations {

	private static final Logger LOGGER = LoggerFactory.getLogger(com.lizhen.elasticsearch.core.AbstractDefaultIndexOperations.class);

	protected final ElasticsearchConverter elasticsearchConverter;
	protected final com.lizhen.elasticsearch.core.RequestFactory requestFactory;

	@Nullable protected final Class<?> boundClass;
	@Nullable private final IndexCoordinates boundIndex;

	public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter, Class<?> boundClass) {

		Assert.notNull(boundClass, "boundClass may not be null");

		this.elasticsearchConverter = elasticsearchConverter;
		requestFactory = new com.lizhen.elasticsearch.core.RequestFactory(elasticsearchConverter);
		this.boundClass = boundClass;
		this.boundIndex = null;
	}

	public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter, IndexCoordinates boundIndex) {

		Assert.notNull(boundIndex, "boundIndex may not be null");

		this.elasticsearchConverter = elasticsearchConverter;
		requestFactory = new com.lizhen.elasticsearch.core.RequestFactory(elasticsearchConverter);
		this.boundClass = null;
		this.boundIndex = boundIndex;
	}

	protected Class<?> checkForBoundClass() {
		if (boundClass == null) {
			throw new InvalidDataAccessApiUsageException("IndexOperations are not bound");
		}
		return boundClass;
	}

	// region IndexOperations

	@Override
	public boolean create() {

		Settings settings = boundClass != null ? createSettings(boundClass) : new Settings();
		return doCreate(getIndexCoordinates(), settings, null);
	}

	@Override
	public Settings createSettings(Class<?> clazz) {

		Assert.notNull(clazz, "clazz must not be null");

		ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(clazz);
		String settingPath = persistentEntity.settingPath();
		return hasText(settingPath) //
				? Settings.parse(ResourceUtil.readFileFromClasspath(settingPath)) //
				: persistentEntity.getDefaultSettings();
	}

	@Override
	public boolean createWithMapping() {
		return doCreate(getIndexCoordinates(), createSettings(), createMapping());
	}

	@Override
	public boolean create(Map<String, Object> settings) {

		Assert.notNull(settings, "settings must not be null");

		return doCreate(getIndexCoordinates(), settings, null);
	}

	@Override
	public boolean create(Map<String, Object> settings, Document mapping) {

		Assert.notNull(settings, "settings must not be null");
		Assert.notNull(mapping, "mapping must not be null");

		return doCreate(getIndexCoordinates(), settings, mapping);
	}

	protected abstract boolean doCreate(IndexCoordinates index, Map<String, Object> settings, @Nullable Document mapping);

	@Override
	public boolean delete() {
		return doDelete(getIndexCoordinates());
	}

	protected abstract boolean doDelete(IndexCoordinates index);

	@Override
	public boolean exists() {
		return doExists(getIndexCoordinates());
	}

	protected abstract boolean doExists(IndexCoordinates index);

	@Override
	public boolean putMapping(Document mapping) {
		return doPutMapping(getIndexCoordinates(), mapping);
	}

	protected abstract boolean doPutMapping(IndexCoordinates index, Document mapping);

	@Override
	public Map<String, Object> getMapping() {
		return doGetMapping(getIndexCoordinates());
	}

	abstract protected Map<String, Object> doGetMapping(IndexCoordinates index);

	@Override
	public Settings getSettings() {
		return getSettings(false);
	}

	@Override
	public Settings getSettings(boolean includeDefaults) {
		return doGetSettings(getIndexCoordinates(), includeDefaults);
	}

	protected abstract Settings doGetSettings(IndexCoordinates index, boolean includeDefaults);

	@Override
	public void refresh() {
		doRefresh(getIndexCoordinates());
	}

	protected abstract void doRefresh(IndexCoordinates indexCoordinates);

	@Override
	@Deprecated
	public boolean addAlias(AliasQuery query) {
		return doAddAlias(query, getIndexCoordinates());
	}

	@Deprecated
	protected abstract boolean doAddAlias(AliasQuery query, IndexCoordinates index);

	@Override
	public List<AliasMetadata> queryForAlias() {
		return doQueryForAlias(getIndexCoordinates());
	}

	protected abstract List<AliasMetadata> doQueryForAlias(IndexCoordinates index);

	@Override
	@Deprecated
	public boolean removeAlias(AliasQuery query) {
		return doRemoveAlias(query, getIndexCoordinates());
	}

	@Deprecated
	protected abstract boolean doRemoveAlias(AliasQuery query, IndexCoordinates index);

	@Override
	public Map<String, Set<AliasData>> getAliases(String... aliasNames) {

		Assert.notEmpty(aliasNames, "aliasNames must not be empty");

		return doGetAliases(aliasNames, null);
	}

	@Override
	public Map<String, Set<AliasData>> getAliasesForIndex(String... indexNames) {

		Assert.notEmpty(indexNames, "indexNames must not be empty");

		return doGetAliases(null, indexNames);
	}

	protected abstract Map<String, Set<AliasData>> doGetAliases(@Nullable String[] aliasNames,
			@Nullable String[] indexNames);

	@Override
	public Document createMapping() {
		return createMapping(checkForBoundClass());
	}

	@Override
	public Document createMapping(Class<?> clazz) {
		return buildMapping(clazz);
	}

	protected Document buildMapping(Class<?> clazz) {

		// load mapping specified in Mapping annotation if present
		Mapping mappingAnnotation = AnnotatedElementUtils.findMergedAnnotation(clazz, Mapping.class);
		if (mappingAnnotation != null) {
			String mappingPath = mappingAnnotation.mappingPath();

			if (hasText(mappingPath)) {
				String mappings = ResourceUtil.readFileFromClasspath(mappingPath);

				if (hasText(mappings)) {
					return Document.parse(mappings);
				}
			} else {
				LOGGER.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
			}
		}

		// build mapping from field annotations
		try {
			String mapping = new MappingBuilder(elasticsearchConverter).buildPropertyMapping(clazz);
			return Document.parse(mapping);
		} catch (Exception e) {
			throw new UncategorizedElasticsearchException("Failed to build mapping for " + clazz.getSimpleName(), e);
		}
	}

	@Override
	public Settings createSettings() {
		return createSettings(checkForBoundClass());
	}

	// endregion

	// region Helper functions
	ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
		return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
	}

	@Override
	public IndexCoordinates getIndexCoordinates() {
		return (boundClass != null) ? getIndexCoordinatesFor(boundClass) : Objects.requireNonNull(boundIndex);
	}

	public IndexCoordinates getIndexCoordinatesFor(Class<?> clazz) {
		return getRequiredPersistentEntity(clazz).getIndexCoordinates();
	}
	// endregion
}
