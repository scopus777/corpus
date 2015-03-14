package com.corpus.web.json;

import com.corpus.sensor.Sensor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class SensorFilter extends SimpleBeanPropertyFilter {
	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {

		// System.out.println(pojo.getClass().getName());

		// The property is not filtered
		if (!Sensor.class.isAssignableFrom(pojo.getClass()))
			writer.serializeAsField(pojo, jgen, provider);
	}

	@Override
	protected boolean include(BeanPropertyWriter writer) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean include(PropertyWriter writer) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean includeElement(Object elementValue) {
		System.out.println(elementValue.getClass().getName());
		return true;
	}
}