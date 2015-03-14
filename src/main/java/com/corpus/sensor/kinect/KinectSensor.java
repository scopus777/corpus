package com.corpus.sensor.kinect;

import java.util.HashMap;
import java.util.Map;

import kinect.Kinect;
import kinect.KinectObserver;
import kinect.skeleton.Skeleton;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.sensor.Sensor;

/**
 * <p>
 * A wrapper for the <i>Microsoft Kinect</i>. Extends the {@link Sensor} class.
 * To communicate with the Kinect the <i>Kinect-for-Java</i> is used. The
 * library was extended to enable a direct access to the orientations provided
 * by the Kinect.
 * </p>
 * <p>
 * The wrapper can update the current data through an event-based system or
 * directly in the {@link Sensor#updateCurrentData() updateCurrentData} method.
 * The update method depends on the field<code>eventBased</code> which can be
 * set by the user through the configuration file.
 * </p>
 * <p>
 * The used confidence values are based on the experiences with the sensor.
 * </p>
 * <p>
 * It is possible to use a {@link KinectDebugWindow} which shows the video
 * recording of the Kinect. The oppening of the window depends on the
 * <code>showDebugWindow</code> field which can be set though the configuration
 * file. The window is also able to draw the bones (<code>drawBones</code>) and
 * orientations (<code>drawCoordinateSystems</code>) of the body model.
 * </p>
 * <p>
 * Only the Kinect V1 is supported. The wrapper uses windows drivers and
 * therefore only runs on windows systems.
 * </p>
 * 
 * @see <a href="http://www.microsoft.com/en-us/kinectforwindows">Microsoft
 *      Kinect</a>
 * @see <a
 *      href="https://github.com/ccgimperial/Kinect-for-Java">Kinect-for-Java</a>
 * 
 * 
 * @author Matthias Weise
 * 
 */
public class KinectSensor extends Sensor implements KinectObserver {

	private boolean eventBased;
	private boolean showDebugWindow;
	private KinectDebugWindow debugWindow;
	private Thread debugWindowThread;

	public KinectSensor(Map<String, String> arguments) {
		super(arguments);
	}

	@Override
	public void init() {
		eventBased = Boolean.parseBoolean(arguments.get("eventBased"));
		showDebugWindow = Boolean.parseBoolean(arguments.get("showDebugWindow"));
		if (showDebugWindow) {
			boolean drawBones = Boolean.parseBoolean(arguments.get("drawBones"));
			boolean drawCoordinateSystems = Boolean.parseBoolean(arguments.get("drawCoordinateSystems"));
			debugWindow = new KinectDebugWindow(drawBones, drawCoordinateSystems);
			debugWindowThread = new Thread(debugWindow);
			debugWindowThread.start();
		}
	}

	@Override
	public void run() {
		Kinect.init(this);
	}

	@Override
	public void terminate() {
		if (showDebugWindow) {
			debugWindow.terminate();
			try {
				debugWindowThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Kinect.terminate();
	}

	@Override
	protected void updateCurrentData() {
		if (!eventBased)
			getDataFromKinect();
	}

	@Override
	public void DepthEvent() {
	}

	@Override
	public void VideoEvent() {
	}

	@Override
	public void SkeletonEvent() {
		if (eventBased)
			getDataFromKinect();
	}

	/**
	 * Collects the current data from the kinect.
	 * 
	 */
	private void getDataFromKinect() {
		HashMap<JointType, Joint> currentData = new HashMap<JointType, Joint>();
		if (Skeleton.isTrackingSomeSkeleton()) {
			Skeleton skeleton = Skeleton.getTrackedSkeleton();
			float[][] orientations = Skeleton.getJointOrientations();
			currentData.put(JointType.ANKLE_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_ANKLE_LEFT));
			currentData.put(JointType.ANKLE_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_ANKLE_RIGHT));
			currentData.put(JointType.ELBOW_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_ELBOW_LEFT));
			currentData.put(JointType.ELBOW_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_ELBOW_RIGHT));
			currentData.put(JointType.FOOT_CENTER_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_FOOT_LEFT));
			currentData.put(JointType.FOOT_CENTER_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_FOOT_RIGHT));
			currentData.put(JointType.HAND_CENTER_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_HAND_LEFT));
			currentData.put(JointType.HAND_CENTER_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_HAND_RIGHT));
			currentData.put(JointType.HEAD, createJoint(skeleton, orientations, Skeleton.POSITION_HEAD));
			currentData.put(JointType.SPINE_BASE, createJoint(skeleton, orientations, Skeleton.POSITION_HIP_CENTER));
			currentData.put(JointType.HIP_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_HIP_LEFT));
			currentData.put(JointType.HIP_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_HIP_RIGHT));
			currentData.put(JointType.KNEE_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_KNEE_LEFT));
			currentData.put(JointType.KNEE_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_KNEE_RIGHT));
			currentData.put(JointType.SPINE_SHOULDER, createJoint(skeleton, orientations, Skeleton.POSITION_SHOULDER_CENTER));
			currentData.put(JointType.SHOULDER_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_SHOULDER_LEFT));
			currentData.put(JointType.SHOULDER_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_SHOULDER_RIGHT));
			currentData.put(JointType.WRIST_LEFT, createJoint(skeleton, orientations, Skeleton.POSITION_WRIST_LEFT));
			currentData.put(JointType.WRIST_RIGHT, createJoint(skeleton, orientations, Skeleton.POSITION_WRIST_RIGHT));

		}
		setCurrentData(currentData);
	}

	/**
	 * Creates a {@link Joint} containing the corresponding data tracked by the
	 * Kinect.
	 * 
	 * @param skeleton
	 *            tracked {@link Skeleton}
	 * @param orientations
	 *            Two dimensional array containing the orientations of the
	 *            joints.
	 * @param jointId
	 *            ID of the joint.
	 * @return created {@link Joint}
	 */
	private Joint createJoint(Skeleton skeleton, float[][] orientations, int jointId) {
		kinect.skeleton.Joint joint = new kinect.skeleton.Joint(skeleton, jointId);
		kinect.geometry.Position jointPos = joint.getPosition();
		Vector3D pos = new Vector3D(jointPos.x * 100f, jointPos.y * 100f, jointPos.z * 100f);
		float[] value = orientations[jointId];
		Rotation ori = null;
		if (value != null)
			ori = new Rotation(value[3], value[0], value[1], value[2], false);
		Joint result = new Joint(null, pos, ori);

		// set confidence values
		if (positionConfidence <= 0)
			switch (joint.getTrackingState()) {
			case kinect.skeleton.Joint.POSITION_TRACKED:
				result.setPositionConfidence(0.5f);
				break;
			case kinect.skeleton.Joint.POSITION_INFERRED:
				result.setPositionConfidence(0.25f);
				break;
			default:
				result.setPositionConfidence(0);
				break;
			}
		else
			result.setPositionConfidence(positionConfidence);
		if (orientationConfidence <= 0)
			switch (joint.getTrackingState()) {
			case kinect.skeleton.Joint.POSITION_TRACKED:
				result.setOrientationConfidence(0.2f);
				break;
			case kinect.skeleton.Joint.POSITION_INFERRED:
				result.setOrientationConfidence(0.1f);
				break;
			default:
				result.setOrientationConfidence(0);
				break;
			}
		else
			result.setPositionConfidence(orientationConfidence);

		return result;
	}
}
