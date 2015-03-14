package com.corpus.fuser;

import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.Scene;
import com.corpus.sensor.Sensor;

/**
 * This is the base class for the fuser. A fuser fuses the data of different
 * sensors.
 * 
 * @author Matthias Weise
 * 
 */
public abstract class Fuser {

	protected Map<String, String> arguments;

	public Fuser(Map<String, String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * This method fuses the data of different sensors. The data of the sensors
	 * can be accessed through the sensor list of the current scene. The fused
	 * data should directly be set in the current scene.
	 * 
	 * @param currentScene
	 */
	public abstract void fuseData(Scene currentScene);

	/**
	 * Calculates the absolute position of a {@link Joint} in dependence of the
	 * orientation and position of the sensor.
	 * 
	 * @param joint
	 * @param sensor
	 * @return absolute position of a {@link Joint}
	 */
	protected Vector3D getAbsolutePosition(Joint joint, Sensor sensor) {
		return sensor.getAbsoluteOrientation().applyInverseTo(joint.getAbsolutePosition()).add(sensor.getAbsolutePosition());
	}

	/**
	 * Calculates the absolute orientation of a {@link Joint} in dependence of
	 * the orientation of the sensor.
	 * 
	 * @param joint
	 * @param sensor
	 * @return absolute position of a {@link Joint}
	 */
	protected Rotation getAbsoluteRotation(Joint joint, Sensor sensor) {
		return joint.getAbsoluteOrientation().applyTo(sensor.getAbsoluteOrientation());
	}
}
