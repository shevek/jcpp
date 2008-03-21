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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import static org.anarres.cpp.Token.*;

/**
 * A macro argument.
 *
 * This encapsulates a raw and preprocessed token stream.
 */
/* pp */ class Argument extends ArrayList<Token> {
	public static final int	NO_ARGS = -1;

	private List<Token>	expansion;

	public Argument() {
		this.expansion = null;
	}

	public void addToken(Token tok) {
		add(tok);
	}

	/* pp */ void expand(Preprocessor p)
						throws IOException,
								LexerException {
		/* Cache expansion. */
		if (expansion == null) {
			this.expansion = p.expand(this);
			// System.out.println("Expanded arg " + this);
		}
	}

	public Iterator<Token> expansion() {
		return expansion.iterator();
	}

	public String toString() {
		StringBuilder	buf = new StringBuilder();
		buf.append("Argument(");
		// buf.append(super.toString());
		buf.append("raw=[ ");
		for (int i = 0; i < size(); i++)
			buf.append(get(i).getText());
		buf.append(" ];expansion=[ ");
		if (expansion == null)
			buf.append("null");
		else
			for (int i = 0; i < expansion.size(); i++)
				buf.append(expansion.get(i).getText());
		buf.append(" ])");
		return buf.toString();
	}

}
