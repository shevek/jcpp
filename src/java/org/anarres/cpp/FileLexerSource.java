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

import java.util.List;
import java.util.Iterator;

import static org.anarres.cpp.Token.*;

/**
 * A {@link Source} which lexes a file.
 *
 * The input is buffered.
 *
 * @see Source
 */
public class FileLexerSource extends LexerSource {
	private File	file;

	/**
	 * Creates a new Source for lexing the given File.
	 *
	 * Preprocessor directives are honoured within the file.
	 */
	public FileLexerSource(File file)
						throws IOException {
		super(
			new BufferedReader(
				new FileReader(
					file
				)
			),
			true
		);

		this.file = file;
	}

	@Override
	/* pp */ File getFile() {
		return file;
	}

	@Override
	/* pp */ String getName() {
		return String.valueOf(file);
	}

	public String toString() {
		return "file " + file;
	}
}
