package com.corpus.scene;

import java.util.Calendar;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents a node in a {@link Scene}.
 * 
 * @author Matthias Weise
 * 
 */
public abstract class SceneNode {

	protected Vector3D relativePosition;
	protected Rotation relativeOrientation;
	protected Vector3D absolutePosition;
	protected Rotation absoluteOrientation;
	protected Calendar positionTimestamp;
	protected Calendar orientationTimestamp;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "jointType")
	@JsonIdentityReference(alwaysAsId = true)
	protected Joint parent;

	/**
	 * Creates a SceneNode with the given relative position, relative
	 * orientation and parent and determines the absolute position and
	 * orientation of the node.
	 * 
	 * @param relativePosition
	 * @param relativeOrientation
	 * @param parent
	 */
	public SceneNode(Vector3D relativePosition, Rotation relativeOrientation, Joint parent) {
		setParent(parent);
		this.relativePosition = relativePosition;
		this.relativeOrientation = relativeOrientation;
		this.positionTimestamp = Calendar.getInstance();
		this.orientationTimestamp = Calendar.getInstance();
	}

	/**
	 * Creates a SceneNode with the given absolute position and orientation and
	 * determines the relative position and orientation. Because this SceneNode
	 * has no parent the relative values are equal to the absolute values.
	 * 
	 * @param absolutePosition
	 * @param absoluteOrientation
	 */
	public SceneNode(Vector3D absolutePosition, Rotation absoluteOrientation) {
		setAbsolutePosition(absolutePosition);
		setAbsoluteOrientation(absoluteOrientation);
		this.positionTimestamp = Calendar.getInstance();
		this.orientationTimestamp = Calendar.getInstance();
	}

	/**
	 * Empty C'tor to clone the SceneNode.
	 * 
	 */
	protected SceneNode() {
	}

	/**
	 * Returns the relative position in dependence to the orientation and
	 * position of the parent.
	 * 
	 * @return relative position
	 */
	public Vector3D getRelativePosition() {
		if (relativePosition == null && absolutePosition != null) {
			if (parent != null && parent.getAbsoluteOrientation() != null && parent.getAbsolutePosition() != null)
				this.relativePosition = parent.getAbsoluteOrientation().applyTo(absolutePosition.subtract(parent.getAbsolutePosition()));
			else
				this.relativePosition = absolutePosition;
		}
		return relativePosition;
	}

	/**
	 * Sets the relative position in dependence to the orientation and position
	 * of the parent.
	 * 
	 * @param relativePosition
	 */
	public void setRelativePosition(Vector3D relativePosition) {
		if (relativePosition == null)
			this.absolutePosition = this.getAbsolutePosition();
		else
			this.absolutePosition = null;
		this.relativePosition = relativePosition;
	}

	/**
	 * Returns the relative orientation in dependence to the orientation of the
	 * parent.
	 * 
	 * @return relative orientation
	 */
	public Rotation getRelativeOrientation() {
		if (relativeOrientation == null && absoluteOrientation != null) {
			if (parent != null && parent.getAbsoluteOrientation() != null)
				this.relativeOrientation = absoluteOrientation.applyTo(parent.getAbsoluteOrientation().revert());
			else
				this.relativeOrientation = absoluteOrientation;
		}
		return relativeOrientation;
	}

	/**
	 * Sets the relative orientation in dependence to the orientation of the
	 * parent.
	 * 
	 * @param relativeOrientation
	 */
	public void setRelativeOrientation(Rotation relativeOrientation) {
		if (relativeOrientation == null)
			this.absoluteOrientation = this.getAbsoluteOrientation();
		else
			this.absoluteOrientation = null;
		this.relativeOrientation = relativeOrientation;
	}

	/**
	 * Returns the absolute position of the node.
	 * 
	 * @return absolute position
	 */
	public Vector3D getAbsolutePosition() {
		if (absolutePosition == null && relativePosition != null) {
			if (parent != null && parent.getAbsolutePosition() != null && parent.getAbsoluteOrientation() != null)
				absolutePosition = parent.getAbsolutePosition().add(parent.getAbsoluteOrientation().applyInverseTo(relativePosition));
			else
				absolutePosition = relativePosition;
		}
		return absolutePosition;
	}

	/**
	 * Sets the absolute position of the node.
	 * 
	 * @param absolutePosition
	 */
	public void setAbsolutePosition(Vector3D absolutePosition) {
		if (absolutePosition == null)
			this.relativePosition = this.getRelativePosition();
		else
			this.relativePosition = null;
		this.absolutePosition = absolutePosition;
	}

	/**
	 * Returns the absolute orientation of the node.
	 * 
	 * @return absolute orientation
	 */
	public Rotation getAbsoluteOrientation() {
		if (absoluteOrientation == null && relativeOrientation != null) {
			if (parent != null && parent.getAbsoluteOrientation() != null)
				absoluteOrientation = relativeOrientation.applyTo(parent.getAbsoluteOrientation());
			else
				absoluteOrientation = relativeOrientation;
		}
		return absoluteOrientation;
	}

	/**
	 * Sets the absolute orientation of the node.
	 * 
	 * @param absoluteOrientation
	 */
	public void setAbsoluteOrientation(Rotation absoluteOrientation) {
		if (absoluteOrientation == null)
			this.relativeOrientation = this.getRelativeOrientation();
		else
			this.relativeOrientation = null;
		this.absoluteOrientation = absoluteOrientation;
	}

	/**
	 * Returns the last time when the position of the joint was tracked.
	 * 
	 * @return tracking point in time
	 */
	public Calendar getPositionTimestamp() {
		return positionTimestamp;
	}

	/**
	 * Sets the last time when the position of the joint was tracked.
	 * 
	 * @param timestamp
	 */
	public void setPositionTimestamp(Calendar timestamp) {
		this.positionTimestamp = timestamp;
	}

	/**
	 * Returns the last time when the orientation of the joint was tracked.
	 * 
	 * @return tracking point in time
	 */
	public Calendar getOrientationTimestamp() {
		return orientationTimestamp;
	}

	/**
	 * Sets the last time when the orientation of the joint was tracked.
	 * 
	 * @param timestamp
	 */
	public void setOrientationTimestamp(Calendar orientationTimestamp) {
		this.orientationTimestamp = orientationTimestamp;
	}

	/**
	 * Returns the parent.
	 * 
	 * @return parent
	 */
	public Joint getParent() {
		return parent;
	}

	/**
	 * Removes this node from the children list of the old parent, sets the new
	 * parent and adds this node to the children list of the new parent.
	 * 
	 * @param parent
	 *            new parent
	 */
	public void setParent(Joint parent) {
		if (this.parent != null && this.parent.getChildren().contains(this))
			parent.getChildren().remove(this);
		this.parent = parent;
		if (this.parent != null)
			if (!this.parent.getChildren().contains(this))
				this.parent.getChildren().add(this);
	}

	/**
	 * Clones the {@link SceneNode}
	 * 
	 * @param parent
	 * @return Cloned {@link SceneNode}
	 */
	public abstract SceneNode clone(Joint parent);

}
