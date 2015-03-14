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
 * This is a basic implementation of a fuser. The confidence values of the
 * joints will be mapped to 16 variance values. The positions and orientations
 * of the joints are then fused based on the variance values.
 * 
 * 
 * @see "Fusion of Continuous-valued Sensor Measurements using Confidence-weighted Averaging"
 *      by Wilfried Elmenreich
 * 
 * @author Scopus
 * 
 */
public class ConfidenceWeightedFuser extends Fuser {

	private final float[] variance = new float[] { 3333.33f, 1644.65f, 811.47f, 400.37f, 197.54f, 97.47f, 48.09f, 23.73f, 11.71f, 5.78f,
			2.85f, 1.41f, 0.69f, 0.34f, 0.17f, 0.08f };

	public ConfidenceWeightedFuser(Map<String, String> arguments) {
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

		float posSumVariance = 0;
		float oriSumVariance = 0;

		float posSumConfidence = 0;
		float oriSumConfidence = 0;
		int posCount = 0;
		int oriCount = 0;

		// calculate sum variance
		for (Entry<Sensor, Map<JointType, Joint>> entry : fusionData.entrySet()) {
			Joint tmpJoint = entry.getValue().get(joint.getJointType());
			if (tmpJoint != null) {
				if (tmpJoint.getAbsolutePosition() != null)
					posSumVariance += 1 / getVariance(tmpJoint.getPositionConfidence());
				if (tmpJoint.getAbsoluteOrientation() != null) {
					oriSumVariance += 1 / getVariance(tmpJoint.getOrientationConfidence());
				}
			}
		}

		double[] newPosition = new double[] { 0, 0, 0 };
		double[] newOrientation = new double[] { 0, 0, 0, 0 };
		Rotation firstOrientation = null;
		boolean fusedPosition = false;
		boolean fusedOrientation = false;
		for (Entry<Sensor, Map<JointType, Joint>> entry : fusionData.entrySet()) {
			Joint tmpJoint = entry.getValue().get(joint.getJointType());
			if (tmpJoint != null) {
				if (tmpJoint.getAbsolutePosition() != null) {
					Vector3D absPosition;
					if (entry.getKey().getCollectsAbsoluteData())
						absPosition = tmpJoint.getAbsolutePosition();
					else
						absPosition = getAbsolutePosition(tmpJoint, entry.getKey());
					float sensorVariance = 1 / (getVariance(tmpJoint.getPositionConfidence()) * posSumVariance);
					newPosition[0] += absPosition.getX() * sensorVariance;
					newPosition[1] += absPosition.getY() * sensorVariance;
					newPosition[2] += absPosition.getZ() * sensorVariance;
					fusedPosition = true;

					posSumConfidence += tmpJoint.getPositionConfidence();
					posCount++;
				}
				if (tmpJoint.getAbsoluteOrientation() != null) {
					Rotation absOrientation;
					if (entry.getKey().getCollectsAbsoluteData())
						absOrientation = tmpJoint.getAbsoluteOrientation();
					else
						absOrientation = getAbsoluteRotation(tmpJoint, entry.getKey());

					if (firstOrientation == null)
						firstOrientation = absOrientation;

					if (!AreQuaternionsClose(absOrientation, firstOrientation))
						absOrientation = InverseSignQuaternion(tmpJoint.getAbsoluteOrientation());

					float sensorVariance = 1 / (getVariance(tmpJoint.getOrientationConfidence()) * oriSumVariance);

					newOrientation[0] += absOrientation.getQ0() * sensorVariance;
					newOrientation[1] += absOrientation.getQ1() * sensorVariance;
					newOrientation[2] += absOrientation.getQ2() * sensorVariance;
					newOrientation[3] += absOrientation.getQ3() * sensorVariance;
					fusedOrientation = true;

					oriSumConfidence += tmpJoint.getOrientationConfidence();
					oriCount++;
				}
			}
		}

		// set new joint position if position was tracked, else absolute
		// position has to be recalculated because the position of a parent
		// joint may changed
		if (fusedPosition) {
			joint.setAbsolutePosition(new Vector3D(newPosition[0], newPosition[1], newPosition[2]));
		} else {
			joint.setAbsolutePosition(null);
		}

		// set new joint orientation if position was tracked, else absolute
		// orientation has to be recalculated because the position of a parent
		// joint may changed
		if (fusedOrientation) {
			joint.setAbsoluteOrientation(new Rotation(newOrientation[0], newOrientation[1], newOrientation[2], newOrientation[3], true));
		} else {
			joint.setAbsoluteOrientation(null);
		}

		if (fusedPosition) {
			joint.setPositionTimestamp(Calendar.getInstance());
			joint.setPositionTracked(true);
		} else {
			if (Calendar.getInstance().getTimeInMillis() - joint.getPositionTimestamp().getTimeInMillis() >= Controller.RETURN_TO_DEFAULT) {
				joint.setRelativePosition(joint.getDefaultPosition());
			}
			joint.setPositionTracked(false);
		}

		if (fusedOrientation) {
			joint.setOrientationTimestamp(Calendar.getInstance());
			joint.setOrientationTracked(true);
		} else {
			if (Calendar.getInstance().getTimeInMillis() - joint.getOrientationTimestamp().getTimeInMillis() >= Controller.RETURN_TO_DEFAULT) {
				joint.setRelativeOrientation(joint.getDefaultOrientation());
			}
			joint.setOrientationTracked(false);
		}

		// set average confidence values
		joint.setPositionConfidence(posSumConfidence / posCount);
		joint.setOrientationConfidence(oriSumConfidence / oriCount);
	}

	private Rotation InverseSignQuaternion(Rotation q) {
		return new Rotation(-q.getQ0(), -q.getQ1(), -q.getQ2(), -q.getQ3(), false);
	}

	/**
	 * Returns true if the two input quaternions are close to each other.
	 * 
	 * @param r1
	 * @param r2
	 * @return true if close, false else
	 */
	private boolean AreQuaternionsClose(Rotation r1, Rotation r2) {
		if (Rotation.distance(r1, r2) < 0.0d)
			return false;
		return true;
	}

	private float getVariance(float confidence) {
		if (confidence < 0)
			return variance[0];
		if (confidence > 1)
			return variance[15];
		return variance[(int) Math.floor(confidence * 15)];
	}
}
