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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.anarres.cpp.Token.*;

/* pp */ class MacroTokenSource extends Source {
	private Macro				macro;
	private Iterator<Token>		tokens;	/* Pointer into the macro.  */
	private List<Argument>		args;	/* { unexpanded, expanded } */
	private Iterator<Token>		arg;	/* "current expansion" */

	/* pp */ MacroTokenSource(Macro m, List<Argument> args) {
		this.macro = m;
		this.tokens = m.getTokens().iterator();
		this.args = args;
		this.arg = null;
	}

	@Override
	/* pp */ boolean isExpanding(Macro m) {
		/* When we are expanding an arg, 'this' macro is not
		 * being expanded, and thus we may re-expand it. */
		if (/* XXX this.arg == null && */ this.macro == m)
			return true;
		return super.isExpanding(m);
	}

	private static void escape(StringBuilder buf, CharSequence cs) {
		for (int i = 0; i < cs.length(); i++) {
			char	c = cs.charAt(i);
			switch (c) {
				case '\\':
					buf.append("\\\\");
					break;
				case '"':
					buf.append("\\\"");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\r':
					buf.append("\\r");
					break;
				default:
					buf.append(c);
			}
		}
	}

	private void concat(StringBuilder buf, Argument arg) {
		Iterator<Token>	it = arg.iterator();
		while (it.hasNext()) {
			Token	tok = it.next();
			buf.append(tok.getText());
		}
	}

	private Token stringify(Token pos, Argument arg) {
		StringBuilder	buf = new StringBuilder();
		concat(buf, arg);
		StringBuilder	str = new StringBuilder("\"");
		escape(str, buf);
		str.append('\"');
		return new Token(STRING,
				pos.getLine(), pos.getColumn(),
				str.toString(), buf.toString());
	}


	/* At this point, we have consumed the first M_PASTE.
	 * @see Macro#addPaste(Token) */
	private void paste(Token ptok)
						throws IOException,
								LexerException {
		StringBuilder	buf = new StringBuilder();
		/* We know here that arg is null or expired,
		 * since we cannot paste an expanded arg. */

		int	count = 2;
		for (int i = 0; i < count; i++) {
			if (!tokens.hasNext())
				error(ptok.getLine(), ptok.getColumn(),
						"Paste at end of expansion");
			Token	tok = tokens.next();
			switch (tok.getType()) {
				case M_PASTE:
					/* One extra to paste, plus one because the
					 * paste token didn't count. */
					count += 2;
					ptok = tok;
					break;
				case M_ARG:
					int idx = ((Integer)tok.getValue()).intValue();
					concat(buf, args.get(idx));
					break;
				/* XXX Test this. */
				case COMMENT:
					break;
				default:
					buf.append(tok.getText());
					break;
			}
		}

		/* XXX Somewhere here, need to check that concatenation
		 * produces a valid token. */

		/* Push and re-lex. */
		StringBuilder		src = new StringBuilder();
		escape(src, buf);
		StringLexerSource	sl = new StringLexerSource(src.toString());

		arg = new SourceIterator(sl);
	}

	public Token token()
						throws IOException,
								LexerException {
		for (;;) {
			/* Deal with lexed tokens first. */

			if (arg != null) {
				if (arg.hasNext())
					return arg.next();
				arg = null;
			}

			if (!tokens.hasNext())
				return new Token(EOF, -1, -1, "");	/* End of macro. */
			Token	tok = tokens.next();
			int		idx;
			switch (tok.getType()) {
				case M_STRING:
					/* Use the nonexpanded arg. */
					idx = ((Integer)tok.getValue()).intValue();
					return stringify(tok, args.get(idx));
				case M_ARG:
					/* Expand the arg. */
					idx = ((Integer)tok.getValue()).intValue();
					// System.out.println("Pushing arg " + args.get(idx));
					arg = args.get(idx).expansion();
					break;
				case M_PASTE:
					paste(tok);
					break;
				default:
					return tok;
			}
		} /* for */
	}

	public String toString() {
		StringBuilder	buf = new StringBuilder();
		buf.append("expansion of ").append(macro.getName());
		Source	parent = getParent();
		if (parent != null)
			buf.append(" in ").append(String.valueOf(parent));
		return buf.toString();
	}
}
