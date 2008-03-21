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
import java.io.IOException;
import java.io.Reader;

import static org.anarres.cpp.Token.*;

/**
 * A Reader wrapper around the Preprocessor.
 *
 * This is a utility class to provide a transparent {@link Reader}
 * which preprocesses the input text.
 *
 * @see Preprocessor
 * @see Reader
 */
public class CppReader extends Reader {

	private Preprocessor	cpp;
	private String			token;
	private int				idx;

	public CppReader(final Reader r) {
		cpp = new Preprocessor(new LexerSource(r, true) {
			@Override
			public String getName() {
				return "<CppReader Input@" +
						System.identityHashCode(r) + ">";
			}
		});
		token = "";
		idx = 0;
	}

	public CppReader(Preprocessor p) {
		cpp = p;
		token = "";
		idx = 0;
	}

	/**
	 * Returns the Preprocessor used by this CppReader.
	 */
	public Preprocessor getPreprocessor() {
		return cpp;
	}

	/**
	 * Defines the given name as a macro.
	 *
	 * This is a convnience method.
	 */
	public void addMacro(String name)
						throws LexerException {
		cpp.addMacro(name);
	}

	/**
	 * Defines the given name as a macro.
	 *
	 * This is a convnience method.
	 */
	public void addMacro(String name, String value)
						throws LexerException {
		cpp.addMacro(name, value);
	}

	private boolean refill()
						throws IOException {
		try {
			assert cpp != null : "cpp is null : was it closed?";
			if (token == null)
				return false;
			while (idx >= token.length()) {
				Token	tok = cpp.token();
				switch (tok.getType()) {
					case EOF:
						token = null;
						return false;
					case COMMENT:
						if (false) {
							token = " ";
							break;
						}
					default:
						token = tok.getText();
						break;
				}
				idx = 0;
			}
			return true;
		}
		catch (LexerException e) {
			IOException	ie = new IOException(String.valueOf(e));
			ie.initCause(e);
			throw ie;
		}
	}

	public int read()
						throws IOException {
		if (!refill())
			return -1;
		return token.charAt(idx++);
	}

	/* XXX Very slow and inefficient. */
	public int read(char cbuf[], int off, int len)
						throws IOException {
		if (token == null)
			return -1;
		for (int i = 0; i < len; i++) {
			int	ch = read();
			if (ch == -1)
				return i;
			cbuf[off + i] = (char)ch;
		}
		return len;
	}

	public void close()
						throws IOException {
		cpp = null;
		token = null;
	}

}
