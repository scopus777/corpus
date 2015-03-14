package com.corpus.fuser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.controller.Controller;
import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.sensor.Sensor;

/**
 * {@link Fuser} that fuses the data in dependence of the given confidence
 * values. Chooses the position or orientation of the joint with the highest
 * confidence value.
 * 
 * @author Matthias Weise
 * 
 */
public class ConfidenceDominanceFuser extends Fuser {

	public ConfidenceDominanceFuser(Map<String, String> arguments) {
		super(arguments);
	}

	@Override
	public void fuseData(Scene currentScene) {

		// identify the joints a sensor depends on
		Set<JointType> sensorDependingJoints = new HashSet<JointType>();
		for (Sensor sensor : currentScene.getSensors()) {
			if (sensor.getParent() instanceof Joint)
				addToSetRecursive((Joint) sensor.getParent(), sensorDependingJoints);
		}

		// get new data from the sensors
		Map<Sensor, Map<JointType, Joint>> fusionData = new HashMap<Sensor, Map<JointType, Joint>>();
		for (Sensor sensor : currentScene.getSensors()) {
			fusionData.put(sensor, sensor.getCurrentData());
		}

		// update the joints a sensor depends on
		for (Joint joint : currentScene.getRootJoints()) {
			if (sensorDependingJoints.contains(joint.getJointType()))
				updateSensorDependingJoints(joint, sensorDependingJoints, fusionData);
		}

		// update the remaining joints
		for (Joint j : currentScene.getRootJoints())
			updateRemainingJoints(j, sensorDependingJoints, fusionData);

	}

	/**
	 * Adds a {@link Joint} and its parent recursively to the given set.
	 * 
	 * @param joint
	 * @param set
	 */
	private void addToSetRecursive(Joint joint, Set<JointType> set) {
		set.add(joint.getJointType());
		if (joint.getParent() != null)
			if (joint.getParent() instanceof Joint)
				addToSetRecursive((Joint) joint.getParent(), set);
	}

	/**
	 * Updates the joint a sensor depends on.
	 * 
	 * @param joint
	 * @param sensorDependingJoints
	 * @param fusionData
	 */
	private void updateSensorDependingJoints(Joint joint, Set<JointType> sensorDependingJoints,
			Map<Sensor, Map<JointType, Joint>> fusionData) {
		updateJoint(joint, fusionData);

		for (com.corpus.scene.SceneNode child : joint.getChildren()) {
			if (child instanceof Joint) {
				Joint childJoint = (Joint) child;
				if (sensorDependingJoints.contains(childJoint.getJointType()))
					updateSensorDependingJoints((Joint) child, sensorDependingJoints, fusionData);
			} else if (child instanceof Sensor) {
				child.setAbsolutePosition(null);
				child.setAbsoluteOrientation(null);
			}
		}
	}

	/**
	 * Updates the joints no sensor depends on.
	 * 
	 * @param joint
	 * @param sensorDependingJoints
	 * @param fusionData
	 */
	private void updateRemainingJoints(Joint joint, Set<JointType> sensorDependingJoints, Map<Sensor, Map<JointType, Joint>> fusionData) {
		if (!sensorDependingJoints.contains(joint.getJointType()))
			updateJoint(joint, fusionData);

		for (com.corpus.scene.SceneNode child : joint.getChildren()) {
			if (child instanceof Joint)
				updateRemainingJoints((Joint) child, sensorDependingJoints, fusionData);
		}

	}

	/**
	 * Updates a single joint.
	 * 
	 * @param joint
	 * @param fusionData
	 */
	private void updateJoint(Joint joint, Map<Sensor, Map<JointType, Joint>> fusionData) {

		float positionConfidence = 0f;
		Vector3D newPosition = null;
		float orientationConfidence = 0f;
		Rotation newOrientation = null;

		for (Entry<Sensor, Map<JointType, Joint>> entry : fusionData.entrySet()) {
			Joint tmpJoint = entry.getValue().get(joint.getJointType());
			if (tmpJoint != null) {
				if (tmpJoint.getPositionConfidence() > positionConfidence && tmpJoint.getAbsolutePosition() != null) {
					if (entry.getKey().getCollectsAbsoluteData())
						newPosition = tmpJoint.getAbsolutePosition();
					else
						newPosition = getAbsolutePosition(tmpJoint, entry.getKey());
					positionConfidence = tmpJoint.getPositionConfidence();
				}
				if (tmpJoint.getOrientationConfidence() > orientationConfidence && tmpJoint.getAbsoluteOrientation() != null) {
					if (entry.getKey().getCollectsAbsoluteData())
						newOrientation = tmpJoint.getAbsoluteOrientation();
					else
						newOrientation = getAbsoluteRotation(tmpJoint, entry.getKey());
					orientationConfidence = tmpJoint.getOrientationConfidence();
				}
			}
		}

		// set new joint position if position was tracked, else absolute
		// position has to be recalculated because the position of a parent
		// joint may changed
		if (newPosition != null) {
			joint.setAbsolutePosition(newPosition);
		} else {
			joint.setAbsolutePosition(null);
		}

		// set new joint orientation if position was tracked, else absolute
		// orientation has to be recalculated because the position of a parent
		// joint may changed
		if (newOrientation != null) {
			joint.setAbsoluteOrientation(newOrientation);
		} else {
			joint.setAbsoluteOrientation(null);
		}

		if (newPosition != null) {
			joint.setPositionTimestamp(Calendar.getInstance());
			joint.setPositionTracked(true);
		} else {
			if (Calendar.getInstance().getTimeInMillis() - joint.getPositionTimestamp().getTimeInMillis() >= Controller.RETURN_TO_DEFAULT) {
				joint.setRelativePosition(joint.getDefaultPosition());
			}
			joint.setPositionTracked(false);
		}

		if (newOrientation != null) {
			joint.setOrientationTimestamp(Calendar.getInstance());
			joint.setOrientationTracked(true);
		} else {
			if (Calendar.getInstance().getTimeInMillis() - joint.getOrientationTimestamp().getTimeInMillis() >= Controller.RETURN_TO_DEFAULT) {
				joint.setRelativeOrientation(joint.getDefaultOrientation());
			}
			joint.setOrientationTracked(false);
		}

		joint.setPositionConfidence(positionConfidence);
		joint.setOrientationConfidence(orientationConfidence);
	}
}
