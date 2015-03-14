package com.corpus.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Timer;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.corpus.configuration.ConfigurationParser;
import com.corpus.configuration.NativeLibrary;
import com.corpus.web.websocket.WebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * <p>
 * This is the main controller of the framework. It starts the thread updating
 * the scene and manages the incoming requests.
 * </p>
 * <p>
 * Before the server is initialized a configuration file is parsed. Thus the
 * framework will be configured.
 * </p>
 * 
 * @author Matthias Weise
 * 
 */
@Path("corpus")
public class Controller {

	// Base URI the Grizzly HTTP server will listen on
	public static String BASE_URI = "localhost:8080/myapp/";

	// Update frequency of the model
	public static int UPDATE_FREQUENCY = 30;

	// Time until a joint is reset to its default position if no new data is
	// received by a sensor
	public static int RETURN_TO_DEFAULT = 2000;

	// Path to the configuration file
	private static final String CONFIG_PATH = "config.xml";

	private static HttpServer server;
	private static Timer sceneControllerTimer;

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		// load native libraries
		if (!NativeLibrary.loadSystem("native"))
			System.err.println("Warning: Native libraries could not successful loaded.");

		// parse config file and initialize SceneController
		try {
			ConfigurationParser.readConfigAndInitSceneController(args.length > 0 ? args[0] : CONFIG_PATH);
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("Shutting down server!");
			shutDownGracefully(1);
		}

		// start the update process of the SceneController
		sceneControllerTimer = new Timer();
		sceneControllerTimer.schedule(SceneController.getInstance(), 0l, (long) (1000 / UPDATE_FREQUENCY));

		// start the server
		try {
			startServer();
			System.out.println(String.format("Server started with WADL available at %sapplication.wadl", BASE_URI));
			System.out.println("Hit enter to stop it...");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Shutting down server!");
			shutDownGracefully(1);
		}

		// wait for input to close the server
		System.in.read();

		// shut down server
		shutDownGracefully(0);
	}

	/**
	 * Shuts the server down gracefully, by closing the SceneController which
	 * stops the sensors and shuts them down.
	 * 
	 * @param returnCode
	 *            return code returned by the application after closing
	 * @throws InterruptedException
	 */
	private static void shutDownGracefully(int returnCode) throws InterruptedException {
		if (sceneControllerTimer != null)
			sceneControllerTimer.cancel();
		SceneController.getInstance().terminate();
		if (server != null)
			server.shutdown();
		System.out.println("Server shut down.");
		System.exit(returnCode);
	}

	/**
	 * Starts Grizzly HTTP server exposing the model as JAX-RS resources.
	 * 
	 * @return Grizzly HTTP server.
	 * @throws IOException
	 */
	private static HttpServer startServer() throws IOException {
		// create a resource config that scans for JAX-RS resources and
		// providers in the controller package
		final ResourceConfig rc = new ResourceConfig().packages("com.corpus.controller");

		// create a new instance of grizzly http server exposing the application
		// at BASE_URI
		server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);

		// add WebSocketAddOn to allow communication via a WebSocket
		WebSocketAddOn addOn = new WebSocketAddOn();
		addOn.setTimeoutInSeconds(60);
		for (NetworkListener listener : server.getListeners()) {
			listener.registerAddOn(addOn);
		}
		WebSocketEngine.getEngine().register("/corpus", "/websocket", new WebSocketHandler());

		// start the server
		server.start();

		return server;
	}

	@GET
	@Path("fullHierarchicalModel")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getFullHierarchicalModel() throws JsonProcessingException {
		return SceneController.getInstance().getFullHierarchicalModelJSON();
	}

	@GET
	@Path("fullListModel")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getFullListModel() throws JsonProcessingException {
		return SceneController.getInstance().getFullListModelJSON();
	}

	@GET
	@Path("customModel")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getCustomModel(@DefaultValue("list") @QueryParam("type") String type,
			@DefaultValue("absolutePosition_absoluteOrientation") @QueryParam("field") List<String> fields) throws JsonProcessingException {
		String result = SceneController.getInstance().getCustomModelJSON(type, fields);
		return result;
	}

	@GET
	@Path("singleJoint")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getSingleJoints(@DefaultValue("SPINE_BASE") @QueryParam("jointType") List<String> jointTypes,
			@DefaultValue("absolutePosition_absoluteOrientation") @QueryParam("field") List<String> fields) throws JsonProcessingException {
		return SceneController.getInstance().getSingleJointsJSON(jointTypes, fields);
	}

	@GET
	@Path("sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getSensors() throws JsonProcessingException {
		return SceneController.getInstance().getSensorsJSON();
	}

	@GET
	@Path("elapsedScene")
	@Produces(MediaType.APPLICATION_JSON)
	public static String getElapsedSceneJSON(@DefaultValue("1") @QueryParam("index") int index) throws JsonProcessingException {
		return SceneController.getInstance().getElapsedSceneJSON(index);
	}

	@POST
	@Path("callSensorMethod")
	@Produces(MediaType.APPLICATION_JSON)
	public static String callSensorMethod(@QueryParam("sensorId") String sensorId, @QueryParam("methodName") String methodName,
			@QueryParam("param") List<String> params) throws JsonProcessingException {
		return SceneController.getInstance().callSensorMethod(sensorId, methodName, params);
	}
}
