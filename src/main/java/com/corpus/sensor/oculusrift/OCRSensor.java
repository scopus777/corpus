package com.corpus.sensor.oculusrift;

import static com.oculusvr.capi.OvrLibrary.ovrHmdType.ovrHmd_DK1;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.ovrTrackingCap_Orientation;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.ovrTrackingCap_Position;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.sensor.Sensor;
import com.corpus.sensor.SensorInitializationException;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.OvrLibrary.ovrStatusBits;
import com.oculusvr.capi.OvrQuaternionf;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.TrackingState;

/**
 * <p>
 * Wrapper for the <i>Oculus Rift Development Kit 1</i> and <i>Development Kit
 * 2</i>. If the Development Kit 1 is used, only the tracking of the orientation
 * is supported. To communicate with the Rift the <i>jovr</i> library is used.
 * </p>
 * 
 * <p>
 * The wrapper updates the current data in the
 * {@link Sensor#updateCurrentData() updateCurrentData} method.
 * </p>
 * 
 * @see <a href="https://www.oculus.com">Oculus Rift</a>
 * @see <a href="https://github.com/jherico/jovr">jovr</a>
 * 
 * @author Matthias Weise
 * 
 */
public class OCRSensor extends Sensor {

	private Hmd hmd;

	public OCRSensor(Map<String, String> arguments) {
		super(arguments);
	}

	@Override
	public void run() {
	}

	@Override
	public void terminate() {
		hmd.destroy();
		Hmd.shutdown();
	}

	@Override
	public void init() {
		Hmd.initialize();

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			throw new SensorInitializationException("Unable initialize the OCRSensor.");
		}

		hmd = openFirstHmd();
		if (null == hmd) {
			throw new SensorInitializationException("Unable initialize the OCRSensor.");
		}

		if (0 == hmd.configureTracking(ovrTrackingCap_Orientation | ovrTrackingCap_Position, 0)) {
			throw new SensorInitializationException("Unable to start the OCRSensor.");
		}

	}

	private static Hmd openFirstHmd() {
		Hmd hmd = Hmd.create(0);
		if (null == hmd) {
			hmd = Hmd.createDebug(ovrHmd_DK1);
		}
		return hmd;
	}

	@Override
	protected void updateCurrentData() {
		TrackingState state = hmd.getSensorState(Hmd.getTimeInSeconds());
		OvrVector3f ovrPos = state.HeadPose.Pose.Position;

		// Tracking origin is set located 100 cm away from the camera by
		// default
		Vector3D pos = new Vector3D(ovrPos.x * 100, ovrPos.y * 100, (ovrPos.z * 100) + 100);
		OvrQuaternionf ovrOri = state.HeadPose.Pose.Orientation;

		// Turn head to sensor
		Rotation ori = new Rotation(0f, 0f, 1f, 0f, false).applyTo(new Rotation(ovrOri.w, ovrOri.x, ovrOri.y, ovrOri.z, true));

		HashMap<JointType, Joint> currentData = new HashMap<JointType, Joint>();
		Joint joint = new Joint(JointType.HEAD, pos, ori);

		if (positionConfidence > 0)
			joint.setPositionConfidence(positionConfidence);
		else {
			if ((state.StatusFlags & ovrStatusBits.ovrStatus_PositionTracked) != 0)
				joint.setPositionConfidence(1);
			else
				joint.setPositionConfidence(0);
		}

		if (orientationConfidence > 0)
			joint.setOrientationConfidence(orientationConfidence);
		else {
			if ((state.StatusFlags & ovrStatusBits.ovrStatus_OrientationTracked) != 0)
				joint.setOrientationConfidence(1);
			else
				joint.setOrientationConfidence(0);
		}

		currentData.put(JointType.HEAD, joint);
		setCurrentData(currentData);
	}

}
