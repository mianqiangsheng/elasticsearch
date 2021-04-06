/*
 * Copyright 2020-2021 the original author or authors.
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
package com.lizhen.elasticsearch.core.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.lizhen.elasticsearch.core.geo.GeoJson;
import com.lizhen.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.springframework.util.Assert;

/**
 * {@link com.lizhen.elasticsearch.core.geo.GeoJsonLineString} is defined as list of {@link Point}s.<br/>
 * Copied from Spring Data Mongodb
 *
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 * @since 4.1
 * @see <a href="https://geojson.org/geojson-spec.html#multipoint">https://geojson.org/geojson-spec.html#multipoint</a>
 */
public class GeoJsonLineString implements GeoJson<Iterable<Point>> {

	public static final String TYPE = "LineString";

	private final List<Point> points;

	private GeoJsonLineString(List<Point> points) {
		this.points = new ArrayList<>(points);
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonLineString} for the given {@link Point}s.
	 *
	 * @param points points must not be {@literal null} and have at least 2 entries.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonLineString of(List<Point> points) {

		Assert.notNull(points, "Points must not be null.");
		Assert.isTrue(points.size() >= 2, "Minimum of 2 Points required.");

		return new com.lizhen.elasticsearch.core.geo.GeoJsonLineString(points);
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonLineString} for the given {@link Point}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param others must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonLineString of(Point first, Point second, Point... others) {

		Assert.notNull(first, "First point must not be null!");
		Assert.notNull(second, "Second point must not be null!");
		Assert.notNull(others, "Additional points must not be null!");

		List<Point> points = new ArrayList<>();
		points.add(first);
		points.add(second);
		points.addAll(Arrays.asList(others));

		return new com.lizhen.elasticsearch.core.geo.GeoJsonLineString(points);
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonLineString} for the given {@link GeoPoint}s.
	 *
	 * @param geoPoints geoPoints must not be {@literal null} and have at least 2 entries.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonLineString ofGeoPoints(List<GeoPoint> geoPoints) {

		Assert.notNull(geoPoints, "Points must not be null.");
		Assert.isTrue(geoPoints.size() >= 2, "Minimum of 2 Points required.");

		return new com.lizhen.elasticsearch.core.geo.GeoJsonLineString(geoPoints.stream().map(GeoPoint::toPoint).collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonLineString} for the given {@link GeoPoint}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param others must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonLineString of(GeoPoint first, GeoPoint second, GeoPoint... others) {

		Assert.notNull(first, "First point must not be null!");
		Assert.notNull(second, "Second point must not be null!");
		Assert.notNull(others, "Additional points must not be null!");

		List<Point> points = new ArrayList<>();
		points.add(GeoPoint.toPoint(first));
		points.add(GeoPoint.toPoint(second));
		points.addAll(Arrays.stream(others).map(GeoPoint::toPoint).collect(Collectors.toList()));

		return new com.lizhen.elasticsearch.core.geo.GeoJsonLineString(points);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<Point> getCoordinates() {
		return Collections.unmodifiableList(this.points);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		com.lizhen.elasticsearch.core.geo.GeoJsonLineString that = (com.lizhen.elasticsearch.core.geo.GeoJsonLineString) o;

		return points.equals(that.points);
	}

	@Override
	public int hashCode() {
		return points.hashCode();
	}

	@Override
	public String toString() {
		return "GeoJsonLineString{" + "points=" + points + '}';
	}
}
