package com.corpus.sensor.myo;

import java.util.Map;

import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.sensor.Sensor;
import com.corpus.sensor.SensorInitializationException;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;

/**
 * <p>
 * Wrapper for the <i>Myo</i>. To communicate with the Myo the <i>myo-java</i>
 * library is used.
 * </p>
 * 
 * <p>
 * The Myo can theoretically by worn on every body part. Therefore the joint for
 * which the Myo delivers data is identified through the
 * {@link Sensor#getParent() parent}.
 * </p>
 * 
 * <p>
 * The wrapper uses a event-based system. The {@link MyoListener} therefore
 * updates the current collected rotation whenever new data is available.
 * </p>
 * 
 * @see <a href="https://www.thalmic.com/en/myo">Myo</a>
 * @see <a href="https://github.com/NicholasAStuart/myo-java">myo-java</a>
 * 
 * @author Matthias Weise
 * 
 */
public class MyoSensor extends Sensor {

	Hub hub;
	MyoListener myoListener;
	volatile boolean running;

	public MyoSensor(Map<String, String> arguments) {
		super(arguments);
		collectsAbsoluteData = true;
	}

	@Override
	public void run() {
		while (running) {
			hub.run(1000 / 20);
		}
	}

	@Override
	public void init() {
		// initlialize myo hub
		hub = new Hub("com.corpus.sensor.myo.MyoSensor");

		// try to find myo
		Myo myo = hub.waitForMyo(2000);
		if (myo == null) {
			throw new SensorInitializationException("Unable to find a Myo!");
		}

		// determine parent
		JointType parentType;
		if (this.parent != null && this.parent instanceof Joint)
			parentType = ((Joint) this.parent).getJointType();
		else {
			parentType = JointType.WRIST_RIGHT;
			System.err
					.println("WARNING: No parent was set for the Myo. It will be assumed that the collected data belongs to the standard joint: "
							+ parentType);
		}
		// add myo listener
		boolean usbOrientationToEllbow = Boolean.parseBoolean(arguments.get("usbOrientationToEllbow"));
		myoListener = new MyoListener(parentType, usbOrientationToEllbow, orientationConfidence);
		hub.addListener(myoListener);

		running = true;
	}

	@Override
	public void terminate() {
		running = false;
	}

	@Override
	protected void updateCurrentData() {
		setCurrentData(myoListener.getCurrentData());
	}
}
