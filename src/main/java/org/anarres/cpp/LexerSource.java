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

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import java.util.Set;

import static org.anarres.cpp.Token.*;

/** Does not handle digraphs. */
public class LexerSource extends Source {
	private static final boolean	DEBUG = false;

	private JoinReader		reader;
	private boolean			ppvalid;
	private boolean			bol;
	private boolean			include;

	private boolean			digraphs;

	/* Unread. */
	private int				u0, u1;
	private int				ucount;

	private int				line;
	private int				column;
	private int				lastcolumn;
	private boolean			cr;

	/* ppvalid is:
	 * false in StringLexerSource,
	 * true in FileLexerSource */
	public LexerSource(Reader r, boolean ppvalid) {
		this.reader = new JoinReader(r);
		this.ppvalid = ppvalid;
		this.bol = true;
		this.include = false;

		this.digraphs = true;

		this.ucount = 0;

		this.line = 1;
		this.column = 0;
		this.lastcolumn = -1;
		this.cr = false;
	}

	@Override
	/* pp */ void init(Preprocessor pp) {
		super.init(pp);
		this.digraphs = pp.getFeature(Feature.DIGRAPHS);
		this.reader.init(pp, this);
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getColumn() {
		return column;
	}

	@Override
	/* pp */ boolean isNumbered() {
		return true;
	}

/* Error handling. */

	private final void _error(String msg, boolean error)
						throws LexerException {
		int	_l = line;
		int	_c = column;
		if (_c == 0) {
			_c = lastcolumn;
			_l--;
		}
		else {
			_c--;
		}
		if (error)
			super.error(_l, _c, msg);
		else
			super.warning(_l, _c, msg);
	}

	/* Allow JoinReader to call this. */
	/* pp */ final void error(String msg)
						throws LexerException {
		_error(msg, true);
	}

	/* Allow JoinReader to call this. */
	/* pp */ final void warning(String msg)
						throws LexerException {
		_error(msg, false);
	}

/* A flag for string handling. */

	/* pp */ void setInclude(boolean b) {
		this.include = b;
	}

/*
	private boolean _isLineSeparator(int c) {
		return Character.getType(c) == Character.LINE_SEPARATOR
				|| c == -1;
	}
*/

	/* XXX Move to JoinReader and canonicalise newlines. */
	private static final boolean isLineSeparator(int c) {
		switch ((char)c) {
			case '\r':
			case '\n':
			case '\u2028':
			case '\u2029':
			case '\u000B':
			case '\u000C':
			case '\u0085':
				return true;
			default:
				return (c == -1);
		}
	}


	private int read()
						throws IOException,
								LexerException {
		int c;
		assert ucount <= 2 : "Illegal ucount: " + ucount;
		switch (ucount) {
			case 2:
				ucount = 1;
				c = u1;
				break;
			case 1:
				ucount = 0;
				c = u0;
				break;
			default:
				if (reader == null)
					c = -1;
				else
					c = reader.read();
				break;
		}

		switch (c) {
			case '\r':
				cr = true;
				line++;
				lastcolumn = column;
				column = 0;
				break;
			case '\n':
				if (cr) {
					cr = false;
					break;
				}
				/* fallthrough */
			case '\u2028':
			case '\u2029':
			case '\u000B':
			case '\u000C':
			case '\u0085':
				cr = false;
				line++;
				lastcolumn = column;
				column = 0;
				break;
			case -1:
				cr = false;
				break;
			default:
				cr = false;
				column++;
				break;
		}

/*
		if (isLineSeparator(c)) {
			line++;
			lastcolumn = column;
			column = 0;
		}
		else {
			column++;
		}
*/

		return c;
	}

	/* You can unget AT MOST one newline. */
	private void unread(int c)
						throws IOException {
		/* XXX Must unread newlines. */
		if (c != -1) {
			if (isLineSeparator(c)) {
				line--;
				column = lastcolumn;
				cr = false;
			}
			else {
				column--;
			}
			switch (ucount) {
				case 0:
					u0 = c;
					ucount = 1;
					break;
				case 1:
					u1 = c;
					ucount = 2;
					break;
				default:
					throw new IllegalStateException(
							"Cannot unget another character!"
								);
			}
			// reader.unread(c);
		}
	}

	/* Consumes the rest of the current line into an invalid. */
	private Token invalid(StringBuilder text, String reason)
						throws IOException,
								LexerException {
		int	d = read();
		while (!isLineSeparator(d)) {
			text.append((char)d);
			d = read();
		}
		unread(d);
		return new Token(INVALID, text.toString(), reason);
	}

	private Token ccomment()
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder("/*");
		int				d;
		do {
			do {
				d = read();
				text.append((char)d);
			} while (d != '*');
			do {
				d = read();
				text.append((char)d);
			} while (d == '*');
		} while (d != '/');
		return new Token(CCOMMENT, text.toString());
	}

	private Token cppcomment()
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder("//");
		int				d = read();
		while (!isLineSeparator(d)) {
			text.append((char)d);
			d = read();
		}
		unread(d);
		return new Token(CPPCOMMENT, text.toString());
	}

	private int escape(StringBuilder text)
						throws IOException,
								LexerException {
		int		d = read();
		switch (d) {
			case 'a': text.append('a'); return 0x07;
			case 'b': text.append('b'); return '\b';
			case 'f': text.append('f'); return '\f';
			case 'n': text.append('n'); return '\n';
			case 'r': text.append('r'); return '\r';
			case 't': text.append('t'); return '\t';
			case 'v': text.append('v'); return 0x0b;
			case '\\': text.append('\\'); return '\\';

			case '0': case '1': case '2': case '3':
			case '4': case '5': case '6': case '7':
				int	len = 0;
				int	val = 0;
				do {
					val = (val << 3) + Character.digit(d, 8);
					text.append((char)d);
					d = read();
				} while (++len < 3 && Character.digit(d, 8) != -1);
				unread(d);
				return val;

			case 'x':
				text.append((char)d);
				len = 0;
				val = 0;
				while (len++ < 2) {
					d = read();
					if (Character.digit(d, 16) == -1) {
						unread(d);
						break;
					}
					val = (val << 4) + Character.digit(d, 16);
					text.append((char)d);
				}
				return val;

			/* Exclude two cases from the warning. */
			case '"': text.append('"'); return '"';
			case '\'': text.append('\''); return '\'';

			default:
				warning("Unnecessary escape character " + (char)d);
				text.append((char)d);
				return d;
		}
	}

	private Token character()
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder("'");
		int				d = read();
		if (d == '\\') {
			text.append('\\');
			d = escape(text);
		}
		else if (isLineSeparator(d)) {
			unread(d);
			return new Token(INVALID, text.toString(),
							"Unterminated character literal");
		}
		else if (d == '\'') {
			text.append('\'');
			return new Token(INVALID, text.toString(),
							"Empty character literal");
		}
		else if (!Character.isDefined(d)) {
			text.append('?');
			return invalid(text, "Illegal unicode character literal");
		}
		else {
			text.append((char)d);
		}

		int		e = read();
		if (e != '\'') {
			// error("Illegal character constant");
			/* We consume up to the next ' or the rest of the line. */
			for (;;) {
				if (isLineSeparator(e)) {
					unread(e);
					break;
				}
				text.append((char)e);
				if (e == '\'')
					break;
				e = read();
			}
			return new Token(INVALID, text.toString(),
							"Illegal character constant " + text);
		}
		text.append('\'');
		/* XXX It this a bad cast? */
		return new Token(CHARACTER,
				text.toString(), Character.valueOf((char)d));
	}

	private Token string(char open, char close)
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder();
		text.append(open);

		StringBuilder	buf = new StringBuilder();

		for (;;) {
			int	c = read();
			if (c == close) {
				break;
			}
			else if (c == '\\') {
				text.append('\\');
				if (!include) {
					char	d = (char)escape(text);
					buf.append(d);
				}
			}
			else if (c == -1) {
				unread(c);
				// error("End of file in string literal after " + buf);
				return new Token(INVALID, text.toString(),
						"End of file in string literal after " + buf);
			}
			else if (isLineSeparator(c)) {
				unread(c);
				// error("Unterminated string literal after " + buf);
				return new Token(INVALID, text.toString(),
						"Unterminated string literal after " + buf);
			}
			else {
				text.append((char)c);
				buf.append((char)c);
			}
		}
		text.append(close);
		switch (close) {
			case '"':
				return new Token(STRING,
					text.toString(), buf.toString());
			case '>':
				return new Token(HEADER,
					text.toString(), buf.toString());
			case '\'':
				if (buf.length() == 1)
					return new Token(CHARACTER,
						text.toString(), buf.toString());
				return new Token(SQSTRING,
					text.toString(), buf.toString());
			default:
				throw new IllegalStateException(
					"Unknown closing character " + (char)close);
		}
	}

	private Token _number_suffix(StringBuilder text, NumericValue value, int d)
						throws IOException,
								LexerException {
		int	flags = 0;	// U, I, L, LL, F, D, MSB
		for (;;) {
			if (d == 'U' || d == 'u') {
				if ((flags & NumericValue.F_UNSIGNED) != 0)
					warning("Duplicate unsigned suffix " + d);
				flags |= NumericValue.F_UNSIGNED;
				text.append((char)d);
				d = read();
			}
			else if (d == 'L' || d == 'l') {
				if ((flags & NumericValue.FF_SIZE) != 0)
					warning("Nultiple length suffixes after " + text);
				text.append((char)d);
				int e = read();
				if (e == d) {	// Case must match. Ll is Welsh.
					flags |= NumericValue.F_LONGLONG;
					text.append((char)e);
					d = read();
				} else {
					flags |= NumericValue.F_LONG;
					d = e;
				}
			}
			else if (d == 'I' || d == 'i') {
				if ((flags & NumericValue.FF_SIZE) != 0)
					warning("Nultiple length suffixes after " + text);
				flags |= NumericValue.F_INT;
				text.append((char)d);
				d = read();
			} else if (d == 'F' || d == 'f') {
				if ((flags & NumericValue.FF_SIZE) != 0)
					warning("Nultiple length suffixes after " + text);
				flags |= NumericValue.F_FLOAT;
				text.append((char)d);
				d = read();
			} else if (d == 'D' || d == 'd') {
				if ((flags & NumericValue.FF_SIZE) != 0)
					warning("Nultiple length suffixes after " + text);
				flags |= NumericValue.F_DOUBLE;
				text.append((char)d);
				d = read();
			}
			// This should probably be isPunct() || isWhite().
			else if (Character.isLetter(d) || d == '_') {
				unread(d);
				value.setFlags(flags);
				return invalid(text, 
						"Invalid suffix \"" + (char)d +
						"\" on numeric constant");
			}
			else {
				unread(d);
				value.setFlags(flags);
				return new Token(NUMBER,
					text.toString(), value);
			}
		}
	}

	/* Either a decimal part, or a hex exponent. */
	private String _number_part(StringBuilder text, int base)
						throws IOException,
								LexerException {
		StringBuilder	part = new StringBuilder();
		int				d = read();
		while (Character.digit(d, base) != -1) {
			text.append((char)d);
			part.append((char)d);
			d = read();
		}
		unread(d);
		return part.toString();
	}

	/* We already chewed a zero, so empty is fine. */
	private Token number_octal()
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder("0");
		String			integer = _number_part(text, 8);
		int				d = read();
		NumericValue	value = new NumericValue(8, integer);
		return _number_suffix(text, value, d);
	}

	/* We do not know whether know the first digit is valid. */
	private Token number_hex(char x)
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder("0");
		text.append(x);
		String			integer = _number_part(text, 16);
		NumericValue	value = new NumericValue(16, integer);
		int				d = read();
		if (d == '.') {
			String		fraction = _number_part(text, 16);
			value.setFractionalPart(fraction);
			d = read();
		}
		if (d == 'P' || d == 'p') {
			String		exponent = _number_part(text, 10);
			value.setExponent(exponent);
			d = read();
		}
		// XXX Make sure it's got enough parts
		return _number_suffix(text, value, d);
	}

	/* We know we have at least one valid digit, but empty is not
	 * fine. */
	private Token number_decimal()
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder();
		String			integer = _number_part(text, 10);
		NumericValue	value = new NumericValue(10, integer);
		int				d = read();
		if (d == '.') {
			String		fraction = _number_part(text, 10);
			value.setFractionalPart(fraction);
			d = read();
		}
		if (d == 'E' || d == 'e') {
			String		exponent = _number_part(text, 10);
			value.setExponent(exponent);
			d = read();
		}
		// XXX Make sure it's got enough parts
		return _number_suffix(text, value, d);
	}

	private Token identifier(int c)
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder();
		int				d;
		text.append((char)c);
		for (;;) {
			d = read();
			if (Character.isIdentifierIgnorable(d))
				;
			else if (Character.isJavaIdentifierPart(d))
				text.append((char)d);
			else
				break;
		}
		unread(d);
		return new Token(IDENTIFIER, text.toString());
	}

	private Token whitespace(int c)
						throws IOException,
								LexerException {
		StringBuilder	text = new StringBuilder();
		int				d;
		text.append((char)c);
		for (;;) {
			d = read();
			if (ppvalid && isLineSeparator(d))	/* XXX Ugly. */
				break;
			if (Character.isWhitespace(d))
				text.append((char)d);
			else
				break;
		}
		unread(d);
		return new Token(WHITESPACE, text.toString());
	}

	/* No token processed by cond() contains a newline. */
	private Token cond(char c, int yes, int no)
						throws IOException,
								LexerException {
		int	d = read();
		if (c == d)
			return new Token(yes);
		unread(d);
		return new Token(no);
	}

	public Token token()
						throws IOException,
								LexerException {
		Token	tok = null;

		int		_l = line;
		int		_c = column;

		int		c = read();
		int		d;

		switch (c) {
			case '\n':
				if (ppvalid) {
					bol = true;
					if (include) {
						tok = new Token(NL, _l, _c, "\n");
					}
					else {
						int	nls = 0;
						do {
							nls++;
							d = read();
						} while (d == '\n');
						unread(d);
						char[]	text = new char[nls];
						for (int i = 0; i < text.length; i++)
							text[i] = '\n';
						// Skip the bol = false below.
						tok = new Token(NL, _l, _c, new String(text));
					}
					if (DEBUG)
						System.out.println("lx: Returning NL: " + tok);
					return tok;
				}
				/* Let it be handled as whitespace. */
				break;

			case '!':
				tok = cond('=', NE, '!');
				break;

			case '#':
				if (bol)
					tok = new Token(HASH);
				else
					tok = cond('#', PASTE, '#');
				break;

			case '+':
				d = read();
				if (d == '+')
					tok = new Token(INC);
				else if (d == '=')
					tok = new Token(PLUS_EQ);
				else
					unread(d);
				break;
			case '-':
				d = read();
				if (d == '-')
					tok = new Token(DEC);
				else if (d == '=')
					tok = new Token(SUB_EQ);
				else if (d == '>')
					tok = new Token(ARROW);
				else
					unread(d);
				break;

			case '*':
				tok = cond('=', MULT_EQ, '*');
				break;
			case '/':
				d = read();
				if (d == '*')
					tok = ccomment();
				else if (d == '/')
					tok = cppcomment();
				else if (d == '=')
					tok = new Token(DIV_EQ);
				else
					unread(d);
				break;

			case '%':
				d = read();
				if (d == '=')
					tok = new Token(MOD_EQ);
				else if (digraphs && d == '>')
					tok = new Token('}');	// digraph
				else if (digraphs && d == ':') PASTE: {
					d = read();
					if (d != '%') {
						unread(d);
						tok = new Token('#');	// digraph
						break PASTE;
					}
					d = read();
					if (d != ':') {
						unread(d);	// Unread 2 chars here.
						unread('%');
						tok = new Token('#');	// digraph
						break PASTE;
					}
					tok = new Token(PASTE);	// digraph
				}
				else
					unread(d);
				break;

			case ':':
				/* :: */
				d = read();
				if (digraphs && d == '>')
					tok = new Token(']');	// digraph
				else
					unread(d);
				break;

			case '<':
				if (include) {
					tok = string('<', '>');
				}
				else {
					d = read();
					if (d == '=')
						tok = new Token(LE);
					else if (d == '<')
						tok = cond('=', LSH_EQ, LSH);
					else if (digraphs && d == ':')
						tok = new Token('[');	// digraph
					else if (digraphs && d == '%')
						tok = new Token('{');	// digraph
					else
						unread(d);
				}
				break;

			case '=':
				tok = cond('=', EQ, '=');
				break;

			case '>':
				d = read();
				if (d == '=')
					tok = new Token(GE);
				else if (d == '>')
					tok = cond('=', RSH_EQ, RSH);
				else
					unread(d);
				break;

			case '^':
				tok = cond('=', XOR_EQ, '^');
				break;

			case '|':
				d = read();
				if (d == '=')
					tok = new Token(OR_EQ);
				else if (d == '|')
					tok = cond('=', LOR_EQ, LOR);
				else
					unread(d);
				break;
			case '&':
				d = read();
				if (d == '&')
					tok = cond('=', LAND_EQ, LAND);
				else if (d == '=')
					tok = new Token(AND_EQ);
				else
					unread(d);
				break;

			case '.':
				d = read();
				if (d == '.')
					tok = cond('.', ELLIPSIS, RANGE);
				else
					unread(d);
				if (Character.isDigit(d)) {
					unread('.');
					tok = number_decimal();
				}
				/* XXX decimal fraction */
				break;

			case '0':
				/* octal or hex */
				d = read();
				if (d == 'x' || d == 'X')
					tok = number_hex((char)d);
				else {
					unread(d);
					tok = number_octal();
				}
				break;

			case '\'':
				tok = string('\'', '\'');
				break;

			case '"':
				tok = string('"', '"');
				break;

			case -1:
				close();
				tok = new Token(EOF, _l, _c, "<eof>");
				break;
		}

		if (tok == null) {
			if (Character.isWhitespace(c)) {
				tok = whitespace(c);
			}
			else if (Character.isDigit(c)) {
				unread(c);
				tok = number_decimal();
			}
			else if (Character.isJavaIdentifierStart(c)) {
				tok = identifier(c);
			}
			else {
				tok = new Token(c);
			}
		}

		if (bol) {
			switch (tok.getType()) {
				case WHITESPACE:
				case CCOMMENT:
					break;
				default:
					bol = false;
					break;
			}
		}

		tok.setLocation(_l, _c);
		if (DEBUG)
			System.out.println("lx: Returning " + tok);
		// (new Exception("here")).printStackTrace(System.out);
		return tok;
	}

	public void close()
						throws IOException {
		if (reader != null) {
			reader.close();
			reader = null;
		}
		super.close();
	}

}
