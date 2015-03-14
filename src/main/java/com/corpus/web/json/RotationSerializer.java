package com.corpus.web.json;

import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * {@link JsonSerializer} for the {@link Rotation}.
 * 
 * @author Scopus
 * 
 */
public class RotationSerializer extends JsonSerializer<Rotation> {

	@Override
	public void serialize(Rotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("w", JsonStatics.roundDown4(value.getQ0()));
		jgen.writeNumberField("x", JsonStatics.roundDown4(value.getQ1()));
		jgen.writeNumberField("y", JsonStatics.roundDown4(value.getQ2()));
		jgen.writeNumberField("z", JsonStatics.roundDown4(value.getQ3()));
		jgen.writeEndObject();
	}

}
