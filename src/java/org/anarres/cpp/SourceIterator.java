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

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.anarres.cpp.Token.*;

/**
 * An Iterator for {@link Source Sources},
 * returning {@link Token Tokens}.
 */
public class SourceIterator implements Iterator<Token> {
	private Source	source;
	private Token	tok;

	public SourceIterator(Source s) {
		this.source = s;
		this.tok = null;
	}

	/**
	 * Rethrows IOException inside IllegalStateException.
	 */
	private void advance() {
		try {
			if (tok == null)
				tok = source.token();
		}
		catch (LexerException e) {
			throw new IllegalStateException(e);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns true if the enclosed Source has more tokens.
	 *
	 * The EOF token is never returned by the iterator.
	 * @throws IllegalStateException if the Source
	 *		throws a LexerException or IOException
	 */
	public boolean hasNext() {
		advance();
		return tok.getType() != EOF;
	}

	/**
	 * Returns the next token from the enclosed Source.
	 *
	 * The EOF token is never returned by the iterator.
	 * @throws IllegalStateException if the Source
	 *		throws a LexerException or IOException
	 */
	public Token next() {
		if (!hasNext())
			throw new NoSuchElementException();
		Token	t = this.tok;
		this.tok = null;
		return t;
	}

	/**
	 * Not supported.
	 *
	 * @throws UnsupportedOperationException.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

