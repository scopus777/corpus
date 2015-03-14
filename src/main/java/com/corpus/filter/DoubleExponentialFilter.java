package com.corpus.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.helper.ParsingHelper;
import com.corpus.scene.Joint;
import com.corpus.scene.JointType;
import com.corpus.scene.Scene;
import com.corpus.scene.SceneNode;

/**
 * An Double Exponential Filter. Smoothes the data with the help of the old data
 * and the calculated trend.
 * 
 * @see <a
 *      href="https://msdn.microsoft.com/en-us/library/jj131429.aspx">https://msdn.microsoft.com/en-us/library/jj131429.aspx</a>
 * @author Matthias Weise
 * 
 */
public class DoubleExponentialFilter extends Filter {

	// alpha
	private float alpha = 0.75f;

	// gamma
	private float gamma = 0.75f;

	// history
	private Map<JointType, FilterOutput> positionFilterHistory;
	private Map<JointType, FilterOutput> orientationFilterHistory;

	/**
	 * Constructor which parses the given arguments to determine alpha and
	 * gamma.
	 * 
	 * @param arguments
	 */
	public DoubleExponentialFilter(Map<String, String> arguments) {
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
			} else {
				positionFilterHistory.put(joint.getJointType(), new FilterOutput(new double[] { pos.getX(), pos.getY(), pos.getZ() }));
			}
		} else {
			positionFilterHistory.remove(joint.getJointType());
		}
		if (joint.getOrientationTracked()) {
			Rotation ori = joint.getAbsoluteOrientation();
			if (orientationFilterHistory.containsKey(joint.getJointType())) {
				FilterOutput output = orientationFilterHistory.get(joint.getJointType());
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
					joint.setAbsoluteOrientation(new Rotation(newOutput[0], newOutput[1], newOutput[2], newOutput[3], true));
				} else {
					output.Trend = new double[] { calculateFirstTrend(ori.getQ0(), output.Output[0]),
							calculateFirstTrend(ori.getQ1(), output.Output[1]), calculateFirstTrend(ori.getQ2(), output.Output[2]),
							calculateFirstTrend(ori.getQ3(), output.Output[3]) };
				}
			} else {
				orientationFilterHistory.put(joint.getJointType(), new FilterOutput(new double[] { ori.getQ0(), ori.getQ1(), ori.getQ2(),
						ori.getQ3() }));
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
	 * Represents the Output of the Filter of the last iteration.
	 * 
	 * @author Matthias Weise
	 * 
	 */
	class FilterOutput {
		public double[] Trend;
		public double[] Output;

		public FilterOutput(double[] output) {
			this.Output = output;
		}
	}

	/**
	 * Parses the arguments.
	 */
	private void parseArguments() {
		for (Entry<String, String> entry : arguments.entrySet()) {
			Float value = ParsingHelper.parseFloat(entry.getKey(), entry.getValue());
			if (value != null) {
				if (entry.getKey().equals("alpha"))
					alpha = value;
				else if (entry.getKey().equals("gamma"))
					gamma = value;
				else
					System.err.println("WARNING: Unknown argument " + entry.getKey()
							+ " for DoubleExponentialFilter in the config file! The argument will be ignored!");
			}
		}
	}

}
