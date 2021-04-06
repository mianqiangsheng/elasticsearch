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
package com.lizhen.elasticsearch.query;

import com.lizhen.elasticsearch.Field;
import com.lizhen.elasticsearch.annotations.FieldType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The most trivial implementation of a Field. The {@link #name} is updatable, so it may be changed during query
 * preparation by the {@link com.lizhen.elasticsearch.convert.MappingElasticsearchConverter}.
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Peter-Josef Meisch
 */
public class SimpleField implements Field {

	private String name;
	@Nullable private FieldType fieldType;

	public SimpleField(String name) {

		Assert.hasText(name, "name must not be null");

		this.name = name;
	}

	@Override
	public void setName(String name) {

		Assert.hasText(name, "name must not be null");

		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	@Nullable
	@Override
	public FieldType getFieldType() {
		return fieldType;
	}

	@Override
	public String toString() {
		return getName();
	}
}
