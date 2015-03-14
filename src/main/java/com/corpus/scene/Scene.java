package com.corpus.scene;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.corpus.sensor.Sensor;

/**
 * The scene represents the position of the joints and sensors at a specific
 * point in time.
 * 
 * @author Scopus
 * 
 */
public class Scene implements Cloneable {

	private List<Joint> rootJoints;
	private List<Sensor> sensors;
	private Map<JointType, Joint> joints;
	private List<SceneNode> rootNodes;
	private Calendar timestamp;

	/**
	 * Creates a empty scene.
	 * 
	 */
	public Scene() {
		joints = new HashMap<JointType, Joint>();
		sensors = new ArrayList<Sensor>();
	}

	/**
	 * Returns the root {@link Joint}s (no parents). Determines these joints if
	 * it isn't already done.
	 * 
	 * @return root {@link Joint}s
	 */
	public List<Joint> getRootJoints() {
		if (rootJoints == null) {
			rootJoints = new ArrayList<Joint>();
			for (Joint joint : joints.values())
				if (joint.parent == null)
					rootJoints.add(joint);
		}
		return rootJoints;
	}

	/**
	 * Returns the root {@link SceneNodes}s. Determines these nodes if it is not
	 * already done.
	 * 
	 * @return root {@link SceneNodes}s
	 */
	public List<SceneNode> getRootNodes() {
		if (rootNodes == null) {
			rootNodes = new ArrayList<SceneNode>();
			rootNodes.addAll(getRootJoints());
			for (Sensor sensor : sensors)
				if (sensor.parent == null)
					rootNodes.add(sensor);
		}
		return rootNodes;
	}

	/**
	 * Resets the root nodes.
	 */
	public void resetRootNodes() {
		rootNodes = null;
	}

	/**
	 * Returns the sensors placed in the scene.
	 * 
	 * @return sensors
	 */
	public List<Sensor> getSensors() {
		return sensors;
	}

	/**
	 * Sets the sensors of the scene.
	 * 
	 * @param sensors
	 */
	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	/**
	 * Returns the tracked joints.
	 * 
	 * @return
	 */
	public Map<JointType, Joint> getJoints() {
		return joints;
	}

	/**
	 * Sets the tracked joints.
	 * 
	 * @param joints
	 */
	public void setJoints(HashMap<JointType, Joint> joints) {
		this.joints = joints;
	}

	/**
	 * Returns the timestamp.
	 * 
	 * @return timestamp
	 */
	public Calendar getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 * 
	 * @param timestamp
	 */
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public Scene clone() {
		Scene sceneCopy = new Scene();
		sceneCopy.timestamp = this.timestamp;
		for (SceneNode joint : getRootNodes()) {
			cloneRecursively(null, joint, sceneCopy);
		}
		return sceneCopy;
	}

	/**
	 * Clones a {@link SceneNode} and its children. The cloned nodes will be
	 * added to the {@link Joint}s or {@link Sensor}s of the given {@link Scene}
	 * .
	 * 
	 * @param parent
	 * @param node
	 * @param sceneCopy
	 */
	private void cloneRecursively(Joint parent, SceneNode node, Scene sceneCopy) {
		SceneNode clone = node.clone(parent);
		if (node instanceof Joint) {
			Joint jointClone = (Joint) clone;
			sceneCopy.joints.put(jointClone.getJointType(), jointClone);
			for (SceneNode child : ((Joint) node).getChildren()) {
				cloneRecursively(jointClone, child, sceneCopy);
			}
		} else if (node instanceof Sensor)
			sceneCopy.sensors.add((Sensor) clone);
	}

}
