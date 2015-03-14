package com.corpus.filter;

import java.util.List;
import java.util.Map;

import com.corpus.scene.Scene;

/**
 * This is the base class for the filters smoothing the fused data.
 * 
 * @author Matthias Weise
 * 
 */
public abstract class Filter {

	protected Map<String, String> arguments;

	/**
	 * C'tor of the Filter. It is possible to define arguments for the filter in
	 * the configuration file. The C'tor is called with these arguments as a
	 * map.
	 * 
	 * @param arguments
	 */
	public Filter(Map<String, String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Filters the data. This function should directly set the position and/or
	 * orientation in the current scene.
	 * 
	 * @param sceneHistory
	 *            history containing the last scenes
	 * @param currentScene
	 *            current scene
	 */
	public abstract void filterData(List<Scene> sceneHistory, Scene currentScene);
}
