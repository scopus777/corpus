package com.corpus.helper;

/**
 * Provides function to simplify the parsing process.
 * 
 * @author Matthias Weise
 * 
 */
public class ParsingHelper {

	/**
	 * Pares a float value and prints a error message if the value is not vaild.
	 * 
	 * @param argumentName
	 * @param argumentValue
	 * @return null if value was not valid, else Float value
	 */
	public static Float parseFloat(String argumentName, String argumentValue) {
		Float result = null;
		try {
			result = Float.valueOf(argumentValue);
		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("WARNING: The in the config file defined argument " + argumentName
					+ " has no valid value! It will be ignored!");
		}
		return result;
	}

}
