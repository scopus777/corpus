package com.corpus.scene;

import java.util.HashMap;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.configuration.ModelType;

/**
 * Initializes the model of the human body with the default positions.
 * 
 * @author Matthias Weise
 * 
 */
public class ModelInitializer {

	/**
	 * Initializes the model in dependence of the given model type.
	 * 
	 * @param modelType
	 */
	public static HashMap<JointType, Joint> initializeModel(ModelType torsoType, ModelType handsType, ModelType feetType) {
		HashMap<JointType, Joint> joints = new HashMap<JointType, Joint>();
		switch (torsoType) {
		case SPARSE:
		case COMPLEX:
			generateTorso(joints, torsoType);
			break;
		default:
			break;
		}
		switch (handsType) {
		case SPARSE:
		case COMPLEX:
			generateHands(joints, handsType, torsoType);
			break;
		default:
			break;
		}
		switch (feetType) {
		case SPARSE:
		case COMPLEX:
			generateFeet(joints, feetType, torsoType);
			break;
		default:
			break;
		}
		return joints;
	}

	/**
	 * Generates the {@link Joint}s of the torso in dependence of to the chosen
	 * {@link ModelType}.
	 * 
	 * @param joints
	 *            Map containing the current {@link Joint}s
	 * @param torsoType
	 *            chosen {@link ModelType}
	 */
	private static void generateTorso(HashMap<JointType, Joint> joints, ModelType torsoType) {
		joints.put(JointType.SPINE_BASE, new Joint(JointType.SPINE_BASE, new Vector3D(0, 20, 300), new Rotation(0, 0, 1, 0, false), null));
		// joints.put(JointType.SPINE_BASE, new Joint(JointType.SPINE_BASE, new
		// Vector3D(0, 30, 180), new Rotation(0.382, 0, 0.923, 0, false),
		// null));
		// joints.put(JointType.SPINE_BASE, new Joint(JointType.SPINE_BASE, new
		// Vector3D(0, 30, 180), new Rotation(0.342, 0, 0.939, 0, false),
		// null));

		joints.put(JointType.HIP_RIGHT, new Joint(JointType.HIP_RIGHT, new Vector3D(-17.9, 0, 0),
				new Rotation(0.7071, 0, 0, 0.7071, false), joints.get(JointType.SPINE_BASE)));
		joints.put(JointType.KNEE_RIGHT, new Joint(JointType.KNEE_RIGHT, new Vector3D(-41, 0, 0),
				new Rotation(0.7071, 0, 0, 0.7071, false), joints.get(JointType.HIP_RIGHT)));

		joints.put(JointType.HIP_LEFT, new Joint(JointType.HIP_LEFT, new Vector3D(17.9, 0, 0), new Rotation(0.7071, 0, 0, -0.7071, false),
				joints.get(JointType.SPINE_BASE)));
		joints.put(JointType.KNEE_LEFT, new Joint(JointType.KNEE_LEFT, new Vector3D(41, 0, 0), new Rotation(0.7071, 0, 0, -0.7071, false),
				joints.get(JointType.HIP_LEFT)));

		if (torsoType == ModelType.COMPLEX) {
			joints.put(JointType.SPINE_MID, new Joint(JointType.SPINE_MID, new Vector3D(0, 25.9, 0), new Rotation(1, 0, 0, 0, false),
					joints.get(JointType.SPINE_BASE)));
			joints.put(JointType.SPINE_SHOULDER, new Joint(JointType.SPINE_SHOULDER, new Vector3D(0, 25.9, 0), new Rotation(1, 0, 0, 0,
					false), joints.get(JointType.SPINE_MID)));
		} else {
			joints.put(JointType.SPINE_SHOULDER, new Joint(JointType.SPINE_SHOULDER, new Vector3D(0, 51.8, 0), new Rotation(1, 0, 0, 0,
					false), joints.get(JointType.SPINE_BASE)));
		}

		joints.put(JointType.SHOULDER_RIGHT, new Joint(JointType.SHOULDER_RIGHT, new Vector3D(-24.45, 0, 0), new Rotation(0.7071, 0, 0,
				0.7071, false), joints.get(JointType.SPINE_SHOULDER)));
		joints.put(JointType.ELBOW_RIGHT, new Joint(JointType.ELBOW_RIGHT, new Vector3D(-39, 0, 0), new Rotation(0.7071, 0, 0, 0.7071,
				false), joints.get(JointType.SHOULDER_RIGHT)));

		joints.put(JointType.SHOULDER_LEFT, new Joint(JointType.SHOULDER_LEFT, new Vector3D(24.45, 0, 0), new Rotation(0.7071, 0, 0,
				-0.7071, false), joints.get(JointType.SPINE_SHOULDER)));
		joints.put(JointType.ELBOW_LEFT, new Joint(JointType.ELBOW_LEFT, new Vector3D(39, 0, 0),
				new Rotation(0.7071, 0, 0, -0.7071, false), joints.get(JointType.SHOULDER_LEFT)));

		if (torsoType == ModelType.COMPLEX) {
			joints.put(
					JointType.NECK,
					new Joint(JointType.NECK, new Vector3D(0, 12.5, 0), new Rotation(1, 0, 0, 0, false), joints
							.get(JointType.SPINE_SHOULDER)));
			joints.put(JointType.HEAD,
					new Joint(JointType.HEAD, new Vector3D(0, 12.5, 0), new Rotation(1, 0, 0, 0, false), joints.get(JointType.NECK)));
		} else {
			joints.put(
					JointType.HEAD,
					new Joint(JointType.HEAD, new Vector3D(0, 25, 0), new Rotation(1, 0, 0, 0, false), joints.get(JointType.SPINE_SHOULDER)));
		}

	}

	/**
	 * Generates the {@link Joint}s of the hands in dependence of to the chosen
	 * {@link ModelType}.
	 * 
	 * @param joints
	 *            Map containing the current {@link Joint}s
	 * @param torsoType
	 *            chosen {@link ModelType}
	 */
	private static void generateHands(HashMap<JointType, Joint> joints, ModelType handsType, ModelType torsoType) {
		if (torsoType == ModelType.NONE) {
			joints.put(JointType.WRIST_RIGHT, new Joint(JointType.WRIST_RIGHT, new Vector3D(24.45, -6.19, 300), new Rotation(0, 1, 0, 0,
					false), null));
			joints.put(JointType.WRIST_LEFT, new Joint(JointType.WRIST_LEFT, new Vector3D(-24.45, -6.19, 300), new Rotation(0, 1, 0, 0,
					false), null));
		} else {
			joints.put(JointType.WRIST_RIGHT, new Joint(JointType.WRIST_RIGHT, new Vector3D(0, 39, 0), new Rotation(1, 0, 0, 0, false),
					joints.get(JointType.ELBOW_RIGHT)));
			joints.put(JointType.WRIST_LEFT, new Joint(JointType.WRIST_LEFT, new Vector3D(0, 39, 0), new Rotation(1, 0, 0, 0, false),
					joints.get(JointType.ELBOW_LEFT)));
		}

		joints.put(JointType.HAND_CENTER_RIGHT, new Joint(JointType.HAND_CENTER_RIGHT, new Vector3D(0, 8, 0), new Rotation(1, 0, 0, 0,
				false), joints.get(JointType.WRIST_RIGHT)));
		joints.put(JointType.HAND_CENTER_LEFT, new Joint(JointType.HAND_CENTER_LEFT, new Vector3D(0, 8, 0),
				new Rotation(1, 0, 0, 0, false), joints.get(JointType.WRIST_LEFT)));

		if (handsType == ModelType.COMPLEX) {
			joints.put(JointType.CMC_SMALL_FINGER_RIGHT, new Joint(JointType.CMC_SMALL_FINGER_RIGHT, new Vector3D(-2.7, 1.2, 0),
					new Rotation(0.829, 0, 0, 0.559, false), joints.get(JointType.WRIST_RIGHT)));
			joints.put(JointType.MCP_SMALL_FINGER_RIGHT, new Joint(JointType.MCP_SMALL_FINGER_RIGHT, new Vector3D(5.1, 2.7, 0),
					new Rotation(0.857, 0, 0, -0.515, false), joints.get(JointType.CMC_SMALL_FINGER_RIGHT)));
			joints.put(JointType.PIP_SMALL_FINGER_RIGHT,
					new Joint(JointType.PIP_SMALL_FINGER_RIGHT, new Vector3D(0, 4.1, 0), joints.get(JointType.MCP_SMALL_FINGER_RIGHT)));
			joints.put(JointType.DIP_SMALL_FINGER_RIGHT,
					new Joint(JointType.DIP_SMALL_FINGER_RIGHT, new Vector3D(0, 2.2, 0), joints.get(JointType.PIP_SMALL_FINGER_RIGHT)));
			joints.put(JointType.BTIP_SMALL_FINGER_RIGHT,
					new Joint(JointType.BTIP_SMALL_FINGER_RIGHT, new Vector3D(0, 2.1, 0), joints.get(JointType.DIP_SMALL_FINGER_RIGHT)));

			joints.put(JointType.CMC_RING_FINGER_RIGHT, new Joint(JointType.CMC_RING_FINGER_RIGHT, new Vector3D(-1.5, 1.7, 0),
					new Rotation(0.927, 0, 0, 0.374, false), joints.get(JointType.WRIST_RIGHT)));
			joints.put(JointType.MCP_RING_FINGER_RIGHT, new Joint(JointType.MCP_RING_FINGER_RIGHT, new Vector3D(4.4, 4.4, 0), new Rotation(
					0.933, 0, 0, -0.358, false), joints.get(JointType.CMC_RING_FINGER_RIGHT)));
			joints.put(JointType.PIP_RING_FINGER_RIGHT,
					new Joint(JointType.PIP_RING_FINGER_RIGHT, new Vector3D(0, 4.7, 0), joints.get(JointType.MCP_RING_FINGER_RIGHT)));
			joints.put(JointType.DIP_RING_FINGER_RIGHT,
					new Joint(JointType.DIP_RING_FINGER_RIGHT, new Vector3D(0, 3.4, 0), joints.get(JointType.PIP_RING_FINGER_RIGHT)));
			joints.put(JointType.BTIP_RING_FINGER_RIGHT,
					new Joint(JointType.BTIP_RING_FINGER_RIGHT, new Vector3D(0, 2.2, 0), joints.get(JointType.DIP_RING_FINGER_RIGHT)));

			joints.put(JointType.CMC_MIDDLE_FINGER_RIGHT, new Joint(JointType.CMC_MIDDLE_FINGER_RIGHT, new Vector3D(0.4, 1.8, 0),
					new Rotation(0.998, 0, 0, -0.061, false), joints.get(JointType.WRIST_RIGHT)));
			joints.put(JointType.MCP_MIDDLE_FINGER_RIGHT, new Joint(JointType.MCP_MIDDLE_FINGER_RIGHT, new Vector3D(-0.4, 6.9, 0),
					new Rotation(0.999, 0, 0, 0.017, false), joints.get(JointType.CMC_MIDDLE_FINGER_RIGHT)));
			joints.put(JointType.PIP_MIDDLE_FINGER_RIGHT,
					new Joint(JointType.PIP_MIDDLE_FINGER_RIGHT, new Vector3D(0, 5.3, 0), joints.get(JointType.MCP_MIDDLE_FINGER_RIGHT)));
			joints.put(JointType.DIP_MIDDLE_FINGER_RIGHT,
					new Joint(JointType.DIP_MIDDLE_FINGER_RIGHT, new Vector3D(0, 2.9, 0), joints.get(JointType.PIP_MIDDLE_FINGER_RIGHT)));
			joints.put(JointType.BTIP_MIDDLE_FINGER_RIGHT,
					new Joint(JointType.BTIP_MIDDLE_FINGER_RIGHT, new Vector3D(0, 2.4, 0), joints.get(JointType.DIP_MIDDLE_FINGER_RIGHT)));

			joints.put(JointType.CMC_INDEX_FINGER_RIGHT, new Joint(JointType.CMC_INDEX_FINGER_RIGHT, new Vector3D(1.5, 1.7, 0),
					new Rotation(0.933, 0, 0, -0.358, false), joints.get(JointType.WRIST_RIGHT)));
			joints.put(JointType.MCP_INDEX_FINGER_RIGHT, new Joint(JointType.MCP_INDEX_FINGER_RIGHT, new Vector3D(-3.6, 6.5, 0),
					new Rotation(0.968, 0, 0, 0.250, false), joints.get(JointType.CMC_INDEX_FINGER_RIGHT)));
			joints.put(JointType.PIP_INDEX_FINGER_RIGHT,
					new Joint(JointType.PIP_INDEX_FINGER_RIGHT, new Vector3D(0, 5.2, 0), joints.get(JointType.MCP_INDEX_FINGER_RIGHT)));
			joints.put(JointType.DIP_INDEX_FINGER_RIGHT,
					new Joint(JointType.DIP_INDEX_FINGER_RIGHT, new Vector3D(0, 2.4, 0), joints.get(JointType.PIP_INDEX_FINGER_RIGHT)));
			joints.put(JointType.BTIP_INDEX_FINGER_RIGHT,
					new Joint(JointType.BTIP_INDEX_FINGER_RIGHT, new Vector3D(0, 2.2, 0), joints.get(JointType.DIP_INDEX_FINGER_RIGHT)));

			joints.put(JointType.CMC_THUMB_RIGHT, new Joint(JointType.CMC_THUMB_RIGHT, new Vector3D(3.2, 0.7, 0), new Rotation(0.788, 0, 0,
					-0.615, false), joints.get(JointType.WRIST_RIGHT)));
			joints.put(JointType.MCP_THUMB_RIGHT, new Joint(JointType.MCP_THUMB_RIGHT, new Vector3D(-3.2, 4, 0), new Rotation(0.942, 0, 0,
					0.333, false), joints.get(JointType.CMC_THUMB_RIGHT)));
			joints.put(JointType.IP_THUMB_RIGHT,
					new Joint(JointType.IP_THUMB_RIGHT, new Vector3D(0, 3.9, 0), joints.get(JointType.MCP_THUMB_RIGHT)));
			joints.put(JointType.BTIP_THUMB_RIGHT,
					new Joint(JointType.BTIP_THUMB_RIGHT, new Vector3D(0, 3.5, 0), joints.get(JointType.IP_THUMB_RIGHT)));

			joints.put(JointType.CMC_SMALL_FINGER_LEFT, new Joint(JointType.CMC_SMALL_FINGER_LEFT, new Vector3D(2.7, 1.2, 0), new Rotation(
					0.829, 0, 0, -0.559, false), joints.get(JointType.WRIST_LEFT)));
			joints.put(JointType.MCP_SMALL_FINGER_LEFT, new Joint(JointType.MCP_SMALL_FINGER_LEFT, new Vector3D(-5.1, 2.7, 0),
					new Rotation(0.857, 0, 0, 0.515, false), joints.get(JointType.CMC_SMALL_FINGER_LEFT)));
			joints.put(JointType.PIP_SMALL_FINGER_LEFT,
					new Joint(JointType.PIP_SMALL_FINGER_LEFT, new Vector3D(0, 4.1, 0), joints.get(JointType.MCP_SMALL_FINGER_LEFT)));
			joints.put(JointType.DIP_SMALL_FINGER_LEFT,
					new Joint(JointType.DIP_SMALL_FINGER_LEFT, new Vector3D(0, 2.2, 0), joints.get(JointType.PIP_SMALL_FINGER_LEFT)));
			joints.put(JointType.BTIP_SMALL_FINGER_LEFT,
					new Joint(JointType.BTIP_SMALL_FINGER_LEFT, new Vector3D(0, 2.1, 0), joints.get(JointType.DIP_SMALL_FINGER_LEFT)));

			joints.put(JointType.CMC_RING_FINGER_LEFT, new Joint(JointType.CMC_RING_FINGER_LEFT, new Vector3D(1.5, 1.7, 0), new Rotation(
					0.927, 0, 0, -0.374, false), joints.get(JointType.WRIST_LEFT)));
			joints.put(JointType.MCP_RING_FINGER_LEFT, new Joint(JointType.MCP_RING_FINGER_LEFT, new Vector3D(-4.4, 4.4, 0), new Rotation(
					0.933, 0, 0, 0.358, false), joints.get(JointType.CMC_RING_FINGER_LEFT)));
			joints.put(JointType.PIP_RING_FINGER_LEFT,
					new Joint(JointType.PIP_RING_FINGER_LEFT, new Vector3D(0, 4.7, 0), joints.get(JointType.MCP_RING_FINGER_LEFT)));
			joints.put(JointType.DIP_RING_FINGER_LEFT,
					new Joint(JointType.DIP_RING_FINGER_LEFT, new Vector3D(0, 3.4, 0), joints.get(JointType.PIP_RING_FINGER_LEFT)));
			joints.put(JointType.BTIP_RING_FINGER_LEFT,
					new Joint(JointType.BTIP_RING_FINGER_LEFT, new Vector3D(0, 2.2, 0), joints.get(JointType.DIP_RING_FINGER_LEFT)));

			joints.put(JointType.CMC_MIDDLE_FINGER_LEFT, new Joint(JointType.CMC_MIDDLE_FINGER_LEFT, new Vector3D(-0.4, 1.8, 0),
					new Rotation(0.998, 0, 0, 0.061, false), joints.get(JointType.WRIST_LEFT)));
			joints.put(JointType.MCP_MIDDLE_FINGER_LEFT, new Joint(JointType.MCP_MIDDLE_FINGER_LEFT, new Vector3D(0.4, 6.9, 0),
					new Rotation(0.999, 0, 0, -0.017, false), joints.get(JointType.CMC_MIDDLE_FINGER_LEFT)));
			joints.put(JointType.PIP_MIDDLE_FINGER_LEFT,
					new Joint(JointType.PIP_MIDDLE_FINGER_LEFT, new Vector3D(0, 5.3, 0), joints.get(JointType.MCP_MIDDLE_FINGER_LEFT)));
			joints.put(JointType.DIP_MIDDLE_FINGER_LEFT,
					new Joint(JointType.DIP_MIDDLE_FINGER_LEFT, new Vector3D(0, 2.9, 0), joints.get(JointType.PIP_MIDDLE_FINGER_LEFT)));
			joints.put(JointType.BTIP_MIDDLE_FINGER_LEFT,
					new Joint(JointType.BTIP_MIDDLE_FINGER_LEFT, new Vector3D(0, 2.4, 0), joints.get(JointType.DIP_MIDDLE_FINGER_LEFT)));

			joints.put(JointType.CMC_INDEX_FINGER_LEFT, new Joint(JointType.CMC_INDEX_FINGER_LEFT, new Vector3D(-1.5, 1.7, 0),
					new Rotation(0.933, 0, 0, 0.358, false), joints.get(JointType.WRIST_LEFT)));
			joints.put(JointType.MCP_INDEX_FINGER_LEFT, new Joint(JointType.MCP_INDEX_FINGER_LEFT, new Vector3D(3.6, 6.5, 0), new Rotation(
					0.968, 0, 0, -0.250, false), joints.get(JointType.CMC_INDEX_FINGER_LEFT)));
			joints.put(JointType.PIP_INDEX_FINGER_LEFT,
					new Joint(JointType.PIP_INDEX_FINGER_LEFT, new Vector3D(0, 5.2, 0), joints.get(JointType.MCP_INDEX_FINGER_LEFT)));
			joints.put(JointType.DIP_INDEX_FINGER_LEFT,
					new Joint(JointType.DIP_INDEX_FINGER_LEFT, new Vector3D(0, 2.4, 0), joints.get(JointType.PIP_INDEX_FINGER_LEFT)));
			joints.put(JointType.BTIP_INDEX_FINGER_LEFT,
					new Joint(JointType.BTIP_INDEX_FINGER_LEFT, new Vector3D(0, 2.2, 0), joints.get(JointType.DIP_INDEX_FINGER_LEFT)));

			joints.put(JointType.CMC_THUMB_LEFT, new Joint(JointType.CMC_THUMB_LEFT, new Vector3D(-3.2, 0.7, 0), new Rotation(0.788, 0, 0,
					0.615, false), joints.get(JointType.WRIST_LEFT)));
			joints.put(JointType.MCP_THUMB_LEFT, new Joint(JointType.MCP_THUMB_LEFT, new Vector3D(3.2, 4, 0), new Rotation(0.942, 0, 0,
					-0.333, false), joints.get(JointType.CMC_THUMB_LEFT)));
			joints.put(JointType.IP_THUMB_LEFT,
					new Joint(JointType.IP_THUMB_LEFT, new Vector3D(0, 3.9, 0), joints.get(JointType.MCP_THUMB_LEFT)));
			joints.put(JointType.BTIP_THUMB_LEFT,
					new Joint(JointType.BTIP_THUMB_LEFT, new Vector3D(0, 3.5, 0), joints.get(JointType.IP_THUMB_LEFT)));
		}
	}

	/**
	 * Generates the {@link Joint}s of the feet in dependence of to the chosen
	 * {@link ModelType}.
	 * 
	 * @param joints
	 *            Map containing the current {@link Joint}s
	 * @param torsoType
	 *            chosen {@link ModelType}
	 */
	private static void generateFeet(HashMap<JointType, Joint> joints, ModelType feetType, ModelType torsoType) {
		if (torsoType == ModelType.NONE) {
			joints.put(JointType.ANKLE_RIGHT, new Joint(JointType.ANKLE_RIGHT, new Vector3D(17.9, -75.8, 300), new Rotation(0, 1, 0, 0,
					false), null));
			joints.put(JointType.ANKLE_LEFT, new Joint(JointType.ANKLE_LEFT, new Vector3D(-17.9, -75.8, 300), new Rotation(0, 1, 0, 0,
					false), null));
		} else {
			joints.put(JointType.ANKLE_RIGHT, new Joint(JointType.ANKLE_RIGHT, new Vector3D(0, 54.8, 0), new Rotation(1, 0, 0, 0, false),
					joints.get(JointType.KNEE_RIGHT)));
			joints.put(JointType.ANKLE_LEFT, new Joint(JointType.ANKLE_LEFT, new Vector3D(0, 54.8, 0), new Rotation(1, 0, 0, 0, false),
					joints.get(JointType.KNEE_LEFT)));
		}

		joints.put(JointType.FOOT_CENTER_RIGHT, new Joint(JointType.FOOT_CENTER_RIGHT, new Vector3D(0.4, 5, 7.9), new Rotation(0.865,
				0.499, -0.015, 0.008, false), joints.get(JointType.ANKLE_RIGHT)));
		joints.put(JointType.FOOT_CENTER_LEFT, new Joint(JointType.FOOT_CENTER_LEFT, new Vector3D(-0.4, 5, 7.9), new Rotation(0.865, 0.499,
				0.015, -0.008, false), joints.get(JointType.ANKLE_LEFT)));

		if (feetType == ModelType.COMPLEX) {
			joints.put(JointType.HEEL_BONE_RIGHT, new Joint(JointType.HEEL_BONE_RIGHT, new Vector3D(0, 8, -2.5), new Rotation(0.989,
					-0.147, 0, 0, false), joints.get(JointType.ANKLE_RIGHT)));

			joints.put(JointType.TMT_SMALL_TOE_RIGHT, new Joint(JointType.TMT_SMALL_TOE_RIGHT, new Vector3D(3.5, 5, 7.3), new Rotation(
					0.873, 0.426, 0.209, -0.102, false), joints.get(JointType.ANKLE_RIGHT)));
			joints.put(JointType.MCP_SMALL_TOE_RIGHT, new Joint(JointType.MCP_SMALL_TOE_RIGHT, new Vector3D(-2, 9, 0), new Rotation(0.999,
					0, 0, 0.147, false), joints.get(JointType.TMT_SMALL_TOE_RIGHT)));
			joints.put(JointType.PIP_SMALL_TOE_RIGHT, new Joint(JointType.PIP_SMALL_TOE_RIGHT, new Vector3D(-0.4, 2.1, 1), new Rotation(
					0.935, 0.321, -0.048, 0.139, false), joints.get(JointType.MCP_SMALL_TOE_RIGHT)));
			joints.put(JointType.DIP_SMALL_TOE_RIGHT,
					new Joint(JointType.DIP_SMALL_TOE_RIGHT, new Vector3D(0, 1.1, 0), joints.get(JointType.PIP_SMALL_TOE_RIGHT)));
			joints.put(JointType.BTIP_SMALL_TOE_RIGHT,
					new Joint(JointType.BTIP_SMALL_TOE_RIGHT, new Vector3D(0, 1, 0), joints.get(JointType.DIP_SMALL_TOE_RIGHT)));

			joints.put(JointType.TMT_RING_TOE_RIGHT, new Joint(JointType.TMT_RING_TOE_RIGHT, new Vector3D(1.6, 5, 7.8), new Rotation(0.874,
					0.474, 0.091, -0.049, false), joints.get(JointType.ANKLE_RIGHT)));
			joints.put(JointType.MCP_RING_TOE_RIGHT, new Joint(JointType.MCP_RING_TOE_RIGHT, new Vector3D(-0.4, 9.7, 0), new Rotation(
					0.999, 0, 0, 0.017, false), joints.get(JointType.TMT_RING_TOE_RIGHT)));
			joints.put(JointType.PIP_RING_TOE_RIGHT, new Joint(JointType.PIP_RING_TOE_RIGHT, new Vector3D(-0.4, 2.1, 1), new Rotation(
					0.953, 0.282, -0.029, 0.1, false), joints.get(JointType.MCP_RING_TOE_RIGHT)));
			joints.put(JointType.DIP_RING_TOE_RIGHT,
					new Joint(JointType.DIP_RING_TOE_RIGHT, new Vector3D(0, 1.9, 0), joints.get(JointType.PIP_RING_TOE_RIGHT)));
			joints.put(JointType.BTIP_RING_TOE_RIGHT,
					new Joint(JointType.BTIP_RING_TOE_RIGHT, new Vector3D(0, 1.4, 0), joints.get(JointType.DIP_RING_TOE_RIGHT)));

			joints.put(JointType.TMT_MIDDLE_TOE_RIGHT, new Joint(JointType.TMT_MIDDLE_TOE_RIGHT, new Vector3D(0.4, 5, 7.9), new Rotation(
					0.865, 0.499, -0.015, 0.008, false), joints.get(JointType.ANKLE_RIGHT)));
			joints.put(JointType.MCP_MIDDLE_TOE_RIGHT, new Joint(JointType.MCP_MIDDLE_TOE_RIGHT, new Vector3D(0.7, 10.2, 0), new Rotation(
					0.999, 0, 0, -0.034, false), joints.get(JointType.TMT_MIDDLE_TOE_RIGHT)));
			joints.put(JointType.PIP_MIDDLE_TOE_RIGHT, new Joint(JointType.PIP_MIDDLE_TOE_RIGHT, new Vector3D(0, 2.8, 1.4), new Rotation(
					0.965, 0.258, 0, 0, false), joints.get(JointType.MCP_MIDDLE_TOE_RIGHT)));
			joints.put(JointType.DIP_MIDDLE_TOE_RIGHT,
					new Joint(JointType.DIP_MIDDLE_TOE_RIGHT, new Vector3D(0, 1.2, 0), joints.get(JointType.PIP_MIDDLE_TOE_RIGHT)));
			joints.put(JointType.BTIP_MIDDLE_TOE_RIGHT,
					new Joint(JointType.BTIP_MIDDLE_TOE_RIGHT, new Vector3D(0, 1.7, 0), joints.get(JointType.DIP_MIDDLE_TOE_RIGHT)));

			joints.put(JointType.TMT_LONG_TOE_RIGHT, new Joint(JointType.TMT_LONG_TOE_RIGHT, new Vector3D(-0.9, 5, 8), new Rotation(0.865,
					0.499, -0.030, 0.017, false), joints.get(JointType.ANKLE_RIGHT)));
			joints.put(JointType.MCP_LONG_TOE_RIGHT, new Joint(JointType.MCP_LONG_TOE_RIGHT, new Vector3D(0.4, 10.2, 0), new Rotation(
					0.999, 0, 0, -0.034, false), joints.get(JointType.TMT_LONG_TOE_RIGHT)));
			joints.put(JointType.PIP_LONG_TOE_RIGHT, new Joint(JointType.PIP_LONG_TOE_RIGHT, new Vector3D(0, 2.3, 1.2), new Rotation(0.965,
					0.258, 0, 0, false), joints.get(JointType.MCP_LONG_TOE_RIGHT)));
			joints.put(JointType.DIP_LONG_TOE_RIGHT,
					new Joint(JointType.DIP_LONG_TOE_RIGHT, new Vector3D(0, 1.5, 0), joints.get(JointType.PIP_LONG_TOE_RIGHT)));
			joints.put(JointType.BTIP_LONG_TOE_RIGHT,
					new Joint(JointType.BTIP_LONG_TOE_RIGHT, new Vector3D(0, 2.4, 0), joints.get(JointType.DIP_LONG_TOE_RIGHT)));

			joints.put(JointType.TMT_BIG_TOE_RIGHT, new Joint(JointType.TMT_BIG_TOE_RIGHT, new Vector3D(-2.6, 5, 8), new Rotation(0.855,
					0.493, -0.135, 0.078, false), joints.get(JointType.ANKLE_RIGHT)));
			joints.put(JointType.MTP_BIG_TOE_RIGHT, new Joint(JointType.MTP_BIG_TOE_RIGHT, new Vector3D(2.1, 10.1, 0), new Rotation(0.991,
					0, 0, -0.130, false), joints.get(JointType.TMT_BIG_TOE_RIGHT)));
			joints.put(JointType.IP_BIG_TOE_RIGHT, new Joint(JointType.IP_BIG_TOE_RIGHT, new Vector3D(0, 2.8, 1.4), new Rotation(0.965,
					0.258, 0, 0, false), joints.get(JointType.MTP_BIG_TOE_RIGHT)));
			joints.put(JointType.BTIP_BIG_TOE_RIGHT,
					new Joint(JointType.BTIP_BIG_TOE_RIGHT, new Vector3D(0, 3, 0), joints.get(JointType.IP_BIG_TOE_RIGHT)));

			joints.put(JointType.HEEL_BONE_LEFT, new Joint(JointType.HEEL_BONE_LEFT, new Vector3D(0, 8, -2.5), new Rotation(0.989, -0.147,
					0, 0, false), joints.get(JointType.ANKLE_LEFT)));

			joints.put(JointType.TMT_SMALL_TOE_LEFT, new Joint(JointType.TMT_SMALL_TOE_LEFT, new Vector3D(-3.5, 5, 7.3), new Rotation(
					0.873, 0.426, -0.209, 0.102, false), joints.get(JointType.ANKLE_LEFT)));
			joints.put(JointType.MCP_SMALL_TOE_LEFT, new Joint(JointType.MCP_SMALL_TOE_LEFT, new Vector3D(2, 9, 0), new Rotation(0.999, 0,
					0, -0.147, false), joints.get(JointType.TMT_SMALL_TOE_LEFT)));
			joints.put(JointType.PIP_SMALL_TOE_LEFT, new Joint(JointType.PIP_SMALL_TOE_LEFT, new Vector3D(0.4, 2.1, 1), new Rotation(0.935,
					0.321, 0.048, -0.139, false), joints.get(JointType.MCP_SMALL_TOE_LEFT)));
			joints.put(JointType.DIP_SMALL_TOE_LEFT,
					new Joint(JointType.DIP_SMALL_TOE_LEFT, new Vector3D(0, 1.1, 0), joints.get(JointType.PIP_SMALL_TOE_LEFT)));
			joints.put(JointType.BTIP_SMALL_TOE_LEFT,
					new Joint(JointType.BTIP_SMALL_TOE_LEFT, new Vector3D(0, 1, 0), joints.get(JointType.DIP_SMALL_TOE_LEFT)));

			joints.put(JointType.TMT_RING_TOE_LEFT, new Joint(JointType.TMT_RING_TOE_LEFT, new Vector3D(-1.6, 5, 7.8), new Rotation(0.874,
					0.474, -0.091, 0.049, false), joints.get(JointType.ANKLE_LEFT)));
			joints.put(JointType.MCP_RING_TOE_LEFT, new Joint(JointType.MCP_RING_TOE_LEFT, new Vector3D(0.4, 9.7, 0), new Rotation(0.999,
					0, 0, -0.017, false), joints.get(JointType.TMT_RING_TOE_LEFT)));
			joints.put(JointType.PIP_RING_TOE_LEFT, new Joint(JointType.PIP_RING_TOE_LEFT, new Vector3D(0.4, 2.1, 1), new Rotation(0.953,
					0.282, 0.029, -0.1, false), joints.get(JointType.MCP_RING_TOE_LEFT)));
			joints.put(JointType.DIP_RING_TOE_LEFT,
					new Joint(JointType.DIP_RING_TOE_LEFT, new Vector3D(0, 1.9, 0), joints.get(JointType.PIP_RING_TOE_LEFT)));
			joints.put(JointType.BTIP_RING_TOE_LEFT,
					new Joint(JointType.BTIP_RING_TOE_LEFT, new Vector3D(0, 1.4, 0), joints.get(JointType.DIP_RING_TOE_LEFT)));

			joints.put(JointType.TMT_MIDDLE_TOE_LEFT, new Joint(JointType.TMT_MIDDLE_TOE_LEFT, new Vector3D(-0.4, 5, 7.9), new Rotation(
					0.865, 0.499, 0.015, -0.008, false), joints.get(JointType.ANKLE_LEFT)));
			joints.put(JointType.MCP_MIDDLE_TOE_LEFT, new Joint(JointType.MCP_MIDDLE_TOE_LEFT, new Vector3D(-0.7, 10.2, 0), new Rotation(
					0.999, 0, 0, 0.034, false), joints.get(JointType.TMT_MIDDLE_TOE_LEFT)));
			joints.put(JointType.PIP_MIDDLE_TOE_LEFT, new Joint(JointType.PIP_MIDDLE_TOE_LEFT, new Vector3D(0, 2.8, 1.4), new Rotation(
					0.965, 0.258, 0, 0, false), joints.get(JointType.MCP_MIDDLE_TOE_LEFT)));
			joints.put(JointType.DIP_MIDDLE_TOE_LEFT,
					new Joint(JointType.DIP_MIDDLE_TOE_LEFT, new Vector3D(0, 1.2, 0), joints.get(JointType.PIP_MIDDLE_TOE_LEFT)));
			joints.put(JointType.BTIP_MIDDLE_TOE_LEFT,
					new Joint(JointType.BTIP_MIDDLE_TOE_LEFT, new Vector3D(0, 1.7, 0), joints.get(JointType.DIP_MIDDLE_TOE_LEFT)));

			joints.put(JointType.TMT_LONG_TOE_LEFT, new Joint(JointType.TMT_LONG_TOE_LEFT, new Vector3D(0.9, 5, 8), new Rotation(0.865,
					0.499, 0.030, -0.017, false), joints.get(JointType.ANKLE_LEFT)));
			joints.put(JointType.MCP_LONG_TOE_LEFT, new Joint(JointType.MCP_LONG_TOE_LEFT, new Vector3D(-0.4, 10.2, 0), new Rotation(0.999,
					0, 0, 0.034, false), joints.get(JointType.TMT_LONG_TOE_LEFT)));
			joints.put(JointType.PIP_LONG_TOE_LEFT, new Joint(JointType.PIP_LONG_TOE_LEFT, new Vector3D(0, 2.3, 1.2), new Rotation(0.965,
					0.258, 0, 0, false), joints.get(JointType.MCP_LONG_TOE_LEFT)));
			joints.put(JointType.DIP_LONG_TOE_LEFT,
					new Joint(JointType.DIP_LONG_TOE_LEFT, new Vector3D(0, 1.5, 0), joints.get(JointType.PIP_LONG_TOE_LEFT)));
			joints.put(JointType.BTIP_LONG_TOE_LEFT,
					new Joint(JointType.BTIP_LONG_TOE_LEFT, new Vector3D(0, 2.4, 0), joints.get(JointType.DIP_LONG_TOE_LEFT)));

			joints.put(JointType.TMT_BIG_TOE_LEFT, new Joint(JointType.TMT_BIG_TOE_LEFT, new Vector3D(2.6, 5, 8), new Rotation(0.855,
					0.493, 0.135, -0.078, false), joints.get(JointType.ANKLE_LEFT)));
			joints.put(JointType.MTP_BIG_TOE_LEFT, new Joint(JointType.MTP_BIG_TOE_LEFT, new Vector3D(-2.1, 10.1, 0), new Rotation(0.991,
					0, 0, 0.130, false), joints.get(JointType.TMT_BIG_TOE_LEFT)));
			joints.put(JointType.IP_BIG_TOE_LEFT, new Joint(JointType.IP_BIG_TOE_LEFT, new Vector3D(0, 2.8, 1.4), new Rotation(0.965,
					0.258, 0, 0, false), joints.get(JointType.MTP_BIG_TOE_LEFT)));
			joints.put(JointType.BTIP_BIG_TOE_LEFT,
					new Joint(JointType.BTIP_BIG_TOE_LEFT, new Vector3D(0, 3, 0), joints.get(JointType.IP_BIG_TOE_LEFT)));
		}
	}
}
