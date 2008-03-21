/*
 * Anarres C Preprocessor
 * Copyright (C) 2007 Shevek
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.anarres.cpp;

import java.io.File;

public class PreprocessorListener {

	private int	errors;
	private int	warnings;

	public PreprocessorListener() {
		clear();
	}

	public void clear() {
		errors = 0;
		warnings = 0;
	}

	public int getErrors() {
		return errors;
	}

	public int getWarnings() {
		return warnings;
	}

	protected void print(String msg) {
		System.err.println(msg);
	}

	/**
	 * Handles a warning.
	 *
	 * The behaviour of this method is defined by the
	 * implementation. It may simply record the error message, or
	 * it may throw an exception.
	 */
	public void handleWarning(Source source, int line, int column,
					String msg)
						throws LexerException {
		warnings++;
		print(source.getName() + ":" + line + ":" + column +
				": warning: " + msg); 
	}

	/**
	 * Handles an error.
	 *
	 * The behaviour of this method is defined by the
	 * implementation. It may simply record the error message, or
	 * it may throw an exception.
	 */
	public void handleError(Source source, int line, int column,
					String msg)
						throws LexerException {
		errors++;
		print(source.getName() + ":" + line + ":" + column +
				": error: " + msg); 
	}

	public void handleSourceChange(Source source, String event) {
	}

}
