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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.List;
import java.util.Iterator;

import static org.anarres.cpp.Token.*;

/**
 * A Source for lexing a String.
 *
 * This class is used by token pasting, but can be used by user
 * code.
 */
public class StringLexerSource extends LexerSource {

	/**
	 * Creates a new Source for lexing the given String.
	 *
	 * @param ppvalid true if preprocessor directives are to be
	 *	honoured within the string.
	 */
	public StringLexerSource(String string, boolean ppvalid)
						throws IOException {
		super(new StringReader(string), ppvalid);
	}

	/**
	 * Creates a new Source for lexing the given String.
	 *
	 * By default, preprocessor directives are not honoured within
	 * the string.
	 */
	public StringLexerSource(String string)
						throws IOException {
		this(string, false);
	}

	public String toString() {
		return "string literal";
	}
}
