package com.corpus.configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.corpus.controller.SceneController;
import com.corpus.filter.Filter;
import com.corpus.fuser.ConfidenceWeightedFuser;
import com.corpus.fuser.Fuser;
import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.ModelInitializer;
import com.corpus.scene.Scene;
import com.corpus.scene.SceneNode;
import com.corpus.sensor.Sensor;

/**
 * This class is responsible for parsing the configuration file and initializing
 * the application.
 * 
 * @author Matthias Weise
 * 
 */
public class ConfigurationParser {

	/**
	 * Parses the configuration file and initializes the start {@link Scene} and
	 * the {@link SceneController}.
	 * 
	 * @param configFile
	 *            The path to the configuration file
	 * @param currentScene
	 *            The start {@link Scene}
	 * @throws ParserConfigurationException
	 * @throws InterruptedException
	 * @throws XMLStreamException
	 *             If there went something wrong during the parsing process of
	 *             the configuration file, this Exception is thrown, containing
	 *             all information the user needs. The application should be
	 *             closed after this.
	 */
	public static void readConfigAndInitSceneController(String configFile) throws XMLStreamException, InterruptedException,
			ParserConfigurationException {

		// create start scene
		Scene startScene = new Scene();

		// open the configuration file
		File file = new File(configFile);
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = null;
		try {
			doc = dBuilder.parse(file);
		} catch (SAXException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(
					"Configuration file not found. Restart the server with the path to the config file as the first argument.");
		}

		// get the config element
		NodeList nodeList = doc.getElementsByTagName("config");
		if (nodeList.getLength() != 1)
			throw new XMLStreamException("There has to be exactly one config tag in the config file.");
		Element configElement = (Element) nodeList.item(0);

		// determine the base URI
		com.corpus.controller.Controller.BASE_URI = getRequiredNode("baseURI", configElement).getFirstChild().getNodeValue();

		// get the additional search paths for implementable classes
		Node node = getUnrequiredNode("additionalSearchPaths", configElement);
		URL[] additionalPaths = null;
		if (node != null)
			additionalPaths = parseAdditionalPath((Element) node);
		else
			additionalPaths = new URL[0];

		// create body model in dependence of the chosen types
		node = getRequiredNode("modelType", configElement);
		ModelType torsoType = ModelType.valueOf(getRequiredNode("torso", configElement).getFirstChild().getNodeValue());
		ModelType handsType = ModelType.valueOf(getRequiredNode("hands", configElement).getFirstChild().getNodeValue());
		ModelType feetType = ModelType.valueOf(getRequiredNode("feet", configElement).getFirstChild().getNodeValue());
		HashMap<JointType, Joint> joints = ModelInitializer.initializeModel(torsoType, handsType, feetType);

		// add custom joints
		parseAndAddJoints(configElement, joints);
		if (joints.isEmpty())
			System.err.println("WARNING: The body model is empty!");
		startScene.setJoints(joints);

		// determine update rate
		node = getUnrequiredNode("framesPerSecond", configElement);
		if (node != null) {
			int updateFrequency = Integer.parseInt(node.getFirstChild().getNodeValue());
			if (updateFrequency <= 0)
				throw new XMLStreamException("The update frequency should be greater than 0.");
			com.corpus.controller.Controller.UPDATE_FREQUENCY = updateFrequency;
		}

		// get the length of time until joints will return to their default
		// position and orientation
		node = getUnrequiredNode("returnToDefault", configElement);
		if (node != null) {
			int returnToDefault = Integer.parseInt(node.getFirstChild().getNodeValue());
			if (returnToDefault < 0)
				throw new XMLStreamException("The option returnToDefault should be set to a value greater than or equal to 0.");
			com.corpus.controller.Controller.RETURN_TO_DEFAULT = returnToDefault;
		}

		// get the fuser
		node = getRequiredNode("fuser", configElement);
		Fuser fuser = node != null ? parseFuser((Element) node, additionalPaths) : new ConfidenceWeightedFuser(
				new HashMap<String, String>());

		// get the filter
		node = getUnrequiredNode("filter", configElement);
		Filter filter = node != null ? parseFilter((Element) node, additionalPaths) : null;

		// get the sensors
		node = getUnrequiredNode("sensors", configElement);
		if (node != null) {
			nodeList = ((Element) node).getElementsByTagName("sensor");
			for (int j = 0; j < nodeList.getLength(); j++) {
				Sensor sensor = parseSensor((Element) nodeList.item(j), startScene, additionalPaths);
				startScene.getSensors().add(sensor);
			}
		}

		// check if sensors are defined
		if (startScene.getSensors().isEmpty())
			System.err.println("WARNING: Sensor list is empty.");

		// initialize SceneController
		SceneController.getInstance().reset(startScene, fuser, filter);
	}

	/**
	 * Determines additional search paths for implementable classes.
	 * 
	 * @param additionalPathElement
	 *            corresponding XML-{@link Element}
	 * @return search paths as {@link URL} array
	 * @throws XMLStreamException
	 */
	private static URL[] parseAdditionalPath(Element additionalPathElement) throws XMLStreamException {
		NodeList nodeList = additionalPathElement.getElementsByTagName("path");
		URL[] pathList = new URL[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			String path = nodeList.item(i).getFirstChild().getNodeValue();
			File file = new File(path);
			try {
				pathList[i] = file.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new XMLStreamException("Invalid path: " + path);
			}
		}
		return pathList;
	}

	/**
	 * Parses the additional joints defined by the user and adds them to the
	 * current joints. Overrides already defined joints but preserves the
	 * hierarchy.
	 * 
	 * @param configElement
	 *            corresponding XML-{@link Element}
	 * @param currentJoints
	 *            current joints
	 * @throws XMLStreamException
	 */
	private static void parseAndAddJoints(Element configElement, HashMap<JointType, Joint> currentJoints) throws XMLStreamException {
		Node node = getUnrequiredNode("joints", configElement);
		if (node != null) {
			NodeList nodeList = ((Element) node).getElementsByTagName("joint");
			for (int j = 0; j < nodeList.getLength(); j++) {
				parseJoint((Element) nodeList.item(j), currentJoints);
			}
		}
	}

	/**
	 * Parses a joint and sets its properties
	 * 
	 * @param jointElement
	 *            corresponding XML-{@link Element}
	 * @param currentJoints
	 *            current joints
	 * @return new joint
	 * @throws XMLStreamException
	 */
	private static void parseJoint(Element jointElement, HashMap<JointType, Joint> currentJoints) throws XMLStreamException {
		JointType type = parseJointType(getRequiredNode("type", jointElement).getFirstChild().getNodeValue());

		Joint parent = null;
		Node node = getUnrequiredNode("parent", jointElement);
		if (node != null) {
			JointType parentType = parseJointType(node.getFirstChild().getNodeValue());
			if (!currentJoints.containsKey(parentType))
				throw new XMLStreamException("There is no joint with the type " + parentType + " defined before the joint with the type "
						+ type);
			else
				parent = currentJoints.get(parentType);
		}

		Vector3D defaultPosition = Joint.DEFAULT_POSITION;
		node = getUnrequiredNode("defaultPosition", jointElement);
		if (node != null) {
			defaultPosition = parsePosition((Element) node);
		}

		Rotation defaultOrientation = Joint.DEFAULT_ORIENTATION;
		node = getUnrequiredNode("defaultOrientation", jointElement);
		if (node != null) {
			defaultOrientation = parseOrientation((Element) node);
		}

		Joint newJoint = new Joint(type, defaultPosition, defaultOrientation, parent);
		Joint currentJoint = currentJoints.get(type);
		if (currentJoint != null) {
			if (currentJoint != null) {
				for (SceneNode child : currentJoint.getChildren()) {
					child.setParent(newJoint);
				}
			}
			if (currentJoint.getParent() != null) {
				Joint parentJoint = currentJoint.getParent();
				parentJoint.getChildren().remove(currentJoint);
			}

		}
		currentJoints.put(newJoint.getJointType(), newJoint);
	}

	/**
	 * Parses a string to get the corresponding {@link JointType}.
	 * 
	 * @param stringValue
	 *            the type of the joint as string
	 * @return the type of the joint as {@link JointType}
	 */
	private static JointType parseJointType(String stringValue) {
		try {
			return JointType.valueOf(stringValue);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unexpected JointType: " + stringValue);
		}
	}

	/**
	 * Parses the fuser XML-{@link Element} and creates the corresponding
	 * {@link Fuser}.
	 * 
	 * @param fuserElement
	 *            corresponding XML-{@link Element}
	 * @param additionalPaths
	 *            additional search paths for the implemented {@link Fuser}
	 * @return instance of the {@link Fuser}
	 * @throws XMLStreamException
	 */
	private static Fuser parseFuser(Element fuserElement, URL[] additionalPaths) throws XMLStreamException {
		Node node = getUnrequiredNode("arguments", fuserElement);
		Map<String, String> arguments;
		if (node != null)
			arguments = parseArguments((Element) node);
		else
			arguments = new HashMap<String, String>();

		return instantiateClass(getRequiredNode("class", fuserElement).getFirstChild().getNodeValue(), additionalPaths, arguments);
	}

	/**
	 * Parses the filter XML-{@link Element} and creates the corresponding
	 * {@link Filter}.
	 * 
	 * @param filterElement
	 *            corresponding XML-{@link Element}
	 * @param additionalPaths
	 *            additional search paths for the implemented {@link Filter}
	 * @return instance of the {@link Filter}
	 * @throws XMLStreamException
	 */
	private static Filter parseFilter(Element filterElement, URL[] additionalPaths) throws XMLStreamException {
		Node node = getUnrequiredNode("arguments", filterElement);
		Map<String, String> arguments;
		if (node != null)
			arguments = parseArguments((Element) node);
		else
			arguments = new HashMap<String, String>();

		return instantiateClass(getRequiredNode("class", filterElement).getFirstChild().getNodeValue(), additionalPaths, arguments);
	}

	/**
	 * Parses the sensor XML-{@link Element} and creates the corresponding
	 * {@link Sensor}.
	 * 
	 * @param sensorElement
	 *            corresponding XML-{@link Element}
	 * @param scene
	 *            start {@link Scene}
	 * @param additionalPaths
	 *            additional search paths for the implemented {@link Sensor}
	 * @return instance of the {@link Sensor}
	 * @throws XMLStreamException
	 */
	private static Sensor parseSensor(Element sensorElement, Scene scene, URL[] additionalPaths) throws XMLStreamException {
		Node node = getUnrequiredNode("arguments", sensorElement);
		Map<String, String> arguments;
		if (node != null)
			arguments = parseArguments((Element) node);
		else
			arguments = new HashMap<String, String>();

		Sensor sensor = instantiateClass(getRequiredNode("class", sensorElement).getFirstChild().getNodeValue(), additionalPaths, arguments);

		node = getUnrequiredNode("id", sensorElement);
		if (node != null) {
			sensor.setId(node.getFirstChild().getNodeValue());
		}

		node = getUnrequiredNode("parent", sensorElement);
		if (node != null) {
			JointType parentType = parseJointType(node.getFirstChild().getNodeValue());
			if (!scene.getJoints().containsKey(parentType))
				throw new XMLStreamException("Cannot create Sensor " + sensor.getClass().getName() + ". No joint of type " + parentType
						+ " defined.");
			sensor.setParent(scene.getJoints().get(parentType));
		}

		node = getUnrequiredNode("relativePosition", sensorElement);
		if (node != null) {
			sensor.setRelativePosition(parsePosition((Element) node));
		}

		node = getUnrequiredNode("relativeOrientation", sensorElement);
		if (node != null) {
			sensor.setRelativeOrientation(parseOrientation((Element) node));
		}

		return sensor;
	}

	/**
	 * Parses the a argument XML-{@link Element} and puts the arguments into a
	 * {@link Map}.
	 * 
	 * @param argumentElement
	 *            corresponding XML-{@link Element}
	 * @return arguments in form of an {@link Map}
	 * @throws XMLStreamException
	 */
	private static Map<String, String> parseArguments(Element argumentElement) throws XMLStreamException {
		Map<String, String> result = new HashMap<String, String>();
		NodeList nodeList = argumentElement.getElementsByTagName("argument");
		for (int j = 0; j < nodeList.getLength(); j++) {
			Node name = getRequiredNode("name", (Element) nodeList.item(j));
			Node value = getRequiredNode("value", (Element) nodeList.item(j));
			result.put(name.getFirstChild().getNodeValue(), value.getFirstChild().getNodeValue());
		}
		return result;
	}

	/**
	 * Parses the a position XML-{@link Element} creates the corresponding
	 * {@link Vector3D}.
	 * 
	 * @param positionElement
	 *            corresponding XML-{@link Element}
	 * @return the parsed position as a {@link Vector3D}
	 * @throws XMLStreamException
	 */
	private static Vector3D parsePosition(Element positionElement) throws XMLStreamException {
		double x = Double.parseDouble(getRequiredNode("x", positionElement).getFirstChild().getNodeValue());
		double y = Double.parseDouble(getRequiredNode("y", positionElement).getFirstChild().getNodeValue());
		double z = Double.parseDouble(getRequiredNode("z", positionElement).getFirstChild().getNodeValue());
		return new Vector3D(x, y, z);
	}

	/**
	 * Parses the a orientation XML-{@link Element} creates the corresponding
	 * {@link Rotation}.
	 * 
	 * @param positionElement
	 *            corresponding XML-{@link Element}
	 * @return the parsed position as a {@link Rotation}
	 * @throws XMLStreamException
	 */
	private static Rotation parseOrientation(Element orientationElement) throws XMLStreamException {
		String xString = getRequiredNode("x", orientationElement).getFirstChild().getNodeValue();
		String yString = getRequiredNode("y", orientationElement).getFirstChild().getNodeValue();
		String zString = getRequiredNode("z", orientationElement).getFirstChild().getNodeValue();
		String wString = getRequiredNode("w", orientationElement).getFirstChild().getNodeValue();
		double x = Double.parseDouble(xString);
		double y = Double.parseDouble(yString);
		double z = Double.parseDouble(zString);
		double w = Double.parseDouble(wString);
		Rotation r = new Rotation(w, x, y, z, true);
		if (isNaN(r))
			throw new XMLStreamException("Quaternion (" + wString + "," + xString + "," + yString + "," + zString
					+ ") is not a valid quaternion!");
		return r;
	}

	/**
	 * Checks whether a {@link Rotation} represents valid quaternion.
	 * 
	 * @param rotation
	 *            {@link Rotation} that has to be checked
	 * @return <code>true</code> if quaternion is valid, else <code>false</code>
	 */
	private static boolean isNaN(Rotation rotation) {
		return Double.isNaN(rotation.getQ0()) || Double.isNaN(rotation.getQ1()) || Double.isNaN(rotation.getQ2())
				|| Double.isNaN(rotation.getQ3());
	}

	/**
	 * Initializes a {@link Sensor}.
	 * 
	 * @param className
	 *            class name of the sensor
	 * @param additionalPaths
	 *            additional paths to search for the class
	 * @param arguments
	 *            arguments for the sensor
	 * @return initialized {@link Sensor}
	 */
	@SuppressWarnings({ "resource", "rawtypes", "unchecked" })
	private static <T> T instantiateClass(String className, URL[] additionalPaths, Map<String, String> arguments) {

		ClassLoader cl = new URLClassLoader(additionalPaths);

		Class c;
		try {
			c = cl.loadClass(className);
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("The class " + className + " does not exist.");
		}

		Constructor<T> ctor;
		try {
			ctor = c.getConstructor(Map.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("The class " + className
					+ " has no valid contructor. There has to be a constructor with the argument Map<String,String>.");
		} catch (SecurityException e) {
			throw new RuntimeException("The constructor of the class " + className + " is not accessible.");
		}
		try {
			T instance = (T) ctor.newInstance(arguments);
			return (T) instance;
		} catch (IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("The class " + className + " could not be instantiated.");
		}
	}

	/**
	 * Returns a required {@link Node} from a XML-{@link Element} and handles
	 * the error cases.
	 * 
	 * @param tag
	 *            name of the {@link Node}
	 * @param element
	 *            corresponding XML-{@link Element}
	 * @return the {@link Node}
	 * @throws XMLStreamException
	 */
	private static Node getRequiredNode(String tag, Element element) throws XMLStreamException {
		NodeList nodeList = element.getElementsByTagName(tag);
		if (nodeList.getLength() != 1)
			throw new XMLStreamException("There should be exactly one " + tag + " tag.");
		return nodeList.item(0);
	}

	/**
	 * Returns a unrequired {@link Node} from a XML-{@link Element} and handles
	 * the error cases.
	 * 
	 * @param tag
	 *            name of the {@link Node}
	 * @param element
	 *            corresponding XML-{@link Element}
	 * @return the {@link Node}
	 * @throws XMLStreamException
	 */
	private static Node getUnrequiredNode(String tag, Element element) throws XMLStreamException {
		NodeList nodeList = element.getElementsByTagName(tag);
		if (nodeList.getLength() > 1)
			throw new XMLStreamException("There should be at most one " + tag + " tag.");
		return nodeList.item(0);
	}
}
