package com.corpus.web.websocket;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import com.corpus.controller.SceneController;

/**
 * Handles the WebSocket connections.
 * 
 * @author Matthias Weise
 * 
 */
public class WebSocketHandler extends WebSocketApplication {

	@Override
	public void onMessage(WebSocket socket, String text) {
	}

	@Override
	public void onConnect(WebSocket socket) {
		SceneController.getInstance().webSocketHandler = this;
		add(socket);
	}

	@Override
	public boolean onError(WebSocket webSocket, Throwable t) {
		System.err.println("Error during websocket connection: " + t.getMessage());
		return true;
	}

	/**
	 * Sends the given <code>data</code> the clients.
	 * 
	 * @param data
	 *            String which has to be send.
	 */
	public void send(String data) {
		for (WebSocket webSocket : getWebSockets()) {
			webSocket.send(data);
		}
	}
}
