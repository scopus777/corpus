package com.corpus.sensor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.scene.SceneNode;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * <p>
 * Represents a Sensor tracking joints of a human. A Sensor extends the
 * {@link SceneNode} class and is therefore placed somewhere in the
 * {@link Scene}. There should be an implementation of this class for each
 * device tracking joints. A class extending this class is called
 * <i>wrapper</i>.
 * </p>
 * 
 * <p>
 * The {@link #init() init} method should be used to initialize the connection
 * to the sensor.
 * </p>
 * 
 * <p>
 * The <code>currentData</code> field contains the current data tracked by the
 * sensor. A wrapper implementing this class needs to map the tracked data to
 * {@link Joint}s supported by the framework. The supported human joints are
 * listed in the <code>enum</code> {@link JointType}.
 * </p>
 * 
 * <p>
 * This class implements the Runnable interface. The
 * {@link java.lang.Runnable#run() run} method can be used to communicate with
 * the device and to continuously poll the new data. It is also possible to
 * directly request the data from the sensor in the {@link #updateCurrentData()
 * updateCurrentData} method. This method will be called during the update
 * process in the {@link #getCurrentData() getCurrentData} method. It is also
 * possible to use a event-based system to update the current data. Anyways, the
 * <code>currentData</code> field has to be set by the {@link #setCurrentData()
 * setCurrentData} method whatever update system is used. Updating the date
 * during the update chain can heavily influence the needed time for the update
 * process. However, continuously polling the data or a event-based system can
 * produce unnecessary overhead.
 * </p>
 * 
 * <p>
 * If <code>positionConfidence</code> or <code>orientationConfidence</code> are
 * set through the configuration file, the wrapper should adopt these values for
 * the corresponding confidence values.
 * </p>
 * 
 * @author Matthias Weise
 * 
 */
@JsonFilter("filter")
@JsonIgnoreType
public abstract class Sensor extends SceneNode implements Runnable {

	protected Map<String, String> arguments;

	// default position - relative to the parent
	public static final Vector3D DEFAULT_POSITION = new Vector3D(0, 0, 0);
	// default orientation - relative to the parent
	public static final Rotation DEFAULT_ORIENTATION = new Rotation(1, 0, 0, 0, false);

	// determines whether the sensor collects absolute data or relative data the
	// the own position
	protected boolean collectsAbsoluteData;
	// determines the initialization state of the sensor
	private boolean isInitialized = false;

	// confidence values adjusted by the user. Should be used if the set.
	protected float positionConfidence;
	protected float orientationConfidence;

	// by the sensor tracked data
	private volatile Map<JointType, Joint> currentData;

	// id of the sensor
	private String id;

	// determines whether the object is a copy
	private boolean isCopy = false;

	/**
	 * Creates a sensor and places it in a scene.
	 * 
	 * @param position
	 *            position of the sensor.
	 * @param orientation
	 *            orientation of the sensor.
	 * @param parent
	 *            parent node of the sensor. The parent node will be used to
	 *            calculate the final position of the tracked joints.
	 * @param inferPosition
	 *            determines whether the position of this sensor should be
	 *            inferred. That means, that the data of another sensor is used
	 *            to infer the position of this sensor. A joint has to be
	 *            declared which is used for this process. This joint has to be
	 *            tracked by another sensor.
	 */
	public Sensor(Map<String, String> arguments) {
		super(DEFAULT_POSITION, DEFAULT_ORIENTATION, null);
		this.arguments = arguments;
		this.currentData = new HashMap<JointType, Joint>();
		this.id = UUID.randomUUID().toString();
		if (arguments.get("positionConfidence") != null)
			positionConfidence = Float.parseFloat(arguments.get("positionConfidence"));
		if (arguments.get("orientationConfidence") != null)
			orientationConfidence = Float.parseFloat(arguments.get("orientationConfidence"));
	}

	/**
	 * Tells whether the sensor collects absolute data.
	 * 
	 * @return collectsAbsoluteData
	 */
	public boolean getCollectsAbsoluteData() {
		return collectsAbsoluteData;
	}

	/**
	 * Returns the current data tracked by the sensor.
	 * 
	 * Note: The method calls the {@link #updateCurrentData} method if the
	 * sensor isn't a copy. If the sensor is a copy then there is no association
	 * with a thread anymore and therefore there is no possibility to get new
	 * data.
	 * 
	 * @return currentData
	 */
	public final Map<JointType, Joint> getCurrentData() {
		if (!isCopy)
			updateCurrentData();
		return currentData;
	}

	/**
	 * Sets the current data.
	 * 
	 * Note: This method should be used to set the current data in the
	 * {@link #updateCurrentData} method.
	 * 
	 * @param currentData
	 */
	protected final void setCurrentData(Map<JointType, Joint> currentData) {
		this.currentData = currentData;
	}

	/**
	 * Returns the id of the sensor.
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the sensor.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return initialization state of the sensor.
	 * 
	 * @return initialization state
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * Sets the initialization state of the sensor.
	 * 
	 * @param isInitialized
	 */
	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	@Override
	public Sensor clone(Joint parent) {
		Sensor sensorCopy = null;
		try {
			Class<? extends Sensor> clazz = this.getClass();
			Constructor<? extends Sensor> ctor = clazz.getConstructor(Map.class);
			sensorCopy = (Sensor) ctor.newInstance(this.arguments);
			sensorCopy.setParent(parent);
			sensorCopy.relativePosition = this.relativePosition;
			sensorCopy.absolutePosition = this.absolutePosition;
			sensorCopy.positionTimestamp = this.positionTimestamp;
			sensorCopy.relativeOrientation = this.relativeOrientation;
			sensorCopy.absoluteOrientation = this.absoluteOrientation;
			sensorCopy.orientationTimestamp = this.orientationTimestamp;
			sensorCopy.currentData = this.currentData;
			sensorCopy.id = this.id;
			sensorCopy.isCopy = true;
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return sensorCopy;
	}

	/**
	 * This method is called during the initialization process of the
	 * application. This method should be used to initialize all necessary
	 * components to communicate with the device. It is possible to throw a
	 * {@link com.corpus.sensor.SensorInitializationException} if something went
	 * wrong during the initialization process. The error message will be
	 * printed to the error stream and the sensor will be ignored.
	 * 
	 */
	public abstract void init() throws SensorInitializationException;

	/**
	 * This method is called when the application is closed. This method should
	 * be used to shutdown the sensor.
	 */
	public abstract void terminate();

	/**
	 * This method it called when the current data is requested and this sensor
	 * is not a copy.
	 * 
	 * Note: In an implementation of a sensor, this method can be used to get
	 * the newest data of the sensor. If the sensor uses a event based system to
	 * update the current data then this method can be overwritten with an empty
	 * body. This method should set the current data to an empty HashMap if no
	 * joints are tracked.
	 */
	protected abstract void updateCurrentData();

}
