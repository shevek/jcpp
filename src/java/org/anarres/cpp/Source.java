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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.anarres.cpp.Token.*;

/**
 * An input to the Preprocessor.
 *
 * Inputs may come from Files, Strings or other sources. The
 * preprocessor maintains a stack of Sources. Operations such as
 * file inclusion or token pasting will push a new source onto
 * the Preprocessor stack. Sources pop from the stack when they
 * are exhausted; this may be transparent or explicit.
 *
 * BUG: Error messages are not handled properly.
 */
public abstract class Source implements Iterable<Token> {
	private Source					parent;
	private boolean					autopop;
	private PreprocessorListener	listener;
	private boolean					werror;

	/* LineNumberReader */

/*
	// We can't do this, since we would lose the LexerException
	private class Itr implements Iterator {
		private Token	next = null;
		private void advance() {
			try {
				if (next != null)
					next = token();
			}
			catch (IOException e) {
				throw new UnsupportedOperationException(
						"Failed to advance token iterator: " +
								e.getMessage()
							);
			}
		}
		public boolean hasNext() {
			return next.getType() != EOF;
		}
		public Token next() {
			advance();
			Token	t = next;
			next = null;
			return t;
		}
		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove tokens from a Source."
						);
		}
	}
*/

	public Source() {
		this.parent = null;
		this.autopop = false;
	}

	/**
	 * Sets the parent source of this source.
	 *
	 * Sources form a singly linked list.
	 */
	/* pp */ void setParent(Source parent, boolean autopop) {
		this.parent = parent;
		this.autopop = autopop;
	}

	/**
	 * Returns the parent source of this source.
	 *
	 * Sources form a singly linked list.
	 */
	/* pp */ final Source getParent() {
		return parent;
	}

	// @OverrideMustInvoke
	/* pp */ void init(Preprocessor pp) {
		setListener(pp.getListener());
		this.werror = pp.getWarnings().contains(Warning.ERROR);
	}

	/**
	 * Sets the listener for this Source.
	 *
	 * Normally this is set by the Preprocessor when a Source is
	 * used, but if you are using a Source as a standalone object,
	 * you may wish to call this.
	 */
	public void setListener(PreprocessorListener pl) {
		this.listener = pl;
	}

	/**
	 * Returns the File currently being lexed.
	 *
	 * If this Source is not a {@link FileLexerSource}, then
	 * it will ask the parent Source, and so forth recursively.
	 * If no Source on the stack is a FileLexerSource, returns null.
	 */
	/* pp */ File getFile() {
		Source	parent = getParent();
		while (parent != null) {
			File	file = parent.getFile();
			if (file != null)
				return file;
			parent = parent.getParent();
		}
		return null;
	}

	/* pp */ String getName() {
		Source	parent = getParent();
		while (parent != null) {
			String	name = parent.getName();
			if (name != null)
				return name;
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Returns the current line number within this Source.
	 */
	public int getLine() {
		Source	parent = getParent();
		if (parent == null)
			return 0;
		return parent.getLine();
	}

	/**
	 * Returns the current column number within this Source.
	 */
	public int getColumn() {
		Source	parent = getParent();
		if (parent == null)
			return 0;
		return parent.getColumn();
	}

	/**
	 * Returns true if this Source is expanding the given macro.
	 *
	 * This is used to prevent macro recursion.
	 */
	/* pp */ boolean isExpanding(Macro m) {
		Source	parent = getParent();
		if (parent != null)
			return parent.isExpanding(m);
		return false;
	}

	/**
	 * Returns true if this Source should be transparently popped
	 * from the input stack.
	 *
	 * Examples of such sources are macro expansions.
	 */
	/* pp */ boolean isAutopop() {
		return autopop;
	}

	/**
	 * Returns true if this source has line numbers.
	 */
	/* pp */ boolean isNumbered() {
		return false;
	}

	/**
	 * Returns the next Token parsed from this input stream.
	 *
	 * @see Token
	 */
	public abstract Token token()
						throws IOException,
								LexerException;

	/**
	 * Returns a token iterator for this Source.
	 */
	public Iterator<Token> iterator() {
		return new SourceIterator(this);
	}

	/**
	 * Skips tokens until the end of line.
	 *
	 * @param white true if only whitespace is permitted on the
	 *	remainder of the line.
	 * @return the NL token.
	 */
	public Token skipline(boolean white)
						throws IOException,
								LexerException {
		for (;;) {
			Token	tok = token();
			switch (tok.getType()) {
				case EOF:
					/* There ought to be a newline before EOF.
					 * At least, in any skipline context. */
					/* XXX Are we sure about this? */
					warning(tok.getLine(), tok.getColumn(),
									"No newline before end of file");
					return tok;
				case NL:
					/* This may contain one or more newlines. */
					return tok;
				case COMMENT:
				case WHITESPACE:
					break;
				default:
					/* XXX Check white, if required. */
					if (white)
						warning(tok.getLine(), tok.getColumn(),
										"Unexpected nonwhite token");
					break;
			}
		}
	}

	protected void error(int line, int column, String msg)
						throws LexerException {
		if (listener != null)
			listener.handleError(this, line, column, msg);
		else
			throw new LexerException("Error at " + line + ":" + column + ": " + msg);
	}

	protected void warning(int line, int column, String msg)
						throws LexerException {
		if (werror)
			error(line, column, msg);
		else if (listener != null)
			listener.handleWarning(this, line, column, msg);
		else
			throw new LexerException("Warning at " + line + ":" + column + ": " + msg);
	}

}
