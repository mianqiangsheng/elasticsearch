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
package com.lizhen.elasticsearch.core.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import com.lizhen.elasticsearch.annotations.DateFormat;
import com.lizhen.elasticsearch.core.convert.ConversionException;
import org.springframework.util.Assert;

/**
 * Provides Converter instances to convert to and from Dates in the different date and time formats that elasticsearch
 * understands.
 *
 * @author Peter-Josef Meisch
 * @since 4.0
 */
final public class ElasticsearchDateConverter {

	private static final ConcurrentHashMap<String, com.lizhen.elasticsearch.core.convert.ElasticsearchDateConverter> converters = new ConcurrentHashMap<>();

	private final DateFormatter dateFormatter;

	/**
	 * Creates an ElasticsearchDateConverter for the given {@link DateFormat}.
	 * 
	 * @param dateFormat must not be @{literal null}
	 * @return converter
	 */
	public static com.lizhen.elasticsearch.core.convert.ElasticsearchDateConverter of(DateFormat dateFormat) {

		Assert.notNull(dateFormat, "dateFormat must not be null");

		return of(dateFormat.name());
	}

	/**
	 * Creates an ElasticsearchDateConverter for the given pattern.
	 *
	 * @param pattern must not be {@literal null}
	 * @return converter
	 */
	public static com.lizhen.elasticsearch.core.convert.ElasticsearchDateConverter of(String pattern) {

		Assert.notNull(pattern, "pattern must not be null");
		Assert.hasText(pattern, "pattern must not be empty");

		String[] subPatterns = pattern.split("\\|\\|");

		return converters.computeIfAbsent(subPatterns[0].trim(), p -> new com.lizhen.elasticsearch.core.convert.ElasticsearchDateConverter(forPattern(p)));
	}

	private ElasticsearchDateConverter(DateFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
	}

	/**
	 * Formats the given {@link TemporalAccessor} into a String.
	 *
	 * @param accessor must not be {@literal null}
	 * @return the formatted object
	 */
	public String format(TemporalAccessor accessor) {

		Assert.notNull("accessor", "accessor must not be null");

		if (accessor instanceof Instant) {
			Instant instant = (Instant) accessor;
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
			return dateFormatter.format(zonedDateTime);
		}

		return dateFormatter.format(accessor);
	}

	/**
	 * Formats the given {@link TemporalAccessor} int a String
	 *
	 * @param date must not be {@literal null}
	 * @return the formatted object
	 */
	public String format(Date date) {

		Assert.notNull(date, "accessor must not be null");

		return dateFormatter.format(Instant.ofEpochMilli(date.getTime()));
	}

	/**
	 * Parses a String into a TemporalAccessor.
	 *
	 * @param input the String to parse, must not be {@literal null}.
	 * @param type the class to return
	 * @param <T> the class of type
	 * @return the new created object
	 */
	public <T extends TemporalAccessor> T parse(String input, Class<T> type) {
		return dateFormatter.parse(input, type);
	}

	/**
	 * Parses a String into a Date.
	 *
	 * @param input the String to parse, must not be {@literal null}.
	 * @return the new created object
	 */
	public Date parse(String input) {
		return new Date(dateFormatter.parse(input, Instant.class).toEpochMilli());
	}

	/**
	 * Creates a {@link DateFormatter} for a given pattern. The pattern can be the name of a {@link DateFormat} enum value
	 * or a literal pattern.
	 * 
	 * @param pattern the pattern to use
	 * @return DateFormatter
	 */
	private static DateFormatter forPattern(String pattern) {

		String resolvedPattern = pattern;

		if (DateFormat.epoch_millis.getPattern().equals(pattern)) {
			return new EpochMillisDateFormatter();
		}

		if (DateFormat.epoch_second.getPattern().equals(pattern)) {
			return new EpochSecondDateFormatter();
		}

		// check the enum values
		for (DateFormat dateFormat : DateFormat.values()) {

			switch (dateFormat) {
				case weekyear:
				case weekyear_week:
				case weekyear_week_day:
				case custom:
					continue;
			}

			if (dateFormat.name().equals(pattern)) {
				resolvedPattern = dateFormat.getPattern();
				break;
			}
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(resolvedPattern);
		return new PatternDateFormatter(dateTimeFormatter);
	}

	private static <T extends TemporalAccessor> TemporalQuery<T> getTemporalQuery(Class<T> type) {
		return temporal -> {
			try {
				Method method = type.getMethod("from", TemporalAccessor.class);
				Object o = method.invoke(null, temporal);
				return type.cast(o);
			} catch (NoSuchMethodException e) {
				throw new ConversionException("no 'from' factory method found in class " + type.getName());
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new ConversionException("could not create object of class " + type.getName(), e);
			}
		};
	} // endregion

	/**
	 * a DateFormatter to convert epoch milliseconds
	 */
	static class EpochMillisDateFormatter implements DateFormatter {

		@Override
		public String format(TemporalAccessor accessor) {

			Assert.notNull(accessor, "accessor must not be null");

			return Long.toString(Instant.from(accessor).toEpochMilli());
		}

		@Override
		public <T extends TemporalAccessor> T parse(String input, Class<T> type) {

			Assert.notNull(input, "input must not be null");
			Assert.notNull(type, "type must not be null");

			Instant instant = Instant.ofEpochMilli(Long.parseLong(input));
			TemporalQuery<T> query = getTemporalQuery(type);
			return query.queryFrom(instant);
		}
	}

	/**
	 * a DateFormatter to convert epoch seconds. Elasticsearch's formatter uses double values, so do we
	 */
	static class EpochSecondDateFormatter implements DateFormatter {

		@Override
		public String format(TemporalAccessor accessor) {

			Assert.notNull(accessor, "accessor must not be null");

			long epochMilli = Instant.from(accessor).toEpochMilli();
			long fraction = epochMilli % 1_000;
			if (fraction == 0) {
				return Long.toString(epochMilli / 1_000);
			} else {
				Double d = ((double) epochMilli) / 1_000;
				return String.format(Locale.ROOT, "%.03f", d);
			}
		}

		@Override
		public <T extends TemporalAccessor> T parse(String input, Class<T> type) {

			Assert.notNull(input, "input must not be null");
			Assert.notNull(type, "type must not be null");

			Double epochMilli = Double.parseDouble(input) * 1_000;
			Instant instant = Instant.ofEpochMilli(epochMilli.longValue());
			TemporalQuery<T> query = getTemporalQuery(type);
			return query.queryFrom(instant);
		}
	}

	static class PatternDateFormatter implements DateFormatter {

		private final DateTimeFormatter dateTimeFormatter;

		PatternDateFormatter(DateTimeFormatter dateTimeFormatter) {
			this.dateTimeFormatter = dateTimeFormatter;
		}

		@Override
		public String format(TemporalAccessor accessor) {

			Assert.notNull(accessor, "accessor must not be null");

			try {
				return dateTimeFormatter.format(accessor);
			} catch (Exception e) {
				if (accessor instanceof Instant) {
					// as alternatives try to format a ZonedDateTime or LocalDateTime
					return dateTimeFormatter.format(ZonedDateTime.ofInstant((Instant) accessor, ZoneId.of("UTC")));
				} else {
					throw e;
				}
			}
		}

		@Override
		public <T extends TemporalAccessor> T parse(String input, Class<T> type) {

			Assert.notNull(input, "input must not be null");
			Assert.notNull(type, "type must not be null");

			try {
				return dateTimeFormatter.parse(input, getTemporalQuery(type));
			} catch (Exception e) {

				if (type.equals(Instant.class)) {
					// as alternatives try to parse a ZonedDateTime or LocalDateTime
					try {
						ZonedDateTime zonedDateTime = dateTimeFormatter.parse(input, getTemporalQuery(ZonedDateTime.class));
						// noinspection unchecked
						return (T) zonedDateTime.toInstant();
					} catch (Exception exception) {
						LocalDateTime localDateTime = dateTimeFormatter.parse(input, getTemporalQuery(LocalDateTime.class));
						// noinspection unchecked
						return (T) localDateTime.toInstant(ZoneOffset.UTC);
					}
				} else {
					throw e;
				}
			}
		}
	}
}
