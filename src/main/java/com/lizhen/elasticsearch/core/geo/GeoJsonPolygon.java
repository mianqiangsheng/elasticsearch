/*
 * Copyright 2015-2021 the original author or authors.
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
import java.util.Iterator;
import java.util.List;

import com.lizhen.elasticsearch.core.geo.GeoJson;
import com.lizhen.elasticsearch.core.geo.GeoJsonLineString;
import com.lizhen.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.springframework.util.Assert;

/**
 * {@link GeoJson} representation of a polygon. <br/>
 * Copied from Spring Data Mongodb
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Peter-Josef Meisch
 * @since 4.1
 * @see <a href="https://geojson.org/geojson-spec.html#polygon">https://geojson.org/geojson-spec.html#polygon</a>
 */
public class GeoJsonPolygon implements GeoJson<Iterable<GeoJsonLineString>> {

	public static final String TYPE = "Polygon";

	private final List<GeoJsonLineString> coordinates = new ArrayList<>();

	private GeoJsonPolygon(GeoJsonLineString geoJsonLineString) {
		Assert.notNull(geoJsonLineString, "geoJsonLineString must not be null");
		Assert.isTrue(geoJsonLineString.getCoordinates().size() >= 4, "geoJsonLineString must have at least 4 points");

		this.coordinates.add(geoJsonLineString);
	}

	private GeoJsonPolygon(List<Point> points) {
		this(GeoJsonLineString.of(points));
	}

	/**
	 * Creates new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} from the given {@link GeoJsonLineString}.
	 *
	 * @param geoJsonLineString must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonPolygon of(GeoJsonLineString geoJsonLineString) {
		return new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(geoJsonLineString);
	}

	/**
	 * Creates new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} from the given {@link Point}s.
	 *
	 * @param points must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonPolygon of(List<Point> points) {
		return new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(points);
	}

	/**
	 * Creates new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} from the given {@link GeoPoint}s.
	 *
	 * @param geoPoints must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonPolygon ofGeoPoints(List<GeoPoint> geoPoints) {
		return new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(GeoJsonLineString.ofGeoPoints(geoPoints));
	}

	/**
	 * Creates new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} from the given {@link Point}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param third must not be {@literal null}.
	 * @param fourth must not be {@literal null}
	 * @param others can be empty.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonPolygon of(Point first, Point second, Point third, Point fourth, Point... others) {
		return new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(asList(first, second, third, fourth, others));
	}

	/**
	 * Creates new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} from the given {@link GeoPoint}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param third must not be {@literal null}.
	 * @param fourth must not be {@literal null}
	 * @param others can be empty.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonPolygon of(GeoPoint first, GeoPoint second, GeoPoint third, GeoPoint fourth,
                                                                                    GeoPoint... others) {
		return new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(GeoJsonLineString.ofGeoPoints(asList(first, second, third, fourth, others)));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} with an inner ring defined be the given {@link Point}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param third must not be {@literal null}.
	 * @param fourth must not be {@literal null}.
	 * @param others can be empty.
	 * @return new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon}.
	 */
	public com.lizhen.elasticsearch.core.geo.GeoJsonPolygon withInnerRing(Point first, Point second, Point third, Point fourth, Point... others) {
		return withInnerRing(asList(first, second, third, fourth, others));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} with an inner ring defined be the given {@link GeoPoint}s.
	 *
	 * @param first must not be {@literal null}.
	 * @param second must not be {@literal null}.
	 * @param third must not be {@literal null}.
	 * @param fourth must not be {@literal null}.
	 * @param others can be empty.
	 * @return new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon}.
	 */
	public com.lizhen.elasticsearch.core.geo.GeoJsonPolygon withInnerRing(GeoPoint first, GeoPoint second, GeoPoint third, GeoPoint fourth,
                                                                                        GeoPoint... others) {
		return withInnerRingOfGeoPoints(asList(first, second, third, fourth, others));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} with an inner ring defined be the given {@link List} of {@link Point}s.
	 *
	 * @param points must not be {@literal null}.
	 * @return new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon}.
	 */
	public com.lizhen.elasticsearch.core.geo.GeoJsonPolygon withInnerRing(List<Point> points) {
		return withInnerRing(GeoJsonLineString.of(points));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} with an inner ring defined be the given {@link List} of {@link GeoPoint}s.
	 *
	 * @param geoPoints must not be {@literal null}.
	 * @return new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon}.
	 */
	public com.lizhen.elasticsearch.core.geo.GeoJsonPolygon withInnerRingOfGeoPoints(List<GeoPoint> geoPoints) {
		return withInnerRing(GeoJsonLineString.ofGeoPoints(geoPoints));
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon} with an inner ring defined be the given {@link GeoJsonLineString}.
	 *
	 * @param lineString must not be {@literal null}.
	 * @return new {@link com.lizhen.elasticsearch.core.geo.GeoJsonPolygon}.
	 * @since 1.10
	 */
	public com.lizhen.elasticsearch.core.geo.GeoJsonPolygon withInnerRing(GeoJsonLineString lineString) {

		Assert.notNull(lineString, "LineString must not be null!");

		Iterator<GeoJsonLineString> it = this.coordinates.iterator();
		com.lizhen.elasticsearch.core.geo.GeoJsonPolygon polygon = new com.lizhen.elasticsearch.core.geo.GeoJsonPolygon(it.next().getCoordinates());

		while (it.hasNext()) {
			polygon.coordinates.add(it.next());
		}

		polygon.coordinates.add(lineString);
		return polygon;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<GeoJsonLineString> getCoordinates() {
		return Collections.unmodifiableList(this.coordinates);
	}

	@SafeVarargs
	private static <T> List<T> asList(T first, T second, T third, T fourth, T... others) {

		ArrayList<T> result = new ArrayList<>(3 + others.length);

		result.add(first);
		result.add(second);
		result.add(third);
		result.add(fourth);
		result.addAll(Arrays.asList(others));

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		com.lizhen.elasticsearch.core.geo.GeoJsonPolygon that = (com.lizhen.elasticsearch.core.geo.GeoJsonPolygon) o;

		return coordinates.equals(that.coordinates);
	}

	@Override
	public int hashCode() {
		return coordinates.hashCode();
	}

	@Override
	public String toString() {
		return "GeoJsonPolygon{" + "coordinates=" + coordinates + '}';
	}
}
