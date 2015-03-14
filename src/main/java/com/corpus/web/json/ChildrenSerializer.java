package com.corpus.web.json;

import java.io.IOException;
import java.util.ArrayList;

import com.corpus.sensor.Sensor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <p>
 * Custom serializer to modify the JSON conversion of the <code>children</code>
 * field. Object of type {@link Sensor} will be ignored.
 * </p>
 * <p>
 * TODO: Maybe there is a better way to ignore objects of type {@link Sensor}
 * </p>
 * 
 * @author Matthias Weise
 * 
 */
@SuppressWarnings("rawtypes")
public class ChildrenSerializer extends JsonSerializer<ArrayList> {

	@Override
	public void serialize(ArrayList value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartArray();
		for (int i = 0; i < value.size(); i++) {
			if (!(value.get(i) instanceof Sensor))
				try {
					jgen.writeRaw(JsonCreator.hierarchicalMapper.writer(provider.getFilterProvider()).writeValueAsString(value.get(i)));
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			if (i != value.size() - 1)
				jgen.writeRaw(",");

		}
		jgen.writeEndArray();
	}

}
