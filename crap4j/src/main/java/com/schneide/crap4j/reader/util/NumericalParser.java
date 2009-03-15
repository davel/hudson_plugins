package com.schneide.crap4j.reader.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class NumericalParser {

	private final DecimalFormatSymbols formatSymbols;
	
	public NumericalParser() {
		this(Locale.getDefault());
	}
	
	public NumericalParser(Locale locale) {
		super();
		this.formatSymbols = new DecimalFormatSymbols(locale);
		//this.formatSymbols = DecimalFormatSymbols.getInstance(locale);
	}
	
	public double parseDouble(String text) {
		DecimalFormat format = new DecimalFormat("0.00", this.formatSymbols); //$NON-NLS-1$
		try {
			return format.parse(text).doubleValue();
		} catch (ParseException e) {
			throw new NumberFormatException(e.getMessage() + " for " + text); //$NON-NLS-1$
		}
	}
	
	public int parseInt(String text) {
		return Integer.parseInt(text);
	}
}
