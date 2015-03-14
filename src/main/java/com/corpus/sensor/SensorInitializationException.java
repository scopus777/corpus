package com.corpus.sensor;

/**
 * This Exception can be thrown as a response to an error during the
 * initialization process of a sensor.
 * 
 * @author Matthias Weise
 * 
 */
public class SensorInitializationException extends RuntimeException {

	private static final long serialVersionUID = 3977093150067860665L;

	public SensorInitializationException() {
		super();
	}

	public SensorInitializationException(String message) {
		super(message);
	}
}
