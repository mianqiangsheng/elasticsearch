/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lizhen.elasticsearch.core.geo;

import com.lizhen.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Box;

/**
 * Geo bbox used for #{@link com.lizhen.elasticsearch.core.query.Criteria}.
 *
 * @author Franck Marchand
 */
public class GeoBox {

	private GeoPoint topLeft;
	private GeoPoint bottomRight;

	public GeoBox(GeoPoint topLeft, GeoPoint bottomRight) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}

	public GeoPoint getTopLeft() {
		return topLeft;
	}

	public GeoPoint getBottomRight() {
		return bottomRight;
	}

	/**
	 * return a {@link com.lizhen.elasticsearch.core.geo.GeoBox}
	 * from a {@link Box}.
	 *
	 * @param box {@link Box} to use
	 * @return a {@link com.lizhen.elasticsearch.core.geo.GeoBox}
	 */
	public static com.lizhen.elasticsearch.core.geo.GeoBox fromBox(Box box) {
		GeoPoint topLeft = GeoPoint.fromPoint(box.getFirst());
		GeoPoint bottomRight = GeoPoint.fromPoint(box.getSecond());

		return new com.lizhen.elasticsearch.core.geo.GeoBox(topLeft, bottomRight);
	}
}
