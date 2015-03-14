package com.corpus.web.json;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Contains statics for the JSON conversion process.
 * 
 * @author Matthias Weise
 * 
 */
public class JsonStatics {

	public final static DecimalFormat DF;

	static {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		DF = new DecimalFormat("#.####", otherSymbols);
	}

	/**
	 * Rounds a double down to float with 4 after comma values.
	 * 
	 * @param d
	 *            double
	 * @return float
	 */
	public static float roundDown4(double d) {
		return (int) (d * 1e4) / 1e4f;
	}

}
