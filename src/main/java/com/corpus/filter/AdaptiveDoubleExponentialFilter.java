package com.corpus.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.controller.Controller;
import com.corpus.helper.ParsingHelper;
import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.scene.SceneNode;

/**
 * An Adaptive Double Exponential Filter. Smoothes the data with the help of the
 * old data and the calculated trend. Additional it uses the velocities of the
 * position and orientation of a joint to adopt the process.
 * 
 * @see <a
 *      href="https://msdn.microsoft.com/en-us/library/jj131429.aspx">https://msdn.microsoft.com/en-us/library/jj131429.aspx</a>
 * @author Matthias Weise
 * 
 */
public class AdaptiveDoubleExponentialFilter extends Filter {

	// bounds of the position velocity (centimeter per seconds)
	private float velocityLowPosition = 5;
	private float velocityHighPosition = 50;

	// bounds of the orientation velocity (radians per seconds)
	private float velocityLowOrientation = 200;
	private float velocityHighOrientation = 600;

	// bounds of alpha
	private float alphaLow = 0.5f;
	private float alphaHigh = 0.9f;

	// bounds of gamma
	private float gammaLow = 0.5f;
	private float gammaHigh = 0.9f;

	// alpha
	private float alpha = 0.75f;

	// gamma
	private float gamma = 0.75f;

	// history
	private Map<JointType, FilterOutput> positionFilterHistory;
	private Map<JointType, FilterOutput> orientationFilterHistory;

	/**
	 * Constructor which parses the given arguments to determine alpha and gamma
	 * and the bounds.
	 * 
	 * @param arguments
	 */
	public AdaptiveDoubleExponentialFilter(Map<String, String> arguments) {
		super(arguments);
		positionFilterHistory = new HashMap<JointType, FilterOutput>();
		orientationFilterHistory = new HashMap<JointType, FilterOutput>();
		parseArguments();
	}

	@Override
	public void filterData(List<Scene> sceneHistory, Scene currentScene) {
		for (Joint joint : currentScene.getRootJoints()) {
			applyFilter(joint);
		}

	}

	/**
	 * Applies the filter to a single {@link Joint}.
	 * 
	 * @param joint
	 */
	private void applyFilter(Joint joint) {
		if (joint.getPositionTracked()) {
			Vector3D pos = joint.getAbsolutePosition();
			if (positionFilterHistory.containsKey(joint.getJointType())) {
				FilterOutput output = positionFilterHistory.get(joint.getJointType());
				setParameterPosition((float) pos.distance(new Vector3D(output.Input)) * Controller.UPDATE_FREQUENCY);
				if (output.Trend != null) {
					double[] newOutput = new double[] { calculateFilter(pos.getX(), output.Output[0], output.Trend[0]),
							calculateFilter(pos.getY(), output.Output[1], output.Trend[1]),
							calculateFilter(pos.getZ(), output.Output[2], output.Trend[2]) };
					output.Trend = new double[] { calculateTrend(newOutput[0], output.Output[0], output.Trend[0]),
							calculateTrend(newOutput[1], output.Output[1], output.Trend[1]),
							calculateTrend(newOutput[2], output.Output[2], output.Trend[2]) };
					output.Output = newOutput;
					joint.setAbsolutePosition(new Vector3D(newOutput[0], newOutput[1], newOutput[2]));
				} else {
					output.Trend = new double[] { calculateFirstTrend(pos.getX(), output.Output[0]),
							calculateFirstTrend(pos.getY(), output.Output[1]), calculateFirstTrend(pos.getZ(), output.Output[2]) };
				}
				output.Input = pos.toArray();
			} else {
				positionFilterHistory.put(joint.getJointType(), new FilterOutput(pos.toArray(), pos.toArray(), 3));
			}
		} else {
			positionFilterHistory.remove(joint.getJointType());
		}

		if (joint.getOrientationTracked()) {
			Rotation ori = joint.getAbsoluteOrientation();
			if (orientationFilterHistory.containsKey(joint.getJointType())) {
				FilterOutput output = orientationFilterHistory.get(joint.getJointType());
				setParameterOrientation((float) (Rotation.distance(new Rotation(output.Input[0], output.Input[1], output.Input[2],
						output.Input[3], false), ori)
						* (180d / Math.PI) * Controller.UPDATE_FREQUENCY));
				if (output.Trend != null) {
					double[] newOutput = new double[] { calculateFilter(ori.getQ0(), output.Output[0], output.Trend[0]),
							calculateFilter(ori.getQ1(), output.Output[1], output.Trend[1]),
							calculateFilter(ori.getQ2(), output.Output[2], output.Trend[2]),
							calculateFilter(ori.getQ3(), output.Output[3], output.Trend[3]) };
					output.Trend = new double[] { calculateTrend(newOutput[0], output.Output[0], output.Trend[0]),
							calculateTrend(newOutput[1], output.Output[1], output.Trend[1]),
							calculateTrend(newOutput[2], output.Output[2], output.Trend[2]),
							calculateTrend(newOutput[3], output.Output[3], output.Trend[3]) };
					output.Output = newOutput;
					// output.AddToLastOutputs();
					joint.setAbsoluteOrientation(new Rotation(newOutput[0], newOutput[1], newOutput[2], newOutput[3], true));
				} else {
					output.Trend = new double[] { calculateFirstTrend(ori.getQ0(), output.Output[0]),
							calculateFirstTrend(ori.getQ1(), output.Output[1]), calculateFirstTrend(ori.getQ2(), output.Output[2]),
							calculateFirstTrend(ori.getQ3(), output.Output[3]) };
				}
				output.Input = new double[] { ori.getQ0(), ori.getQ1(), ori.getQ2(), ori.getQ3() };
			} else {
				orientationFilterHistory.put(joint.getJointType(), new FilterOutput(new double[] { ori.getQ0(), ori.getQ1(), ori.getQ2(),
						ori.getQ3() }, new double[] { ori.getQ0(), ori.getQ1(), ori.getQ2(), ori.getQ3() }, 4));
			}
		} else {
			orientationFilterHistory.remove(joint.getJointType());
		}

		for (SceneNode node : joint.getChildren()) {
			if (node instanceof Joint)
				applyFilter((Joint) node);
		}

	}

	/**
	 * Calculates the filter output
	 * 
	 * @param currentFilterInput
	 * @param lastFilterOutput
	 * @param lastTrend
	 * @return Filter output
	 */
	private double calculateFilter(double currentFilterInput, double lastFilterOutput, double lastTrend) {
		return alpha * currentFilterInput + (1 - alpha) * (lastFilterOutput + lastTrend);
	}

	/**
	 * Calculates the trend.
	 * 
	 * @param currentFilterOutput
	 * @param lastFilterOutput
	 * @param lastTrend
	 * @return Trend
	 */
	private double calculateTrend(double currentFilterOutput, double lastFilterOutput, double lastTrend) {
		return gamma * (currentFilterOutput - lastFilterOutput) + (1 - gamma) * lastTrend;
	}

	/**
	 * Calculates the trend of the first iteration.
	 * 
	 * @param currentFilterOutput
	 * @param lastFilterOutput
	 * @return First trend
	 */
	private double calculateFirstTrend(double currentFilterOutput, double lastFilterOutput) {
		return currentFilterOutput - lastFilterOutput;
	}

	/**
	 * Adopts alpha and gamma in dependence of the velocity of the position.
	 * 
	 * @param velocity
	 */
	private void setParameterPosition(float velocity) {
		if (velocity < velocityLowPosition) {
			alpha = alphaLow;
			gamma = gammaLow;
		} else if (velocity >= velocityLowPosition && velocity <= velocityHighPosition) {
			alpha = alphaHigh + ((velocity - velocityHighPosition) / (velocityLowPosition - velocityHighPosition)) * (alphaLow - alphaHigh);
			gamma = gammaHigh + ((velocity - velocityHighPosition) / (velocityLowPosition - velocityHighPosition)) * (gammaLow - gammaHigh);
		} else {
			alpha = alphaHigh;
			gamma = gammaHigh;
		}
	}

	/**
	 * Adopts alpha and gamma in dependence of the velocity of the orientation.
	 * 
	 * @param velocity
	 */
	private void setParameterOrientation(float velocity) {
		if (velocity < velocityLowOrientation) {
			alpha = alphaLow;
			gamma = gammaLow;
		} else if (velocity >= velocityLowOrientation && velocity <= velocityHighOrientation) {
			alpha = alphaHigh + ((velocity - velocityHighOrientation) / (velocityLowPosition - velocityHighOrientation))
					* (alphaLow - alphaHigh);
			gamma = gammaHigh + ((velocity - velocityHighOrientation) / (velocityLowPosition - velocityHighOrientation))
					* (gammaLow - gammaHigh);
		} else {
			alpha = alphaHigh;
			gamma = gammaHigh;
		}
	}

	/**
	 * Represents the Output of the Filter of the last iteration.
	 * 
	 * @author Matthias Weise
	 * 
	 */
	class FilterOutput {
		public double[] Trend;
		public double[] Output;
		public double[] Input;

		public FilterOutput(double[] output, double[] input, int historySize) {
			this.Output = output;
			this.Input = input;
		}
	}

	/**
	 * Parses the arguments.
	 */
	private void parseArguments() {
		for (Entry<String, String> entry : arguments.entrySet()) {
			Float value = ParsingHelper.parseFloat(entry.getKey(), entry.getValue());
			if (value != null) {
				if (entry.getKey().equals("initialAlpha"))
					alpha = value;
				else if (entry.getKey().equals("initialGamma"))
					gamma = value;
				else if (entry.getKey().equals("velocityLowPosition"))
					velocityLowPosition = value;
				else if (entry.getKey().equals("velocityHighPosition"))
					velocityHighPosition = value;
				else if (entry.getKey().equals("velocityLowOrientation"))
					velocityLowOrientation = value;
				else if (entry.getKey().equals("velocityHighOrientation"))
					velocityHighOrientation = value;
				else if (entry.getKey().equals("alphaLow"))
					alphaLow = value;
				else if (entry.getKey().equals("alphaHigh"))
					alphaHigh = value;
				else if (entry.getKey().equals("gammaLow"))
					gammaLow = value;
				else if (entry.getKey().equals("gammaHigh"))
					gammaHigh = value;
				else
					System.err.println("WARNING: Unknown argument " + entry.getKey()
							+ " for AdaptiveDoubleExponentialFilter in the config file! The argument will be ignored!");

			}
		}
	}

}
