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
import java.util.Collections;
import java.util.List;

import com.lizhen.elasticsearch.core.geo.GeoJson;
import com.lizhen.elasticsearch.core.geo.GeoJsonPolygon;
import org.springframework.util.Assert;

/**
 * {@link com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon} is defined as a list of {@link GeoJsonPolygon}s.<br/>
 * Copied fro Spring Data Mongodb.
 *
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 * @since 4.1
 */
public class GeoJsonMultiPolygon implements GeoJson<Iterable<GeoJsonPolygon>> {

	public static final String TYPE = "MultiPolygon";

	private List<GeoJsonPolygon> coordinates = new ArrayList<GeoJsonPolygon>();

	private GeoJsonMultiPolygon(List<GeoJsonPolygon> polygons) {
		this.coordinates.addAll(polygons);
	}

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon} for the given {@link GeoJsonPolygon}s.
	 *
	 * @param polygons must not be {@literal null}.
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon of(List<GeoJsonPolygon> polygons) {

		Assert.notNull(polygons, "Polygons for MultiPolygon must not be null!");

		return new com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon(polygons);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<GeoJsonPolygon> getCoordinates() {
		return Collections.unmodifiableList(this.coordinates);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon that = (com.lizhen.elasticsearch.core.geo.GeoJsonMultiPolygon) o;

		return coordinates.equals(that.coordinates);
	}

	@Override
	public int hashCode() {
		return coordinates.hashCode();
	}

	@Override
	public String toString() {
		return "GeoJsonMultiPolygon{" + "coordinates=" + coordinates + '}';
	}
}
