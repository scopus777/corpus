package com.corpus.web.json;

import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * {@link JsonSerializer} for the {@link Vector3D}.
 * 
 * @author Matthias Weise
 * 
 */
public class Vector3DSerializer extends JsonSerializer<Vector3D> {

	@Override
	public void serialize(Vector3D value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("x", JsonStatics.roundDown4(value.getX()));
		jgen.writeNumberField("y", JsonStatics.roundDown4(value.getY()));
		jgen.writeNumberField("z", JsonStatics.roundDown4(value.getZ()));
		jgen.writeEndObject();
	}
}
