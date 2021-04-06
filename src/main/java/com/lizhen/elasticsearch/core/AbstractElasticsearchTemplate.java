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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.WriteRequestBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.convert.EntityReader;
import com.lizhen.elasticsearch.BulkFailureException;
import com.lizhen.elasticsearch.core.convert.ElasticsearchConverter;
import com.lizhen.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.lizhen.elasticsearch.core.document.Document;
import com.lizhen.elasticsearch.core.document.SearchDocumentResponse;
import com.lizhen.elasticsearch.core.event.AfterConvertCallback;
import com.lizhen.elasticsearch.core.event.AfterSaveCallback;
import com.lizhen.elasticsearch.core.event.BeforeConvertCallback;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.lizhen.elasticsearch.core.mapping.IndexCoordinates;
import com.lizhen.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.lizhen.elasticsearch.core.query.BulkOptions;
import com.lizhen.elasticsearch.core.query.ByQueryResponse;
import com.lizhen.elasticsearch.core.query.IndexQuery;
import com.lizhen.elasticsearch.core.query.IndexQueryBuilder;
import com.lizhen.elasticsearch.core.query.MoreLikeThisQuery;
import com.lizhen.elasticsearch.core.query.NativeSearchQueryBuilder;
import com.lizhen.elasticsearch.core.query.Query;
import com.lizhen.elasticsearch.core.query.SeqNoPrimaryTerm;
import com.lizhen.elasticsearch.core.query.UpdateQuery;
import com.lizhen.elasticsearch.core.routing.DefaultRoutingResolver;
import com.lizhen.elasticsearch.core.routing.RoutingResolver;
import com.lizhen.elasticsearch.support.VersionInfo;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * AbstractElasticsearchTemplate
 *
 * @author Sascha Woo
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 * @author Subhobrata Dey
 */
public abstract class AbstractElasticsearchTemplate implements ElasticsearchOperations, ApplicationContextAware {

	@Nullable protected ElasticsearchConverter elasticsearchConverter;
	@Nullable protected RequestFactory requestFactory;
	@Nullable private EntityOperations entityOperations;
	@Nullable private EntityCallbacks entityCallbacks;
	@Nullable private RefreshPolicy refreshPolicy;
	@Nullable protected RoutingResolver routingResolver;

	// region Initialization
	protected void initialize(ElasticsearchConverter elasticsearchConverter) {

		Assert.notNull(elasticsearchConverter, "elasticsearchConverter must not be null.");

		this.elasticsearchConverter = elasticsearchConverter;
		MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext = this.elasticsearchConverter
				.getMappingContext();
		this.entityOperations = new EntityOperations(mappingContext);
		this.routingResolver = new DefaultRoutingResolver((SimpleElasticsearchMappingContext) mappingContext);

		requestFactory = new RequestFactory(elasticsearchConverter);
		VersionInfo.logVersions(getClusterVersion());
	}

	/**
	 * @return copy of this instance.
	 */
	private com.lizhen.elasticsearch.core.AbstractElasticsearchTemplate copy() {

		com.lizhen.elasticsearch.core.AbstractElasticsearchTemplate copy = doCopy();

		if (entityCallbacks != null) {
			copy.setEntityCallbacks(entityCallbacks);
		}

		copy.setRoutingResolver(routingResolver);

		return copy;
	}

	protected abstract com.lizhen.elasticsearch.core.AbstractElasticsearchTemplate doCopy();

	protected ElasticsearchConverter createElasticsearchConverter() {
		MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(
				new SimpleElasticsearchMappingContext());
		mappingElasticsearchConverter.afterPropertiesSet();
		return mappingElasticsearchConverter;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		if (entityCallbacks == null) {
			setEntityCallbacks(EntityCallbacks.create(applicationContext));
		}

		if (elasticsearchConverter instanceof ApplicationContextAware) {
			((ApplicationContextAware) elasticsearchConverter).setApplicationContext(applicationContext);
		}
	}

	/**
	 * Set the {@link EntityCallbacks} instance to use when invoking {@link EntityCallbacks callbacks} like the
	 * {@link BeforeConvertCallback}.
	 * <p />
	 * Overrides potentially existing {@link EntityCallbacks}.
	 *
	 * @param entityCallbacks must not be {@literal null}.
	 * @throws IllegalArgumentException if the given instance is {@literal null}.
	 * @since 4.0
	 */
	public void setEntityCallbacks(EntityCallbacks entityCallbacks) {

		Assert.notNull(entityCallbacks, "entityCallbacks must not be null");

		this.entityCallbacks = entityCallbacks;
	}

	public void setRefreshPolicy(@Nullable RefreshPolicy refreshPolicy) {
		this.refreshPolicy = refreshPolicy;
	}

	@Nullable
	public RefreshPolicy getRefreshPolicy() {
		return refreshPolicy;
	}
	// endregion

	// region DocumentOperations
	@Override
	public <T> T save(T entity) {

		Assert.notNull(entity, "entity must not be null");

		return save(entity, getIndexCoordinatesFor(entity.getClass()));
	}

	@Override
	public <T> T save(T entity, IndexCoordinates index) {

		Assert.notNull(entity, "entity must not be null");
		Assert.notNull(index, "index must not be null");

		T entityAfterBeforeConvert = maybeCallbackBeforeConvert(entity, index);

		IndexQuery query = getIndexQuery(entityAfterBeforeConvert);
		doIndex(query, index);

		T entityAfterAfterSave = maybeCallbackAfterSave(entityAfterBeforeConvert, index);

		return entityAfterAfterSave;
	}

	@Override
	public <T> Iterable<T> save(Iterable<T> entities) {

		Assert.notNull(entities, "entities must not be null");

		Iterator<T> iterator = entities.iterator();
		if (iterator.hasNext()) {
			return save(entities, getIndexCoordinatesFor(iterator.next().getClass()));
		}

		return entities;
	}

	@Override
	public <T> Iterable<T> save(Iterable<T> entities, IndexCoordinates index) {

		Assert.notNull(entities, "entities must not be null");
		Assert.notNull(index, "index must not be null");

		List<IndexQuery> indexQueries = Streamable.of(entities).stream().map(this::getIndexQuery)
				.collect(Collectors.toList());

		if (!indexQueries.isEmpty()) {
			List<IndexedObjectInformation> indexedObjectInformations = bulkIndex(indexQueries, index);
			Iterator<IndexedObjectInformation> iterator = indexedObjectInformations.iterator();
			entities.forEach(entity -> updateIndexedObject(entity, iterator.next()));
		}

		return indexQueries.stream().map(IndexQuery::getObject).map(entity -> (T) entity).collect(Collectors.toList());
	}

	@Override
	public <T> Iterable<T> save(T... entities) {
		return save(Arrays.asList(entities));
	}

	@Override
	public String index(IndexQuery query, IndexCoordinates index) {

		maybeCallbackBeforeConvertWithQuery(query, index);

		String documentId = doIndex(query, index);

		maybeCallbackAfterSaveWithQuery(query, index);

		return documentId;
	}

	public abstract String doIndex(IndexQuery query, IndexCoordinates indexCoordinates);

	@Override
	@Nullable
	public <T> T get(String id, Class<T> clazz) {
		return get(id, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public <T> List<MultiGetItem<T>> multiGet(Query query, Class<T> clazz) {
		return multiGet(query, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public boolean exists(String id, Class<?> clazz) {
		return exists(id, getIndexCoordinatesFor(clazz));
	}

	@Override
	public boolean exists(String id, IndexCoordinates index) {
		return doExists(id, index);
	}

	abstract protected boolean doExists(String id, IndexCoordinates index);

	@Override
	public String delete(String id, Class<?> entityType) {

		Assert.notNull(id, "id must not be null");
		Assert.notNull(entityType, "entityType must not be null");

		return this.delete(id, getIndexCoordinatesFor(entityType));
	}

	@Override
	public ByQueryResponse delete(Query query, Class<?> clazz) {
		return delete(query, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public String delete(Object entity) {
		return delete(entity, getIndexCoordinatesFor(entity.getClass()));
	}

	@Override
	public String delete(Object entity, IndexCoordinates index) {
		return this.delete(getEntityId(entity), index);
	}

	@Override
	public String delete(String id, IndexCoordinates index) {
		return doDelete(id, routingResolver.getRouting(), index);
	}

	@Override
	@Deprecated
	final public String delete(String id, @Nullable String routing, IndexCoordinates index) {
		return doDelete(id, routing, index);
	}

	protected abstract String doDelete(String id, @Nullable String routing, IndexCoordinates index);

	@Override
	public List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, Class<?> clazz) {
		return bulkIndex(queries, getIndexCoordinatesFor(clazz));
	}

	@Override
	public List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, Class<?> clazz) {
		return bulkIndex(queries, bulkOptions, getIndexCoordinatesFor(clazz));
	}

	@Override
	public final List<IndexedObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions,
			IndexCoordinates index) {

		Assert.notNull(queries, "List of IndexQuery must not be null");
		Assert.notNull(bulkOptions, "BulkOptions must not be null");

		return bulkOperation(queries, bulkOptions, index);
	}

	@Override
	public void bulkUpdate(List<UpdateQuery> queries, Class<?> clazz) {
		bulkUpdate(queries, getIndexCoordinatesFor(clazz));
	}

	public List<IndexedObjectInformation> bulkOperation(List<?> queries, BulkOptions bulkOptions,
			IndexCoordinates index) {

		Assert.notNull(queries, "List of IndexQuery must not be null");
		Assert.notNull(bulkOptions, "BulkOptions must not be null");

		maybeCallbackBeforeConvertWithQueries(queries, index);

		List<IndexedObjectInformation> indexedObjectInformations = doBulkOperation(queries, bulkOptions, index);

		maybeCallbackAfterSaveWithQueries(queries, index);

		return indexedObjectInformations;
	}

	public abstract List<IndexedObjectInformation> doBulkOperation(List<?> queries, BulkOptions bulkOptions,
			IndexCoordinates index);

	/**
	 * Pre process the write request before it is sent to the server, eg. by setting the
	 * {@link WriteRequest#setRefreshPolicy(String) refresh policy} if applicable.
	 *
	 * @param request must not be {@literal null}.
	 * @param <R>
	 * @return the processed {@link WriteRequest}.
	 */
	protected <R extends WriteRequest<R>> R prepareWriteRequest(R request) {

		if (refreshPolicy == null) {
			return request;
		}

		return request.setRefreshPolicy(RequestFactory.toElasticsearchRefreshPolicy(refreshPolicy));
	}

	/**
	 * Pre process the write request before it is sent to the server, eg. by setting the
	 * {@link WriteRequest#setRefreshPolicy(String) refresh policy} if applicable.
	 *
	 * @param requestBuilder must not be {@literal null}.
	 * @param <R>
	 * @return the processed {@link WriteRequest}.
	 */
	protected <R extends WriteRequestBuilder<R>> R prepareWriteRequestBuilder(R requestBuilder) {

		if (refreshPolicy == null) {
			return requestBuilder;
		}

		return requestBuilder.setRefreshPolicy(RequestFactory.toElasticsearchRefreshPolicy(refreshPolicy));
	}

	// endregion

	// region SearchOperations
	@Override
	public long count(Query query, Class<?> clazz) {
		return count(query, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz) {
		return searchForStream(query, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, IndexCoordinates index) {

		long scrollTimeInMillis = TimeValue.timeValueMinutes(1).millis();

		// noinspection ConstantConditions
		int maxCount = query.isLimiting() ? query.getMaxResults() : 0;

		return StreamQueries.streamResults( //
				maxCount, //
				searchScrollStart(scrollTimeInMillis, query, clazz, index), //
				scrollId -> searchScrollContinue(scrollId, scrollTimeInMillis, clazz, index), //
				this::searchScrollClear);
	}

	@Override
	public <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz) {
		return search(query, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz, IndexCoordinates index) {

		Assert.notNull(query.getId(), "No document id defined for MoreLikeThisQuery");

		MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = requestFactory.moreLikeThisQueryBuilder(query, index);
		return search(new NativeSearchQueryBuilder().withQuery(moreLikeThisQueryBuilder).build(), clazz, index);
	}

	@Override
	public <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz) {
		return multiSearch(queries, clazz, getIndexCoordinatesFor(clazz));
	}

	@Override
	public <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz, IndexCoordinates index) {
		MultiSearchRequest request = new MultiSearchRequest();
		for (Query query : queries) {
			request.add(requestFactory.searchRequest(query, clazz, index));
		}

		MultiSearchResponse.Item[] items = getMultiSearchResult(request);

		SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<>(clazz, index);
		List<SearchHits<T>> res = new ArrayList<>(queries.size());
		int c = 0;
		for (Query query : queries) {
			res.add(callback.doWith(SearchDocumentResponse.from(items[c++].getResponse())));
		}
		return res;
	}

	@Override
	public List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes) {

		Assert.notNull(queries, "queries must not be null");
		Assert.notNull(classes, "classes must not be null");
		Assert.isTrue(queries.size() == classes.size(), "queries and classes must have the same size");

		MultiSearchRequest request = new MultiSearchRequest();
		Iterator<Class<?>> it = classes.iterator();
		for (Query query : queries) {
			Class<?> clazz = it.next();
			request.add(requestFactory.searchRequest(query, clazz, getIndexCoordinatesFor(clazz)));
		}

		MultiSearchResponse.Item[] items = getMultiSearchResult(request);

		List<SearchHits<?>> res = new ArrayList<>(queries.size());
		int c = 0;
		Iterator<Class<?>> it1 = classes.iterator();
		for (Query query : queries) {
			Class entityClass = it1.next();

			SearchDocumentResponseCallback<SearchHits<?>> callback = new ReadSearchDocumentResponseCallback<>(entityClass,
					getIndexCoordinatesFor(entityClass));

			SearchResponse response = items[c++].getResponse();
			res.add(callback.doWith(SearchDocumentResponse.from(response)));
		}
		return res;
	}

	@Override
	public List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes,
			IndexCoordinates index) {

		Assert.notNull(queries, "queries must not be null");
		Assert.notNull(classes, "classes must not be null");
		Assert.notNull(index, "index must not be null");
		Assert.isTrue(queries.size() == classes.size(), "queries and classes must have the same size");

		MultiSearchRequest request = new MultiSearchRequest();
		Iterator<Class<?>> it = classes.iterator();
		for (Query query : queries) {
			request.add(requestFactory.searchRequest(query, it.next(), index));
		}

		MultiSearchResponse.Item[] items = getMultiSearchResult(request);

		List<SearchHits<?>> res = new ArrayList<>(queries.size());
		int c = 0;
		Iterator<Class<?>> it1 = classes.iterator();
		for (Query query : queries) {
			Class entityClass = it1.next();

			SearchDocumentResponseCallback<SearchHits<?>> callback = new ReadSearchDocumentResponseCallback<>(entityClass,
					index);

			SearchResponse response = items[c++].getResponse();
			res.add(callback.doWith(SearchDocumentResponse.from(response)));
		}
		return res;
	}

	@Override
	public <T> SearchHits<T> search(Query query, Class<T> clazz) {
		return search(query, clazz, getIndexCoordinatesFor(clazz));
	}

	/*
	 * internal use only, not for public API
	 */
	abstract protected <T> SearchScrollHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz,
			IndexCoordinates index);

	/*
	 * internal use only, not for public API
	 */
	abstract protected <T> SearchScrollHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis,
			Class<T> clazz, IndexCoordinates index);

	/*
	 * internal use only, not for public API
	 */
	protected void searchScrollClear(String scrollId) {
		searchScrollClear(Collections.singletonList(scrollId));
	}

	/*
	 * internal use only, not for public API
	 */
	abstract protected void searchScrollClear(List<String> scrollIds);

	abstract protected MultiSearchResponse.Item[] getMultiSearchResult(MultiSearchRequest request);

	@Override
	public SearchResponse suggest(SuggestBuilder suggestion, Class<?> clazz) {
		return suggest(suggestion, getIndexCoordinatesFor(clazz));
	}

	// endregion

	// region Helper methods
	@Override
	public ElasticsearchConverter getElasticsearchConverter() {

		Assert.notNull(elasticsearchConverter, "elasticsearchConverter is not initialized.");

		return elasticsearchConverter;
	}

	/**
	 * @since 4.0
	 */
	public RequestFactory getRequestFactory() {

		Assert.notNull(requestFactory, "requestfactory not initialized");

		return requestFactory;
	}

	protected static String[] toArray(List<String> values) {
		String[] valuesAsArray = new String[values.size()];
		return values.toArray(valuesAsArray);
	}

	/**
	 * @param clazz the entity class
	 * @return the IndexCoordinates defined on the entity.
	 * @since 4.0
	 */
	@Override
	public IndexCoordinates getIndexCoordinatesFor(Class<?> clazz) {
		return getRequiredPersistentEntity(clazz).getIndexCoordinates();
	}

	/**
	 * @param bulkResponse
	 * @return the list of the item id's
	 */
	protected List<IndexedObjectInformation> checkForBulkOperationFailure(BulkResponse bulkResponse) {

		if (bulkResponse.hasFailures()) {
			Map<String, String> failedDocuments = new HashMap<>();
			for (BulkItemResponse item : bulkResponse.getItems()) {

				if (item.isFailed())
					failedDocuments.put(item.getId(), item.getFailureMessage());
			}
			throw new BulkFailureException(
					"Bulk operation has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages ["
							+ failedDocuments + ']',
					failedDocuments);
		}

		return Stream.of(bulkResponse.getItems()).map(bulkItemResponse -> {
			DocWriteResponse response = bulkItemResponse.getResponse();
			if (response != null) {
				return IndexedObjectInformation.of(response.getId(), response.getSeqNo(), response.getPrimaryTerm(),
						response.getVersion());
			} else {
				return IndexedObjectInformation.of(bulkItemResponse.getId(), null, null, null);
			}

		}).collect(Collectors.toList());
	}

	protected void updateIndexedObject(Object entity, IndexedObjectInformation indexedObjectInformation) {

		ElasticsearchPersistentEntity<?> persistentEntity = elasticsearchConverter.getMappingContext()
				.getPersistentEntity(entity.getClass());

		if (persistentEntity != null) {
			PersistentPropertyAccessor<Object> propertyAccessor = persistentEntity.getPropertyAccessor(entity);
			ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

			// Only deal with text because ES generated Ids are strings!
			if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
				propertyAccessor.setProperty(idProperty, indexedObjectInformation.getId());
			}

			if (indexedObjectInformation.getSeqNo() != null && indexedObjectInformation.getPrimaryTerm() != null
					&& persistentEntity.hasSeqNoPrimaryTermProperty()) {
				ElasticsearchPersistentProperty seqNoPrimaryTermProperty = persistentEntity.getSeqNoPrimaryTermProperty();
				propertyAccessor.setProperty(seqNoPrimaryTermProperty,
						new SeqNoPrimaryTerm(indexedObjectInformation.getSeqNo(), indexedObjectInformation.getPrimaryTerm()));
			}

			if (indexedObjectInformation.getVersion() != null && persistentEntity.hasVersionProperty()) {
				ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();
				propertyAccessor.setProperty(versionProperty, indexedObjectInformation.getVersion());
			}
		}
	}

	ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
		return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
	}

	@Nullable
	private String getEntityId(Object entity) {

		Object id = entityOperations.forEntity(entity, elasticsearchConverter.getConversionService(), routingResolver)
				.getId();

		if (id != null) {
			return stringIdRepresentation(id);
		}

		return null;
	}

	@Nullable
	public String getEntityRouting(Object entity) {
		return entityOperations.forEntity(entity, elasticsearchConverter.getConversionService(), routingResolver)
				.getRouting();
	}

	@Nullable
	private Long getEntityVersion(Object entity) {

		Number version = entityOperations.forEntity(entity, elasticsearchConverter.getConversionService(), routingResolver)
				.getVersion();

		if (version != null && Long.class.isAssignableFrom(version.getClass())) {
			return ((Long) version);
		}

		return null;
	}

	@Nullable
	private SeqNoPrimaryTerm getEntitySeqNoPrimaryTerm(Object entity) {

		EntityOperations.AdaptibleEntity<Object> adaptibleEntity = entityOperations.forEntity(entity,
				elasticsearchConverter.getConversionService(), routingResolver);
		return adaptibleEntity.hasSeqNoPrimaryTerm() ? adaptibleEntity.getSeqNoPrimaryTerm() : null;
	}

	private <T> IndexQuery getIndexQuery(T entity) {

		String id = getEntityId(entity);

		if (id != null) {
			id = elasticsearchConverter.convertId(id);
		}

		IndexQueryBuilder builder = new IndexQueryBuilder() //
				.withId(id) //
				.withObject(entity);

		SeqNoPrimaryTerm seqNoPrimaryTerm = getEntitySeqNoPrimaryTerm(entity);

		if (seqNoPrimaryTerm != null) {
			builder.withSeqNoPrimaryTerm(seqNoPrimaryTerm);
		} else {
			// version cannot be used together with seq_no and primary_term
			builder.withVersion(getEntityVersion(entity));
		}

		String routing = getEntityRouting(entity);

		if (routing != null) {
			builder.withRouting(routing);
		}

		return builder.build();
	}

	/**
	 * tries to extract the version of the Elasticsearch cluster
	 *
	 * @return the version as string if it can be retrieved
	 */
	@Nullable
	abstract protected String getClusterVersion();

	// endregion

	// region Entity callbacks
	protected <T> T maybeCallbackBeforeConvert(T entity, IndexCoordinates index) {

		if (entityCallbacks != null) {
			return entityCallbacks.callback(BeforeConvertCallback.class, entity, index);
		}

		return entity;
	}

	protected void maybeCallbackBeforeConvertWithQuery(Object query, IndexCoordinates index) {

		if (query instanceof IndexQuery) {
			IndexQuery indexQuery = (IndexQuery) query;
			Object queryObject = indexQuery.getObject();

			if (queryObject != null) {
				queryObject = maybeCallbackBeforeConvert(queryObject, index);
				indexQuery.setObject(queryObject);
				// the callback might have set som values relevant for the IndexQuery
				IndexQuery newQuery = getIndexQuery(queryObject);

				if (indexQuery.getRouting() == null && newQuery.getRouting() != null) {
					indexQuery.setRouting(newQuery.getRouting());
				}

				if (indexQuery.getSeqNo() == null && newQuery.getSeqNo() != null) {
					indexQuery.setSeqNo(newQuery.getSeqNo());
				}

				if (indexQuery.getPrimaryTerm() == null && newQuery.getPrimaryTerm() != null) {
					indexQuery.setPrimaryTerm(newQuery.getPrimaryTerm());
				}
			}
		}
	}

	// this can be called with either a List<IndexQuery> or a List<UpdateQuery>; these query classes
	// don't have a common base class, therefore the List<?> argument
	protected void maybeCallbackBeforeConvertWithQueries(List<?> queries, IndexCoordinates index) {
		queries.forEach(query -> maybeCallbackBeforeConvertWithQuery(query, index));
	}

	protected <T> T maybeCallbackAfterSave(T entity, IndexCoordinates index) {

		if (entityCallbacks != null) {
			return entityCallbacks.callback(AfterSaveCallback.class, entity, index);
		}

		return entity;
	}

	protected void maybeCallbackAfterSaveWithQuery(Object query, IndexCoordinates index) {

		if (query instanceof IndexQuery) {
			IndexQuery indexQuery = (IndexQuery) query;
			Object queryObject = indexQuery.getObject();

			if (queryObject != null) {
				queryObject = maybeCallbackAfterSave(queryObject, index);
				indexQuery.setObject(queryObject);
			}
		}
	}

	// this can be called with either a List<IndexQuery> or a List<UpdateQuery>; these query classes
	// don't have a common base class, therefore the List<?> argument
	protected void maybeCallbackAfterSaveWithQueries(List<?> queries, IndexCoordinates index) {
		queries.forEach(query -> maybeCallbackAfterSaveWithQuery(query, index));
	}

	protected <T> T maybeCallbackAfterConvert(T entity, Document document, IndexCoordinates index) {

		if (entityCallbacks != null) {
			return entityCallbacks.callback(AfterConvertCallback.class, entity, document, index);
		}

		return entity;
	}

	// endregion

	protected void updateIndexedObjectsWithQueries(List<?> queries,
			List<IndexedObjectInformation> indexedObjectInformations) {
		for (int i = 0; i < queries.size(); i++) {
			Object query = queries.get(i);
			if (query instanceof IndexQuery) {
				IndexQuery indexQuery = (IndexQuery) query;
				Object queryObject = indexQuery.getObject();
				if (queryObject != null) {
					updateIndexedObject(queryObject, indexedObjectInformations.get(i));
				}
			}
		}
	}

	// region Document callbacks
	protected interface DocumentCallback<T> {
		@Nullable
		T doWith(@Nullable Document document);
	}

	protected class ReadDocumentCallback<T> implements DocumentCallback<T> {
		private final EntityReader<? super T, Document> reader;
		private final Class<T> type;
		private final IndexCoordinates index;

		public ReadDocumentCallback(EntityReader<? super T, Document> reader, Class<T> type, IndexCoordinates index) {

			Assert.notNull(reader, "reader is null");
			Assert.notNull(type, "type is null");

			this.reader = reader;
			this.type = type;
			this.index = index;
		}

		@Nullable
		public T doWith(@Nullable Document document) {

			if (document == null) {
				return null;
			}

			T entity = reader.read(type, document);
			return maybeCallbackAfterConvert(entity, document, index);
		}
	}

	protected interface SearchDocumentResponseCallback<T> {
		@NonNull
		T doWith(@NonNull SearchDocumentResponse response);
	}

	protected class ReadSearchDocumentResponseCallback<T> implements SearchDocumentResponseCallback<SearchHits<T>> {
		private final DocumentCallback<T> delegate;
		private final Class<T> type;

		public ReadSearchDocumentResponseCallback(Class<T> type, IndexCoordinates index) {

			Assert.notNull(type, "type is null");

			this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type, index);
			this.type = type;
		}

		@Override
		public SearchHits<T> doWith(SearchDocumentResponse response) {
			List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
			return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapHits(response, entities);
		}
	}

	protected class ReadSearchScrollDocumentResponseCallback<T>
			implements SearchDocumentResponseCallback<SearchScrollHits<T>> {
		private final DocumentCallback<T> delegate;
		private final Class<T> type;

		public ReadSearchScrollDocumentResponseCallback(Class<T> type, IndexCoordinates index) {

			Assert.notNull(type, "type is null");

			this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type, index);
			this.type = type;
		}

		@Override
		public SearchScrollHits<T> doWith(SearchDocumentResponse response) {
			List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
			return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapScrollHits(response, entities);
		}
	}
	// endregion

	// region routing
	private void setRoutingResolver(RoutingResolver routingResolver) {

		Assert.notNull(routingResolver, "routingResolver must not be null");

		this.routingResolver = routingResolver;
	}

	@Override
	public ElasticsearchOperations withRouting(RoutingResolver routingResolver) {

		Assert.notNull(routingResolver, "routingResolver must not be null");

		com.lizhen.elasticsearch.core.AbstractElasticsearchTemplate copy = copy();
		copy.setRoutingResolver(routingResolver);
		return copy;
	}

	// endregion
}
