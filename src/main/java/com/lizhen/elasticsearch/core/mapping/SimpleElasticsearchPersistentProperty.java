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
package com.lizhen.elasticsearch.core.mapping;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lizhen.elasticsearch.annotations.DateFormat;
import com.lizhen.elasticsearch.annotations.Field;
import com.lizhen.elasticsearch.annotations.FieldType;
import com.lizhen.elasticsearch.annotations.GeoPointField;
import com.lizhen.elasticsearch.annotations.GeoShapeField;
import com.lizhen.elasticsearch.annotations.MultiField;
import com.lizhen.elasticsearch.annotations.Parent;
import com.lizhen.elasticsearch.core.completion.Completion;
import com.lizhen.elasticsearch.core.convert.ConversionException;
import com.lizhen.elasticsearch.core.convert.ElasticsearchDateConverter;
import com.lizhen.elasticsearch.core.geo.GeoJson;
import com.lizhen.elasticsearch.core.geo.GeoPoint;
import com.lizhen.elasticsearch.core.join.JoinField;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.lizhen.elasticsearch.core.mapping.ElasticsearchPersistentPropertyConverter;
import com.lizhen.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Elasticsearch specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 * @author Sascha Woo
 * @author Oliver Gierke
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 */
public class SimpleElasticsearchPersistentProperty extends
		AnnotationBasedPersistentProperty<ElasticsearchPersistentProperty> implements ElasticsearchPersistentProperty {

	private static final Logger LOGGER = LoggerFactory.getLogger(com.lizhen.elasticsearch.core.mapping.SimpleElasticsearchPersistentProperty.class);

	private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id", "document");

	private final boolean isParent;
	private final boolean isId;
	private final boolean isSeqNoPrimaryTerm;
	private final @Nullable String annotatedFieldName;
	@Nullable private ElasticsearchPersistentPropertyConverter propertyConverter;
	private final boolean storeNullValue;
	private final FieldNamingStrategy fieldNamingStrategy;

	public SimpleElasticsearchPersistentProperty(Property property,
			PersistentEntity<?, ElasticsearchPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder,
			@Nullable FieldNamingStrategy fieldNamingStrategy) {

		super(property, owner, simpleTypeHolder);

		this.annotatedFieldName = getAnnotatedFieldName();
		this.fieldNamingStrategy = fieldNamingStrategy == null ? PropertyNameFieldNamingStrategy.INSTANCE
				: fieldNamingStrategy;
		this.isId = super.isIdProperty()
				|| (SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName()) && !hasExplicitFieldName());
		this.isParent = isAnnotationPresent(Parent.class);
		this.isSeqNoPrimaryTerm = SeqNoPrimaryTerm.class.isAssignableFrom(getRawType());

		boolean isField = isAnnotationPresent(Field.class);

		if (isVersionProperty() && !getType().equals(Long.class)) {
			throw new MappingException(String.format("Version property %s must be of type Long!", property.getName()));
		}

		if (isParent && !getType().equals(String.class)) {
			throw new MappingException(String.format("Parent property %s must be of type String!", property.getName()));
		}

		if (isField && isAnnotationPresent(MultiField.class)) {
			throw new MappingException("@Field annotation must not be used on a @MultiField property.");
		}

		initDateConverter();

		storeNullValue = isField && getRequiredAnnotation(Field.class).storeNullValue();
	}

	@Override
	public boolean hasPropertyConverter() {
		return propertyConverter != null;
	}

	@Nullable
	@Override
	public ElasticsearchPersistentPropertyConverter getPropertyConverter() {
		return propertyConverter;
	}

	@Override
	public boolean isWritable() {
		return super.isWritable() && !isSeqNoPrimaryTermProperty();
	}

	@Override
	public boolean isReadable() {
		return !isTransient() && !isSeqNoPrimaryTermProperty();
	}

	@Override
	public boolean storeNullValue() {
		return storeNullValue;
	}

	protected boolean hasExplicitFieldName() {
		return StringUtils.hasText(getAnnotatedFieldName());
	}

	/**
	 * Initializes an {@link ElasticsearchPersistentPropertyConverter} if this property is annotated as a Field with type
	 * {@link FieldType#Date}, has a {@link DateFormat} set and if the type of the property is one of the Java8 temporal
	 * classes or java.util.Date.
	 */
	private void initDateConverter() {
		Field field = findAnnotation(Field.class);

		Class<?> actualType = getActualTypeOrNull();

		if (actualType == null) {
			return;
		}

		boolean isTemporalAccessor = TemporalAccessor.class.isAssignableFrom(actualType);
		boolean isDate = Date.class.isAssignableFrom(actualType);

		if (field != null && (field.type() == FieldType.Date || field.type() == FieldType.Date_Nanos)
				&& (isTemporalAccessor || isDate)) {

			DateFormat[] dateFormats = field.format();
			String[] dateFormatPatterns = field.pattern();

			String property = getOwner().getType().getSimpleName() + "." + getName();

			if (dateFormats.length == 0 && dateFormatPatterns.length == 0) {
				LOGGER.warn(
						"Property '{}' has @Field type '{}' but has no built-in format or custom date pattern defined. Make sure you have a converter registered for type {}.",
						property, field.type().name(), actualType.getSimpleName());
				return;
			}

			List<ElasticsearchDateConverter> converters = new ArrayList<>();

			// register converters for built-in formats
			for (DateFormat dateFormat : dateFormats) {
				switch (dateFormat) {
					case none:
					case custom:
						break;
					case weekyear:
					case weekyear_week:
					case weekyear_week_day:
						LOGGER.warn("No default converter available for '{}' and date format '{}'. Use a custom converter instead.",
								actualType.getName(), dateFormat.name());
						break;
					default:
						converters.add(ElasticsearchDateConverter.of(dateFormat));
						break;
				}
			}

			// register converters for custom formats
			for (String dateFormatPattern : dateFormatPatterns) {
				if (!StringUtils.hasText(dateFormatPattern)) {
					throw new MappingException(String.format("Date pattern of property '%s' must not be empty", property));
				}
				converters.add(ElasticsearchDateConverter.of(dateFormatPattern));
			}

			if (!converters.isEmpty()) {
				propertyConverter = new ElasticsearchPersistentPropertyConverter() {
					final List<ElasticsearchDateConverter> dateConverters = converters;

					@SuppressWarnings("unchecked")
					@Override
					public Object read(String s) {
						for (ElasticsearchDateConverter dateConverter : dateConverters) {
							try {
								if (isTemporalAccessor) {
									return dateConverter.parse(s, (Class<? extends TemporalAccessor>) actualType);
								} else { // must be date
									return dateConverter.parse(s);
								}
							} catch (Exception e) {
								LOGGER.trace(e.getMessage(), e);
							}
						}

						throw new ConversionException(String
								.format("Unable to parse date value '%s' of property '%s' with configured converters", s, property));
					}

					@Override
					public String write(Object property) {
						ElasticsearchDateConverter dateConverter = dateConverters.get(0);
						if (isTemporalAccessor && TemporalAccessor.class.isAssignableFrom(property.getClass())) {
							return dateConverter.format((TemporalAccessor) property);
						} else if (isDate && Date.class.isAssignableFrom(property.getClass())) {
							return dateConverter.format((Date) property);
						} else {
							return property.toString();
						}
					}
				};
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable
	private String getAnnotatedFieldName() {

		String name = null;

		if (isAnnotationPresent(Field.class)) {
			name = findAnnotation(Field.class).name();
		} else if (isAnnotationPresent(MultiField.class)) {
			name = findAnnotation(MultiField.class).mainField().name();
		}

		return StringUtils.hasText(name) ? name : null;
	}

	@Override
	public String getFieldName() {

		if (annotatedFieldName == null) {
			String fieldName = fieldNamingStrategy.getFieldName(this);

			if (!StringUtils.hasText(fieldName)) {
				throw new MappingException(String.format("Invalid (null or empty) field name returned for property %s by %s!",
						this, fieldNamingStrategy.getClass()));
			}

			return fieldName;
		}

		return annotatedFieldName;
	}

	@Override
	public boolean isIdProperty() {
		return isId;
	}

	@Override
	protected Association<ElasticsearchPersistentProperty> createAssociation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isImmutable() {
		return false;
	}

	@Override
	public boolean isParentProperty() {
		return isParent;
	}

	@Override
	public boolean isSeqNoPrimaryTermProperty() {
		return isSeqNoPrimaryTerm;
	}

	@Override
	public boolean isGeoPointProperty() {
		return getActualType() == GeoPoint.class || isAnnotationPresent(GeoPointField.class);
	}

	@Override
	public boolean isGeoShapeProperty() {
		return GeoJson.class.isAssignableFrom(getActualType()) || isAnnotationPresent(GeoShapeField.class);
	}

	@Override
	public boolean isJoinFieldProperty() {
		return getActualType() == JoinField.class;
	}

	@Override
	public boolean isCompletionProperty() {
		return getActualType() == Completion.class;
	}
}
