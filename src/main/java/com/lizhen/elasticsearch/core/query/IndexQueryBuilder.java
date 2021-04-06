/*
 * Copyright 2014-2021 the original author or authors.
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

import com.lizhen.elasticsearch.core.RefreshPolicy;
import com.lizhen.elasticsearch.core.query.IndexQuery;
import com.lizhen.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.lang.Nullable;

/**
 * IndexQuery Builder
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 * @author Subhobrata Dey
 */
public class IndexQueryBuilder {

	@Nullable private String id;
	@Nullable private Object object;
	@Nullable private Long version;
	@Nullable private String source;
	@Deprecated @Nullable private String parentId;
	@Nullable private Long seqNo;
	@Nullable private Long primaryTerm;
	@Nullable private String routing;
	@Nullable private IndexQuery.OpType opType;
	@Nullable private RefreshPolicy refreshPolicy;

	public IndexQueryBuilder() {}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withId(String id) {
		this.id = id;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withObject(Object object) {
		this.object = object;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withVersion(Long version) {
		this.version = version;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withSource(String source) {
		this.source = source;
		return this;
	}

	@Deprecated
	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withParentId(String parentId) {
		this.parentId = parentId;
		return this;
	}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withSeqNoPrimaryTerm(SeqNoPrimaryTerm seqNoPrimaryTerm) {
		this.seqNo = seqNoPrimaryTerm.getSequenceNumber();
		this.primaryTerm = seqNoPrimaryTerm.getPrimaryTerm();
		return this;
	}

	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withRouting(@Nullable String routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * @since 4.2
	 */
	public com.lizhen.elasticsearch.core.query.IndexQueryBuilder withOpType(IndexQuery.OpType opType) {
		this.opType = opType;
		return this;
	}

	public IndexQuery build() {
		return new IndexQuery(id, object, version, source, parentId, seqNo, primaryTerm, routing, opType);
	}
}
