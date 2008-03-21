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

/**
 * A Preprocessor token.
 *
 * @see Preprocessor
 */
public final class Token {

	// public static final int	EOF        = -1;

	private int		type;
	private int		line;
	private int		column;
	private Object	value;
	private String	text;

	public Token(int type, int line, int column,
					String text, Object value) {
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = text;
		this.value = value;
	}

	public Token(int type, int line, int column, String text) {
		this(type, line, column, text, null);
	}

	/* pp */ Token(int type, String text, Object value) {
		this(type, -1, -1, text, value);
	}

	/* pp */ Token(int type, String text) {
		this(type, text, null);
	}

	/* pp */ Token(int type) {
		this(type, texts[type]);
	}

	/**
	 * Returns the semantic type of this token.
	 */
	public int getType() {
		return type;
	}

	/* pp */ void setLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the line at which this token started.
	 *
	 * Lines are numbered from zero.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the column at which this token started.
	 *
	 * Columns are numbered from zero.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Returns the original or generated text of this token.
	 *
	 * This is distinct from the semantic value of the token.
	 *
	 * @see #getValue()
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the semantic value of this token.
	 *
	 * For strings, this is the parsed String.
	 * For integers, this is an Integer object.
	 * For other token types, as appropriate.
	 *
	 * @see #getText()
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns a description of this token, for debugging purposes.
	 */
	public String toString() {
		StringBuilder	buf = new StringBuilder();

		buf.append('[').append(getTokenName(type));
		if (line != -1) {
			buf.append('@').append(line);
			if (column != -1)
				buf.append(',').append(column);
		}
		buf.append("]:");
		if (text != null)
			buf.append('"').append(text).append('"');
		else if (type > 3 && type < 256)
			buf.append( (char)type );
		else
			buf.append('<').append(type).append('>');
		if (value != null)
			buf.append('=').append(value);
		return buf.toString();
	}

	/**
	 * Returns the descriptive name of the given token type.
	 *
	 * This is mostly used for stringification and debugging.
	 */
	public static final String getTokenName(int type) {
		if (type < 0)
			return "Invalid" + type;
		if (type >= names.length)
			return "Invalid" + type;
		if (names[type] == null)
			return "Unknown" + type;
		return names[type];
	}

#set ($i = 257)
#set ($tokens = [ "AND_EQ", "ARROW", "CHARACTER", "COMMENT", "DEC", "DIV_EQ", "ELLIPSIS", "EOF", "EQ", "GE", "HASH", "HEADER", "IDENTIFIER", "INC", "INTEGER", "LAND", "LAND_EQ", "LE", "LITERAL", "LOR", "LOR_EQ", "LSH", "LSH_EQ", "MOD_EQ", "MULT_EQ", "NE", "NL", "OR_EQ", "PASTE", "PLUS_EQ", "RANGE", "RSH", "RSH_EQ", "STRING", "SUB_EQ", "WHITESPACE", "XOR_EQ", "M_ARG", "M_PASTE", "M_STRING", "P_LINE", "ERROR" ])
#foreach ($token in $tokens)
	/** The token type $token. */
	public static final int $token = $i;
#set ($i = $i + 1)
#end
	/**
	 * The number of possible semantic token types.
	 *
	 * Please note that not all token types below 255 are used.
	 */
	public static final int _TOKENS = $i;

	/** The position-less space token. */
	/* pp */ static final Token	 space = new Token(WHITESPACE, -1, -1, " ");

	private static final String[] names = new String[_TOKENS];
	private static final String[] texts = new String[_TOKENS];
	static {
		for (int i = 0; i < 255; i++) {
			texts[i] = String.valueOf(new char[] { (char)i });
			names[i] = texts[i];
		}

		texts[AND_EQ]      = "&=";
		texts[ARROW]       = "->";
		texts[DEC]         = "--";
		texts[DIV_EQ]      = "/=";
		texts[ELLIPSIS]    = "...";
		texts[EQ]          = "==";
		texts[GE]          = ">=";
		texts[HASH]        = "#";
		texts[INC]         = "++";
		texts[LAND]        = "&&";
		texts[LAND_EQ]     = "&&=";
		texts[LE]          = "<=";
		texts[LOR]         = "||";
		texts[LOR_EQ]      = "||=";
		texts[LSH]         = "<<";
		texts[LSH_EQ]      = "<<=";
		texts[MOD_EQ]      = "%=";
		texts[MULT_EQ]     = "*=";
		texts[NE]          = "!=";
		texts[NL]          = "\n";
		texts[OR_EQ]       = "|=";
		/* We have to split the two hashes or Velocity eats them. */
		texts[PASTE]       = "#" + "#";
		texts[PLUS_EQ]     = "+=";
		texts[RANGE]       = "..";
		texts[RSH]         = ">>";
		texts[RSH_EQ]      = ">>=";
		texts[SUB_EQ]      = "-=";
		texts[XOR_EQ]      = "^=";

#foreach ($token in $tokens)
		names[$token] = "$token";
#end
	}

}
