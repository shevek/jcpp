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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A macro object.
 *
 * This encapsulates a name, an argument count, and a token stream
 * for replacement. The replacement token stream may contain the
 * extra tokens {@link Token#M_ARG} and {@link Token#M_STRING}.
 */
public class Macro {
	private String			name;
	/* It's an explicit decision to keep these around here. We don't
	 * need to; the argument token type is M_ARG and the value
	 * is the index. The strings themselves are only used in
	 * stringification of the macro, for debugging. */
	private List<String>	args;
	private boolean			variadic;
	private List<Token>		tokens;

	public Macro(String name) {
		this.name = name;
		this.args = null;
		this.variadic = false;
		this.tokens = new ArrayList<Token>();
	}

	/**
	 * Returns the name of this macro.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the arguments to this macro.
	 */
	public void setArgs(List<String> args) {
		this.args = args;
	}

	/**
	 * Returns true if this is a function-like macro.
	 */
	public boolean isFunctionLike() {
		return args != null;
	}

	/**
	 * Returns the number of arguments to this macro.
	 */
	public int getArgs() {
		return args.size();
	}

	/**
	 * Sets the variadic flag on this Macro.
	 */
	public void setVariadic(boolean b) {
		this.variadic = b;
	}

	/**
	 * Returns true if this is a variadic function-like macro.
	 */
	public boolean isVariadic() {
		return variadic;
	}

	/**
	 * Adds a token to the expansion of this macro.
	 */
	public void addToken(Token tok) {
		this.tokens.add(tok);
	}

	/**
	 * Adds a "paste" operator to the expansion of this macro.
	 *
	 * A paste operator causes the next token added to be pasted
	 * to the previous token when the macro is expanded.
	 * It is an error for a macro to end with a paste token.
	 */
	public void addPaste(Token tok) {
		/*
		 * Given: tok0 ## tok1
		 * We generate: M_PASTE, tok0, tok1
		 * This extends as per a stack language:
		 * tok0 ## tok1 ## tok2 ->
		 *   M_PASTE, tok0, M_PASTE, tok1, tok2
		 */
		this.tokens.add(tokens.size() - 1, tok);
	}

	/* pp */ List<Token> getTokens() {
		return tokens;
	}

	public String toString() {
		StringBuilder	buf = new StringBuilder(name);
		if (args != null) {
			buf.append('(');
			Iterator<String>	it = args.iterator();
			while (it.hasNext()) {
				buf.append(it.next());
				if (it.hasNext())
					buf.append(", ");
				else if (isVariadic())
					buf.append("...");
			}
			buf.append(')');
		}
		if (!tokens.isEmpty()) {
			boolean	paste = false;
			buf.append(" => ");
			for (int i = 0; i < tokens.size(); i++) {
				Token	tok = tokens.get(i);
				if (tok.getType() == Token.M_PASTE) {
					paste = true;
					continue;
				}
				else {
					buf.append(tok.getText());
				}
				if (paste) {
					buf.append(" #" + "# ");
					paste = false;
				}
				// buf.append(tokens.get(i));
			}
		}
		return buf.toString();
	}

}
