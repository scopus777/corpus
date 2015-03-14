package com.corpus.web.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.sensor.Sensor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**
 * Containes operations to create JSONs.
 * 
 * @author Matthias Weise
 * 
 */
public class JsonCreator {

	// mapper used to create the JSONs
	public static ObjectMapper hierarchicalMapper;
	private static ObjectMapper defaultMapper;

	// filter determine the serialized fields
	private static FilterProvider hierarchicalFilterProvider = new SimpleFilterProvider().addFilter("filter",
			SimpleBeanPropertyFilter.serializeAllExcept("parent", "defaultPosition", "defaultOrientation"));
	private static FilterProvider listFilterProvider = new SimpleFilterProvider().addFilter("filter",
			SimpleBeanPropertyFilter.serializeAllExcept("defaultPosition", "defaultOrientation", "children"));
	private static FilterProvider sensorFilterProvider = new SimpleFilterProvider().addFilter("filter",
			SimpleBeanPropertyFilter.serializeAllExcept("children", "currentData"));
	private static FilterProvider elapsedSceneFilterProvider = new SimpleFilterProvider().addFilter("filter",
			SimpleBeanPropertyFilter.serializeAllExcept("children", "currentData", "defaultPosition", "defaultOrientation"));

	// error messages
	private static String JSON_ERROR = "ERROR: Something went wrong during the json conversion process!";
	private static String INVALID_JOINT_TYPE = "ERROR: Invalid joint type!";
	private static String JOINT_NOT_IN_MODEL = "ERROR: The model does not contain a joint of the given joint type!";

	static {
		defaultMapper = new ObjectMapper();
		SimpleModule defaultModule = new SimpleModule();
		defaultModule.addSerializer(Vector3D.class, new Vector3DSerializer());
		defaultModule.addSerializer(Rotation.class, new RotationSerializer());
		defaultMapper.registerModule(defaultModule);

		hierarchicalMapper = new ObjectMapper();
		SimpleModule hierarchicalModule = new SimpleModule();
		hierarchicalModule.addSerializer(Vector3D.class, new Vector3DSerializer());
		hierarchicalModule.addSerializer(Rotation.class, new RotationSerializer());
		hierarchicalModule.addSerializer(ArrayList.class, new ChildrenSerializer());
		hierarchicalMapper.registerModule(hierarchicalModule);
	}

	/**
	 * Creates a hierarchical JSON representation of the full body model.
	 * 
	 * @param scene
	 *            {@link Scene} containing the body model.
	 * @return JSON String
	 */
	public static String getFullHierarchicalModelJSON(Scene scene) {
		try {
			return hierarchicalMapper.writer(hierarchicalFilterProvider).writeValueAsString(scene.getRootJoints());
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}

	/**
	 * Creates a JSON representation of the full body model in form of a list.
	 * 
	 * @param scene
	 *            {@link Scene} containing the body model.
	 * @return JSON String
	 */
	public static String getFullListModelJSON(Scene scene) {
		try {
			return defaultMapper.writer(listFilterProvider).writeValueAsString(scene.getJoints().values());
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}

	/**
	 * Creates a custom JSON representation of the body model in dependence of
	 * the given <code>type</code> and <code>fields</code>.
	 * 
	 * @param scene
	 *            {@link Scene} containing the body model.
	 * @param type
	 *            Determines the type (<code>hierarchical</code> or
	 *            <code>list</code>).
	 * @param fields
	 *            Determines which fields of the {@link Joint}-class are
	 *            serialized.
	 * @return JSON String
	 */
	public static String getCustomModelJSON(Scene scene, String type, List<String> fields) {
		FilterProvider customFilterProvider = new SimpleFilterProvider().addFilter("filter",
				SimpleBeanPropertyFilter.filterOutAllExcept(fields.toArray(new String[fields.size()])));
		try {
			if (type.equals("hierarchical"))
				return hierarchicalMapper.writer(customFilterProvider).writeValueAsString(scene.getRootJoints());
			else
				return defaultMapper.writer(customFilterProvider).writeValueAsString(scene.getJoints().values());
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}

	/**
	 * Creates a JSON representation of a the specified {@link Joint}s.
	 * 
	 * @param scene
	 *            {@link Scene} containing the body model.
	 * @param jointTypes
	 *            The {@link JointType}s of the serialized {@link Joint}s.
	 * @param fields
	 *            Determines which fields of the {@link Joint}-class are
	 *            serialized.
	 * @return JSON String
	 */
	public static String getJointsJSON(Scene scene, List<String> jointTypes, List<String> fields) {
		List<Joint> joints = new ArrayList<Joint>();
		try {
			for (String jointType : jointTypes) {
				Joint joint = scene.getJoints().get(JointType.valueOf(jointType));
				if (joint == null)
					return JOINT_NOT_IN_MODEL;
				else
					joints.add(joint);
			}
		} catch (IllegalArgumentException e) {
			return INVALID_JOINT_TYPE;
		}

		FilterProvider customFilterProvider = new SimpleFilterProvider().addFilter("filter",
				SimpleBeanPropertyFilter.filterOutAllExcept(fields.toArray(new String[fields.size()])));

		try {
			return defaultMapper.writer(customFilterProvider).writeValueAsString(joints);
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}

	/**
	 * Creates a JSON representation of the list of {@link Sensor}s.
	 * 
	 * @param scene
	 *            {@link Scene} containing the sensors
	 * @return JSON String
	 */
	public static String getSensorsJSON(Scene scene) {
		try {
			return defaultMapper.writer(sensorFilterProvider).writeValueAsString(scene.getSensors());
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}

	/**
	 * Creates a JSON representation of a {@link Scene} contained in the scene
	 * history.
	 * 
	 * @param scene
	 *            {@link Scene} which has to be serialized.
	 * @return JSON String
	 */
	public static String getElapsedSceneJSON(Scene scene) {
		Object[] list = new Object[] { scene.getJoints().values(), scene.getSensors() };
		try {
			return defaultMapper.writer(elapsedSceneFilterProvider).writeValueAsString(list);
		} catch (JsonProcessingException e) {
			return JSON_ERROR;
		}
	}
}
