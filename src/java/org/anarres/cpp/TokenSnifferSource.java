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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import static org.anarres.cpp.Token.*;

@Deprecated
/* pp */ class TokenSnifferSource extends Source {
	private List<Token>	target;

	/* pp */ TokenSnifferSource(List<Token> target) {
		this.target = target;
	}

	public Token token()
						throws IOException,
								LexerException {
		Token	tok = getParent().token();
		if (tok.getType() != EOF)
			target.add(tok);
		return tok;
	}

	public String toString() {
		return getParent().toString();
	}
}
