package com.corpus.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import com.corpus.filter.Filter;
import com.corpus.fuser.Fuser;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.sensor.Sensor;
import com.corpus.sensor.SensorInitializationException;
import com.corpus.web.json.JsonCreator;
import com.corpus.web.websocket.WebSocketHandler;

/**
 * <p>
 * This class manages and updates the current {@link Scene}. It also manages the
 * history of {@link Scene}s and generates the JSON representation of the body
 * model. The class is designed as a singleton. The instance if this class is
 * accessible through the {@link #getInstance() getInstance} method.
 * </p>
 * <p>
 * The update process is run though multiple times per second. During the update
 * process the {@link Fuser} and {@link Filter} is called, which are defined in
 * the configuration file.
 * </p>
 * 
 * @author Matthias Weise
 * 
 */
public class SceneController extends TimerTask {

	// instance of this class
	private static SceneController instance;

	// current Scene
	private Scene currentScene;

	// size of the frame history
	public static int FRAME_HISTORY = 60;

	// fields to manage sensors
	private List<Thread> sensorThreads;
	private List<Sensor> sensors;

	// Scene history
	private List<Scene> sceneHistory;
	private Fuser fuser;
	private Filter filter;

	// lock objects
	private final Object currentSceneLock = new Object();
	private final Object sceneHistoryLock = new Object();

	// WebSocketHandler
	public WebSocketHandler webSocketHandler;

	/**
	 * Private Constructor to realize singleton pattern.
	 */
	private SceneController() {
		sensorThreads = new ArrayList<Thread>();
		sceneHistory = new ArrayList<Scene>();
	}

	/**
	 * Returns the instance of the class.
	 * 
	 * @return {@link SceneController} instance
	 */
	public static SceneController getInstance() {
		if (instance == null)
			instance = new SceneController();
		return instance;
	}

	/**
	 * Resets the {@link SceneController} with the given Start{@link Scene},
	 * {@link Fuser} and {@link Filter}. Also starts the {@link Sensor}s.
	 * 
	 * @param startScene
	 *            initial {@link Scene} containing the body model and the
	 *            {@link Sensor} positions
	 * @param fuser
	 *            {@link Fuser} used to fuse the data of the {@link Sensor}s
	 * @param filter
	 *            {@link Filter} used to filter the data
	 * @throws InterruptedException
	 */
	public void reset(Scene startScene, Fuser fuser, Filter filter) throws InterruptedException {
		synchronized (currentSceneLock) {
			if (currentScene != null) {
				terminate();
			}
			this.currentScene = startScene;
		}
		this.fuser = fuser;
		this.filter = filter;
		startSensors();
	}

	/**
	 * Starts the {@link Sensor}s. If a {@link Sensor} throws a exception during
	 * the initialization process, the {@link Sensor} will be ignored.
	 */
	private void startSensors() {
		List<Sensor> ignoredSensors = new ArrayList<Sensor>();
		for (Sensor s : currentScene.getSensors()) {
			try {
				s.init();
			} catch (SensorInitializationException e) {
				System.err.println("Sensor " + s.getClass().getName() + " could not be initialized due to: " + e.getMessage());
				System.err.println("WARNING: Sensor " + s.getClass().getName() + " will be ignored!");
				ignoredSensors.add(s);
				continue;
			}
			s.setInitialized(true);
			Thread t = new Thread(s);
			t.start();
			sensorThreads.add(t);
		}
		currentScene.getSensors().removeAll(ignoredSensors);
		currentScene.resetRootNodes();
		this.sensors = currentScene.getSensors();
	}

	@Override
	public void run() {
		// copy current scene
		Scene workingScene = currentScene;
		synchronized (currentSceneLock) {
			currentScene = workingScene.clone();
		}

		// fuse data
		fuser.fuseData(workingScene);

		// filter data
		if (filter != null)
			filter.filterData(sceneHistory, workingScene);
		workingScene.setTimestamp(Calendar.getInstance());

		// add old scene to scene history
		if (FRAME_HISTORY > 0) {
			synchronized (sceneHistoryLock) {
				if (sceneHistory.size() >= FRAME_HISTORY)
					sceneHistory.remove(0);
				sceneHistory.add(currentScene);
			}
		}

		// set current scene
		currentScene = workingScene;

		// send new model to the WebSocket-Clients
		if (webSocketHandler != null) {
			webSocketHandler.send(getFullListModelJSON());
		}
	}

	/**
	 * Terminates the {@link SceneController}. Therefore the {@link Sensor}s
	 * will be terminated and the {@link Sensor} threads will be joined.
	 * 
	 * @throws InterruptedException
	 */
	public void terminate() throws InterruptedException {
		if (currentScene != null) {
			for (Sensor s : currentScene.getSensors()) {
				if (s.isInitialized())
					s.terminate();
			}
		}
		for (Thread t : sensorThreads) {
			t.join();
		}
	}

	/**
	 * Creates a copy of the current {@link Scene}.
	 * 
	 * @return current {@link Scene}
	 */
	public Scene getCopyOfScene() {
		return currentScene.clone();
	}

	/**
	 * Creates a hierarchical JSON representation of the full body model.
	 * 
	 * @return JSON string
	 */
	public String getFullHierarchicalModelJSON() {
		return JsonCreator.getFullHierarchicalModelJSON(currentScene);
	}

	/**
	 * Creates a JSON representation of the full body model in form of a list.
	 * 
	 * @return JSON string
	 */
	public String getFullListModelJSON() {
		return JsonCreator.getFullListModelJSON(currentScene);
	}

	/**
	 * Creates a custom JSON representation of the body model in dependence of
	 * the given <code>type</code> and <code>fields</code>.
	 * 
	 * @param type
	 *            Determines the type (<code>hierarchical</code> or
	 *            <code>list</code>).
	 * @param fields
	 *            Determines which fields of the {@link Joint}-class are
	 *            serialized.
	 * @return JSON string
	 */
	public String getCustomModelJSON(String type, List<String> fields) {
		return JsonCreator.getCustomModelJSON(currentScene, type, fields);
	}

	/**
	 * Creates a JSON representation of a the specified {@link Joint}s.
	 * 
	 * @param jointTypes
	 *            {@link JointType}s of the {@link Joint}s
	 * @param fields
	 *            Determines which fields of the {@link Joint}-class are
	 *            serialized.
	 * @return JSON string
	 */
	public String getSingleJointsJSON(List<String> jointTypes, List<String> fields) {
		return JsonCreator.getJointsJSON(currentScene, jointTypes, fields);
	}

	/**
	 * Creates a JSON representation of the list of {@link Sensor}s.
	 * 
	 * @return JSON string
	 */
	public String getSensorsJSON() {
		return JsonCreator.getSensorsJSON(currentScene);
	}

	/**
	 * Calls a specific {@link Sensor} method.
	 * 
	 * @param sensorId
	 *            {@link com.corpus.sensor.Sensor#id ID} of the {@link Sensor}.
	 * @param methodName
	 *            The name of the method the user wants to call.
	 * @param params
	 *            Arguments that will be given to the method.
	 * @return Result of the method as a String.
	 */
	public String callSensorMethod(String sensorId, String methodName, List<String> params) {
		Sensor sensor = null;
		for (Sensor s : sensors) {
			if (s.getId().equals(sensorId))
				sensor = s;
		}
		if (sensor == null)
			return "ERROR: There is no sensor with the given id!";

		Class<?>[] paramClasses = new Class<?>[params.size()];
		for (int i = 0; i < params.size(); i++)
			paramClasses[i] = String.class;

		Method method = null;
		try {
			method = sensor.getClass().getDeclaredMethod(methodName, paramClasses);
		} catch (NoSuchMethodException e) {
			return "ERROR: A methood with the given name and arguments does not exist!";
		} catch (SecurityException e) {
			return "ERROR: The method is not accessible!";
		}

		Object result = null;
		try {
			result = method.invoke(sensor, params.toArray());
		} catch (IllegalAccessException e) {
			return "ERROR: The method is not accessible!";
		} catch (IllegalArgumentException e) {
			// Should never happen
			return "ERROR: Error calling the given method!\n" + e.getMessage();
		} catch (InvocationTargetException e) {
			return "ERROR: The called method throwed an exception!\n" + e.getMessage();
		}

		if (!(result instanceof String))
			return "ERROR: Method called but result is not a String!";
		return (String) result;
	}

	/**
	 * Creates a JSON representation of a {@link Scene} contained in the scene
	 * history.
	 * 
	 * @param index
	 *            Index of the {@link Scene} in the scene history (number
	 *            between 1 and {@link #FRAME_HISTORY})
	 * @return JSON string
	 */
	public String getElapsedSceneJSON(int index) {
		if (index <= 0)
			return "ERROR: Index must be greater than 0";
		synchronized (sceneHistoryLock) {
			if (sceneHistory.size() - index < 0)
				return "ERROR: Scene history is not big enough";
			return JsonCreator.getElapsedSceneJSON(sceneHistory.get(sceneHistory.size() - index));
		}
	}
}
