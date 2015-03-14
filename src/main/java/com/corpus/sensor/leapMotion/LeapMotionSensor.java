package com.corpus.sensor.leapMotion;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.sensor.Sensor;
import com.corpus.sensor.SensorInitializationException;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Vector;

/**
 * <p>
 * Wrapper for the <i>Leap Motion</i> which can track the joints of the hands.
 * The current data is updated in the {@link Sensor#updateCurrentData
 * updateCurrentData} method. That means the data is updated in update chain.
 * </p>
 * 
 * <p>
 * Through the arguments defined in the configuration file it is possible to set
 * the <i>policy flags</i>.
 * </p>
 * 
 * <p>
 * The used confidence values are based on the experiences with the sensor.
 * </p>
 * 
 * @see <a
 *      href="https://developer.leapmotion.com/documentation/java/api/Leap_Classes.html">Leap
 *      Motion API</a>
 * 
 * @author Matthias Weise
 * 
 */
public class LeapMotionSensor extends Sensor {

	// leap motion controller enabling a connection to the Leap Motion
	Controller leapController;

	public LeapMotionSensor(Map<String, String> arguments) {
		super(arguments);
	}

	@Override
	public void init() {
		leapController = new Controller();
		int isConnectedTries = 0;
		while (!leapController.isConnected()) {
			isConnectedTries++;
			if (isConnectedTries > 200)
				throw new SensorInitializationException("Leap Motion could not be initialized. Make sure it is connected.");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		parseArguments();
	}

	@Override
	public void run() {
	}

	@Override
	public void terminate() {
	}

	@Override
	protected void updateCurrentData() {
		HashMap<JointType, Joint> newData = new HashMap<JointType, Joint>();
		for (Hand hand : leapController.frame().hands()) {
			if (hand.isLeft()) {
				Vector pos = hand.arm().wristPosition();
				Rotation rot = getRotationLeftHand(hand.arm().basis());
				newData.put(JointType.WRIST_LEFT, createJoint(pos, rot, hand));

				pos = hand.arm().elbowPosition();
				rot = new Rotation(1, 0, 0, 0, false);
				newData.put(JointType.ELBOW_LEFT, createJoint(pos, rot, 0.1f, 0));

				for (Finger finger : hand.fingers()) {
					switch (finger.type()) {
					case TYPE_INDEX:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_INDEX_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_INDEX_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_INDEX_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_INDEX_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_INDEX_FINGER_LEFT, createJoint(pos, rot, hand));
						break;
					case TYPE_MIDDLE:
						pos = hand.palmPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.HAND_CENTER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						newData.put(JointType.CMC_MIDDLE_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_MIDDLE_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_MIDDLE_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_MIDDLE_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_MIDDLE_FINGER_LEFT, createJoint(pos, rot, hand));
						break;
					case TYPE_PINKY:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_SMALL_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_SMALL_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_SMALL_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_SMALL_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_SMALL_FINGER_LEFT, createJoint(pos, rot, hand));
						break;
					case TYPE_RING:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_RING_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_RING_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_RING_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_RING_FINGER_LEFT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_RING_FINGER_LEFT, createJoint(pos, rot, hand));
						break;
					case TYPE_THUMB:
						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.CMC_THUMB_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.MCP_THUMB_LEFT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.IP_THUMB_LEFT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationLeftHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_THUMB_LEFT, createJoint(pos, rot, hand));
						break;
					}
				}
			} else {
				Vector pos = hand.arm().wristPosition();
				Rotation rot = getRotationRightHand(hand.arm().basis());
				newData.put(JointType.WRIST_RIGHT, createJoint(pos, rot, hand));

				pos = hand.arm().elbowPosition();
				rot = new Rotation(1, 0, 0, 0, false);
				newData.put(JointType.ELBOW_RIGHT, createJoint(pos, rot, 0.1f, 0f));

				for (Finger finger : hand.fingers()) {
					switch (finger.type()) {
					case TYPE_INDEX:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_INDEX_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_INDEX_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_INDEX_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_INDEX_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_INDEX_FINGER_RIGHT, createJoint(pos, rot, hand));
						break;
					case TYPE_MIDDLE:
						pos = hand.palmPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.HAND_CENTER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						newData.put(JointType.CMC_MIDDLE_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_MIDDLE_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_MIDDLE_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_MIDDLE_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_MIDDLE_FINGER_RIGHT, createJoint(pos, rot, hand));
						break;
					case TYPE_PINKY:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_SMALL_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_SMALL_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_SMALL_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_SMALL_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_SMALL_FINGER_RIGHT, createJoint(pos, rot, hand));
						break;
					case TYPE_RING:
						pos = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.CMC_RING_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_METACARPAL).basis());
						newData.put(JointType.MCP_RING_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.PIP_RING_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.DIP_RING_FINGER_RIGHT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_RING_FINGER_RIGHT, createJoint(pos, rot, hand));
						break;
					case TYPE_THUMB:
						pos = finger.bone(Bone.Type.TYPE_PROXIMAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.CMC_THUMB_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_INTERMEDIATE).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_PROXIMAL).basis());
						newData.put(JointType.MCP_THUMB_RIGHT, createJoint(pos, rot, hand));

						pos = finger.bone(Bone.Type.TYPE_DISTAL).prevJoint();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_INTERMEDIATE).basis());
						newData.put(JointType.IP_THUMB_RIGHT, createJoint(pos, rot, hand));

						pos = finger.tipPosition();
						rot = getRotationRightHand(finger.bone(Bone.Type.TYPE_DISTAL).basis());
						newData.put(JointType.BTIP_THUMB_RIGHT, createJoint(pos, rot, hand));
						break;
					}
				}
			}
		}
		setCurrentData(newData);
	}

	/**
	 * Creates a {@link Joint}.
	 * 
	 * @param pos
	 *            Absolute position of the joint.
	 * @param rot
	 *            Absolute rotation of the joint.
	 * @param hand
	 *            Hand the joint belongs to.
	 * @return Corresponding joint.
	 */
	private Joint createJoint(Vector pos, Rotation rot, Hand hand) {
		return createJoint(pos, rot, hand.confidence() + (1f - hand.confidence()) / 2, hand.confidence() + (1f - hand.confidence()) / 2);
	}

	/**
	 * Creates a {@link Joint}.
	 * 
	 * @param pos
	 *            Absolute position of the joint.
	 * @param rot
	 *            Absolute rotation of the joint.
	 * @param posConfidence
	 *            Condifence of the position.
	 * @param oriConfidence
	 *            Condifence of the orientation.
	 * @return new {@link Joint}
	 */
	private Joint createJoint(Vector pos, Rotation rot, float posConfidence, float oriConfidence) {
		Vector3D newPos = new Vector3D(pos.getX() / 10f, pos.getY() / 10f, pos.getZ() / 10f);
		Rotation newOri = null;
		// TODO: Sometimes the rotation is not valid.
		if (Double.compare(rot.getQ0(), Double.NaN) != 0)
			newOri = rot;
		Joint joint = new Joint(null, newPos, newOri);
		if (positionConfidence > 0)
			joint.setPositionConfidence(positionConfidence);
		else
			joint.setPositionConfidence(posConfidence);
		if (orientationConfidence > 0)
			joint.setOrientationConfidence(orientationConfidence);
		else
			joint.setOrientationConfidence(oriConfidence);
		return joint;
	}

	/**
	 * Returns the {@link Rotation} for a joint of the left hand on the basis of
	 * a rotation matrix. Therefore the left-hand rule has to be applied. Also
	 * applies an additional rotation to match the rotation of the framework.
	 * 
	 * @param matrix
	 *            Rotation matrix.
	 * @return {@link Rotation}.
	 */
	private Rotation getRotationLeftHand(Matrix matrix) {
		matrix.setZBasis(matrix.getZBasis().times(-1));
		return new Rotation(0.7071, 0.7071, 0, 0, false).applyTo(translateMatrix(matrix));
	}

	/**
	 * Returns the {@link Rotation} for a joint of the right hand on the basis
	 * of a rotation matrix. Therefore the right-hand rule has to be applied.
	 * Also applies an additional rotation to match the rotation of the
	 * framework.
	 * 
	 * @param matrix
	 *            Rotation matrix.
	 * @return {@link Rotation}.
	 */
	private Rotation getRotationRightHand(Matrix matrix) {
		return new Rotation(0, 0, 0.7071, -0.7071, false).applyTo(translateMatrix(matrix));
	}

	/**
	 * Translates a matrix to a {@link Rotation} object.
	 * 
	 * @param matrix
	 *            Rotation matrix.
	 * @return matrix as a {@link Rotation}.
	 */
	private Rotation translateMatrix(Matrix matrix) {
		float[] derivedRotMatrix = matrix.toArray4x4();
		double w = Math.sqrt(1.0 + derivedRotMatrix[0] + derivedRotMatrix[5] + derivedRotMatrix[10]) / 2.0;
		double w4 = (4.0 * w);
		double x = (derivedRotMatrix[6] - derivedRotMatrix[9]) / w4;
		double y = (derivedRotMatrix[8] - derivedRotMatrix[2]) / w4;
		double z = (derivedRotMatrix[1] - derivedRotMatrix[4]) / w4;

		return new Rotation(w, x, y, z, false);
	}

	/**
	 * Parses the arguments and sets corresponding options.
	 * 
	 */
	private void parseArguments() {
		for (Entry<String, String> entry : arguments.entrySet()) {
			if (entry.getKey().equals("policyFlags")) {
				for (String policyFlag : entry.getValue().split(" ")) {
					Controller.PolicyFlag parsedPolicyFlag = null;
					try {
						parsedPolicyFlag = Controller.PolicyFlag.valueOf(policyFlag);
					} catch (IllegalArgumentException e) {
						System.err.println("WARNING: Unknown PolicyFlag \"" + policyFlag + "\" declared for the LeapMotionSensor!");
					}
					if (parsedPolicyFlag != null)
						leapController.setPolicy(parsedPolicyFlag);
				}
			} else
				System.err.println("WARNING: Unknown argument " + entry.getKey()
						+ " for LeapMotionSensor in the config file! The argument will be ignored!");
		}

	}
}
