package com.corpus.scene;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Represents a joint in a scene.
 * 
 * @author Matthias Weise
 * 
 */
@JsonFilter("filter")
public class Joint extends SceneNode {

	// default position - relative to the parent
	public static final Vector3D DEFAULT_POSITION = new Vector3D(0, 0, 0);
	// default orientation - relative to the parent
	public static final Rotation DEFAULT_ORIENTATION = new Rotation(1, 0, 0, 0, false);

	protected float positionConfidence;
	protected float orientationConfidence;
	protected JointType jointType;
	protected boolean positionTracked;
	protected boolean orientationTracked;
	protected Vector3D defaultPosition;
	protected Rotation defaultOrientation;
	protected ArrayList<SceneNode> children;

	/**
	 * Creates a joint. The position and the orientation will be initialized
	 * with the given default values.
	 * 
	 * @param jointType
	 * @param defaultPosition
	 * @param defaultOrientation
	 * @param parent
	 */
	public Joint(JointType jointType, Vector3D defaultPosition, Rotation defaultOrientation, Joint parent) {
		super(defaultPosition, defaultOrientation, parent);
		this.jointType = jointType;
		this.defaultPosition = defaultPosition;
		this.defaultOrientation = defaultOrientation;
	}

	/**
	 * Creates a joint. The position and the orientation will be initialized
	 * with the given default values. Additionally sets the confidence values
	 * 
	 * @param jointType
	 * @param defaultPosition
	 * @param defaultOrientation
	 * @param parent
	 * @param positionConfidence
	 * @param orientationConfidence
	 */
	public Joint(JointType jointType, Vector3D defaultPosition, Rotation defaultOrientation, Joint parent, float positionConfidence,
			float orientationConfidence) {
		this(jointType, defaultPosition, defaultOrientation, parent);
		this.positionConfidence = positionConfidence;
		this.orientationConfidence = orientationConfidence;
	}

	/**
	 * Creates a joint with the default orientation.
	 * 
	 * @param jointType
	 * @param defaultPosition
	 * @param parent
	 */
	public Joint(JointType jointType, Vector3D defaultPosition, Joint parent) {
		this(jointType, defaultPosition, DEFAULT_ORIENTATION, parent);
	}

	/**
	 * Creates a joint with the given absolute values and no parent.
	 * 
	 * @param jointType
	 * @param absolutePosition
	 * @param absoluteOrientation
	 */
	public Joint(JointType jointType, Vector3D absolutePosition, Rotation absoluteOrientation) {
		super(absolutePosition, absoluteOrientation);
		this.jointType = jointType;
	}

	/**
	 * Empty C'tor for to clone the joint.
	 * 
	 */
	private Joint() {
	}

	/**
	 * Sets the position and the orientation of this joint to the default
	 * values.
	 * 
	 */
	public void setToDefault() {
		this.relativePosition = defaultPosition;
		this.relativeOrientation = defaultOrientation;
	}

	/**
	 * Returns the confidence of the tracked position.
	 * 
	 * @return confidence of the tracked position
	 */
	public float getPositionConfidence() {
		return positionConfidence;
	}

	/**
	 * Sets the confidence of the tracked position. Should be a value between 0
	 * and 1.
	 * 
	 * @param positionConfidence
	 *            confidence of the tracked position
	 */
	public void setPositionConfidence(float positionConfidence) {
		this.positionConfidence = positionConfidence;
	}

	/**
	 * Returns the confidence of the tracked orientation.
	 * 
	 * @return confidence of the tracked orientation
	 */
	public float getOrientationConfidence() {
		return orientationConfidence;
	}

	/**
	 * Sets the confidence of the tracked orientation. Should be a value between
	 * 0 and 1.
	 * 
	 * @param orientationConfidence
	 *            confidence of the tracked orientation
	 */
	public void setOrientationConfidence(float orientationConfidence) {
		this.orientationConfidence = orientationConfidence;
	}

	/**
	 * Returns the type of the joint.
	 * 
	 * @return joint type
	 */
	public JointType getJointType() {
		return jointType;
	}

	/**
	 * Returns whether the joint is tracked by at least one sensor.
	 * 
	 * @return joint type
	 */
	public boolean getPositionTracked() {
		return positionTracked;
	}

	/**
	 * Sets whether the joint is tracked by at least one sensor.
	 * 
	 * @param isTracked
	 */
	public void setPositionTracked(boolean isTracked) {
		this.positionTracked = isTracked;
	}

	/**
	 * Returns whether the joint is tracked by at least one sensor.
	 * 
	 * @return joint type
	 */
	public boolean getOrientationTracked() {
		return orientationTracked;
	}

	/**
	 * Sets whether the joint is tracked by at least one sensor.
	 * 
	 * @param isTracked
	 */
	public void setOrientationTracked(boolean isTracked) {
		this.orientationTracked = isTracked;
	}

	/**
	 * Returns the default position.
	 * 
	 * @return default position
	 */
	public Vector3D getDefaultPosition() {
		return defaultPosition;
	}

	/**
	 * Returns the default orientation.
	 * 
	 * @return default orientation
	 */
	public Rotation getDefaultOrientation() {
		return defaultOrientation;
	}

	/**
	 * Returns the children list.
	 * 
	 * @return children
	 */
	public ArrayList<SceneNode> getChildren() {
		if (children == null)
			children = new ArrayList<SceneNode>();
		return children;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Position: ");
		sb.append(relativePosition.toString());
		sb.append("  Orientation: (w=");
		sb.append(relativeOrientation.getQ0());
		sb.append(" x=");
		sb.append(relativeOrientation.getQ1());
		sb.append(" y=");
		sb.append(relativeOrientation.getQ2());
		sb.append(" z=");
		sb.append(relativeOrientation.getQ3());
		sb.append(")  Type: ");
		sb.append(jointType.toString());
		sb.append("\n");
		for (SceneNode child : getChildren()) {
			sb.append(child.toString());
		}
		return sb.toString();
	}

	@Override
	public Joint clone(Joint parent) {
		Joint newJoint = new Joint();
		newJoint.setParent(parent);
		newJoint.jointType = this.jointType;
		newJoint.defaultPosition = this.defaultPosition;
		newJoint.absolutePosition = this.absolutePosition;
		newJoint.relativePosition = this.relativePosition;
		newJoint.positionTimestamp = this.positionTimestamp;
		newJoint.positionConfidence = this.positionConfidence;
		newJoint.positionTracked = this.positionTracked;
		newJoint.defaultOrientation = this.defaultOrientation;
		newJoint.absoluteOrientation = this.absoluteOrientation;
		newJoint.relativeOrientation = this.relativeOrientation;
		newJoint.orientationTimestamp = this.orientationTimestamp;
		newJoint.orientationConfidence = this.orientationConfidence;
		newJoint.orientationTracked = this.orientationTracked;
		return newJoint;
	}
}
