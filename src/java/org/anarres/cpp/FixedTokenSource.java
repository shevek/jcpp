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

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

/* pp */ class FixedTokenSource extends Source {
	private static final Token	EOF =
			new Token(Token.EOF, "<ts-eof>");

	private List<Token>	tokens;
	private int			idx;

	/* pp */ FixedTokenSource(Token... tokens) {
		this.tokens = Arrays.asList(tokens);
		this.idx = 0;
	}

	/* pp */ FixedTokenSource(List<Token> tokens) {
		this.tokens = tokens;
		this.idx = 0;
	}

	public Token token()
						throws IOException,
								LexerException {
		if (idx >= tokens.size())
			return EOF;
		return tokens.get(idx++);
	}

	public String toString() {
		StringBuilder	buf = new StringBuilder();
		buf.append("constant token stream " + tokens);
		Source	parent = getParent();
		if (parent != null)
			buf.append(" in ").append(String.valueOf(parent));
		return buf.toString();
	}
}
