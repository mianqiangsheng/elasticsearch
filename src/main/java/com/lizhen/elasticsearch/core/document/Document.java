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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.lizhen.elasticsearch.core.convert.ConversionException;
import com.lizhen.elasticsearch.support.StringObjectMap;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A representation of a Elasticsearch document as extended {@link StringObjectMap Map}. All iterators preserve
 * original insertion order.
 * <p>
 * Document does not allow {@code null} keys. It allows {@literal null} values.
 * <p>
 * Implementing classes can bei either mutable or immutable. In case a subclass is immutable, its methods may throw
 * {@link UnsupportedOperationException} when calling modifying methods.
 *
 * @author Mark Paluch
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 * @since 4.0
 */
public interface Document extends StringObjectMap<com.lizhen.elasticsearch.core.document.Document> {

	/**
	 * Create a new mutable {@link com.lizhen.elasticsearch.core.document.Document}.
	 *
	 * @return a new {@link com.lizhen.elasticsearch.core.document.Document}.
	 */
	static com.lizhen.elasticsearch.core.document.Document create() {
		return new MapDocument();
	}

	/**
	 * Create a {@link com.lizhen.elasticsearch.core.document.Document} from a {@link Map} containing key-value pairs and sub-documents.
	 *
	 * @param map source map containing key-value pairs and sub-documents. must not be {@literal null}.
	 * @return a new {@link com.lizhen.elasticsearch.core.document.Document}.
	 */
	static com.lizhen.elasticsearch.core.document.Document from(Map<String, ? extends Object> map) {

		Assert.notNull(map, "Map must not be null");

		if (map instanceof LinkedHashMap) {
			return new MapDocument(map);
		}

		return new MapDocument(new LinkedHashMap<>(map));
	}

	/**
	 * Parse JSON to {@link com.lizhen.elasticsearch.core.document.Document}.
	 *
	 * @param json must not be {@literal null}.
	 * @return the parsed {@link com.lizhen.elasticsearch.core.document.Document}.
	 */
	static com.lizhen.elasticsearch.core.document.Document parse(String json) {

		Assert.notNull(json, "JSON must not be null");

		return new MapDocument().fromJson(json);
	}

	@Override
	default com.lizhen.elasticsearch.core.document.Document fromJson(String json) {
		Assert.notNull(json, "JSON must not be null");

		clear();
		try {
			putAll(MapDocument.OBJECT_MAPPER.readerFor(Map.class).readValue(json));
		} catch (IOException e) {
			throw new ConversionException("Cannot parse JSON", e);
		}
		return this;
	}

	/**
	 * Return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with an identifier.
	 *
	 * @return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with an identifier, {@literal false} otherwise.
	 */
	default boolean hasId() {
		return false;
	}

	/**
	 * @return the index if this document was retrieved from an index
	 * @since 4.1
	 */
	@Nullable
	default String getIndex() {
		return null;
	}

	/**
	 * Sets the index name for this document
	 *
	 * @param index index name
	 *          <p>
	 *          The default implementation throws {@link UnsupportedOperationException}.
	 * @since 4.1
	 */
	default void setIndex(@Nullable String index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve the identifier associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check {@link #hasId()}
	 * prior to calling this method.
	 *
	 * @return the identifier associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * @throws IllegalStateException if the underlying implementation supports Id's but no Id was yet associated with the
	 *           document.
	 */
	default String getId() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the identifier for this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 */
	default void setId(String id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a version.
	 *
	 * @return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a version, {@literal false} otherwise.
	 */
	default boolean hasVersion() {
		return false;
	}

	/**
	 * Retrieve the version associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
	 * {@link #hasVersion()} prior to calling this method.
	 *
	 * @return the version associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * @throws IllegalStateException if the underlying implementation supports Id's but no Id was yet associated with the
	 *           document.
	 */
	default long getVersion() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the version for this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 */
	default void setVersion(long version) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a seq_no.
	 *
	 * @return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a seq_no, {@literal false} otherwise.
	 */
	default boolean hasSeqNo() {
		return false;
	}

	/**
	 * Retrieve the seq_no associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
	 * {@link #hasSeqNo()} prior to calling this method.
	 *
	 * @return the seq_no associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * @throws IllegalStateException if the underlying implementation supports seq_no's but no seq_no was yet associated
	 *           with the document.
	 */
	default long getSeqNo() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the seq_no for this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 */
	default void setSeqNo(long seqNo) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a primary_term.
	 *
	 * @return {@literal true} if this {@link com.lizhen.elasticsearch.core.document.Document} is associated with a primary_term, {@literal false} otherwise.
	 */
	default boolean hasPrimaryTerm() {
		return false;
	}

	/**
	 * Retrieve the primary_term associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
	 * {@link #hasPrimaryTerm()} prior to calling this method.
	 *
	 * @return the primary_term associated with this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * @throws IllegalStateException if the underlying implementation supports primary_term's but no primary_term was yet
	 *           associated with the document.
	 */
	default long getPrimaryTerm() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the primary_term for this {@link com.lizhen.elasticsearch.core.document.Document}.
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 */
	default void setPrimaryTerm(long primaryTerm) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method allows the application of a function to {@code this} {@link com.lizhen.elasticsearch.core.document.Document}. The function should expect a
	 * single {@link com.lizhen.elasticsearch.core.document.Document} argument and produce an {@code R} result.
	 * <p>
	 * Any exception thrown by the function will be propagated to the caller.
	 *
	 * @param transformer functional interface to a apply. must not be {@literal null}.
	 * @param <R> class of the result
	 * @return the result of applying the function to this string
	 * @see Function
	 */
	default <R> R transform(Function<? super com.lizhen.elasticsearch.core.document.Document, ? extends R> transformer) {

		Assert.notNull(transformer, "transformer must not be null");

		return transformer.apply(this);
	}
}
