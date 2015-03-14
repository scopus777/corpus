package com.corpus.sensor.myo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Quaternion;

// TODO: cannot handle multiple myos for now
/**
 * Listener for the Myo reacting to new data.
 * 
 * @author Matthias Weise
 * 
 */
public class MyoListener extends AbstractDeviceListener {

	// tracked rotation
	volatile Rotation currentRotation;

	// type of the joint for which the myo is collecting data
	JointType parentType;

	// direction of the myo
	boolean usbOrientationToEllbow;

	// differing confidence set by the user
	float orientationConfidence;

	/**
	 * C'tor determining the <code>parentType</code>,
	 * <code>usbOrientationToEllbow</code> and
	 * <code>orientationConfidence</code>.
	 * 
	 * @param parentType
	 * @param usbOrientationToEllbow
	 * @param orientationConfidence
	 */
	public MyoListener(JointType parentType, boolean usbOrientationToEllbow, float orientationConfidence) {
		super();
		this.parentType = parentType;
		this.usbOrientationToEllbow = usbOrientationToEllbow;
	}

	@Override
	public void onOrientationData(Myo myo, long timestamp, Quaternion rot) {
		currentRotation = new Rotation(rot.getW(), rot.getX(), rot.getY(), rot.getZ(), true);
	}

	@Override
	public void onDisconnect(Myo myo, long timestamp) {
		currentRotation = null;
	}

	/**
	 * Maps the rotation to a corresponding {@link Joint}. Applies a additional
	 * rotation in dependence of the parent joint and the orientation of the usb
	 * connector to match the orientation used in the framework.
	 * 
	 * @return {@link Map} containing the {@link Joint}.
	 */
	public Map<JointType, Joint> getCurrentData() {
		Map<JointType, Joint> currentData = new HashMap<JointType, Joint>();
		Rotation rot = currentRotation;
		if (rot != null) {
			Rotation myoRotation = new Rotation(0.5f, 0.5f, -0.5f, 0.5f, false);
			if (parentType == JointType.WRIST_RIGHT) {
				if (!usbOrientationToEllbow)
					myoRotation = new Rotation(-0.5f, 0.5f, 0.5f, 0.5f, false);
			} else if (parentType == JointType.WRIST_LEFT) {
				if (usbOrientationToEllbow)
					myoRotation = new Rotation(-0.5f, -0.5f, -0.5f, 0.5f, false);
				else
					myoRotation = new Rotation(0.5f, -0.5f, 0.5f, 0.5f, false);
			}
			rot = myoRotation.applyTo(rot).applyTo(new Rotation(0.5, -0.5, -0.5, -0.5, false));
			Joint newJoint = new Joint(parentType, null, rot);
			if (orientationConfidence > 0)
				newJoint.setOrientationConfidence(orientationConfidence);
			else
				newJoint.setOrientationConfidence(0.75f);
			currentData.put(parentType, newJoint);
		}
		return currentData;
	}
}
