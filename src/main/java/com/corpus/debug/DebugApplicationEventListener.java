package com.corpus.debug;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * Can be used to debug the incoming connections of the server.
 * 
 * @author Matthias Weise
 * 
 */
public class DebugApplicationEventListener implements ApplicationEventListener {
	private volatile int requestCnt = 0;

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onEvent(ApplicationEvent event) {
		switch (event.getType()) {
		case INITIALIZATION_FINISHED:
			System.out.println("Application " + event.getResourceConfig().getApplicationName() + " was initialized.");
			break;
		case DESTROY_FINISHED:
			System.out.println("Application " + event.getResourceConfig().getApplicationName() + " destroyed.");
			break;
		}
	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {
		requestCnt++;
		System.out.println("Request " + requestCnt + " started.");
		// return the listener instance that will handle this request.
		return new DebugRequestEventListener(requestCnt);
	}
}
