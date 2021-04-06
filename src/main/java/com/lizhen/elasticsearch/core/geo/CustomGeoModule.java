package com.lizhen.elasticsearch.core.geo;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lizhen.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;

/**
 * @author Artur Konaczak
 * @deprecated since 4.1, not used anymore
 */
@Deprecated
public class CustomGeoModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@link com.lizhen.elasticsearch.core.geo.CustomGeoModule} registering serializers and deserializers for spring data commons geo-spatial types.
	 */
	public CustomGeoModule() {

		super("Spring Data Elasticsearch Geo", new Version(1, 0, 0, null, "com.lizhen.elasticsearch", "spring-data-elasticsearch-geo"));
		addSerializer(Point.class, new com.lizhen.elasticsearch.core.geo.PointSerializer());
		addDeserializer(Point.class, new com.lizhen.elasticsearch.core.geo.PointDeserializer());
	}
}

class PointSerializer extends JsonSerializer<Point> {
	@Override
	public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeObject(GeoPoint.fromPoint(value));
	}
}

class PointDeserializer extends JsonDeserializer<Point> {
	@Override
	public Point deserialize(JsonParser p, DeserializationContext context) throws IOException {
		return GeoPoint.toPoint(p.readValueAs(GeoPoint.class));
	}
}
