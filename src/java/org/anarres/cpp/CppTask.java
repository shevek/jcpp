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
