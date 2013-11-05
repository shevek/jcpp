/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2008, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing
 * permissions and limitations under the License.
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

/* This source should always be active, since we don't expand macros
 * in any inactive context. */
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

	/* XXX Called from Preprocessor [ugly]. */
	/* pp */ static void escape(StringBuilder buf, CharSequence cs) {
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
		// System.out.println("Concat: " + arg + " -> " + buf);
		StringBuilder	str = new StringBuilder("\"");
		escape(str, buf);
		str.append("\"");
		// System.out.println("Escape: " + buf + " -> " + str);
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
		Token			err = null;
		/* We know here that arg is null or expired,
		 * since we cannot paste an expanded arg. */

		int	count = 2;
		for (int i = 0; i < count; i++) {
			if (!tokens.hasNext()) {
				/* XXX This one really should throw. */
				error(ptok.getLine(), ptok.getColumn(),
						"Paste at end of expansion");
				buf.append(' ').append(ptok.getText());
				break;
			}
			Token	tok = tokens.next();
			// System.out.println("Paste " + tok);
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
				case CCOMMENT:
				case CPPCOMMENT:
					break;
				default:
					buf.append(tok.getText());
					break;
			}
		}

		/* Push and re-lex. */
		/*
		StringBuilder		src = new StringBuilder();
		escape(src, buf);
		StringLexerSource	sl = new StringLexerSource(src.toString());
		*/
		StringLexerSource	sl = new StringLexerSource(buf.toString());

		/* XXX Check that concatenation produces a valid token. */

		arg = new SourceIterator(sl);
	}

	public Token token()
						throws IOException,
								LexerException {
		for (;;) {
			/* Deal with lexed tokens first. */

			if (arg != null) {
				if (arg.hasNext()) {
					Token	tok = arg.next();
					/* XXX PASTE -> INVALID. */
					assert tok.getType() != M_PASTE :
								"Unexpected paste token";
					return tok;
				}
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
