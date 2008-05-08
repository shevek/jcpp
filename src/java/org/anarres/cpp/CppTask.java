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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.Token;

public class CppTask extends Task {

	public static class Macro {
		private String name;
		private String value;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private File			input;
	private File			output;
	private Preprocessor	cpp;

	public CppTask() {
		super();
		cpp = new Preprocessor();
	}

	public void setInput(File input) {
		this.input = input;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public void addMacro(Macro macro) {
		try {
			cpp.addMacro(macro.getName(), macro.getValue());
		}
		catch (LexerException e) {
			throw new BuildException(e);
		}
	}

	public void execute() {
		FileWriter writer = null;
		try {
			if (input == null)
				throw new BuildException("Input not specified");
			if (output == null)
				throw new BuildException("Output not specified");
			cpp.addInput(this.input);
			writer = new FileWriter(this.output);
			for (;;) {
				Token	tok = cpp.token();
				if (tok != null && tok.getType() == Token.EOF)
					break;
				writer.write(tok.getText());
			}
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

}
