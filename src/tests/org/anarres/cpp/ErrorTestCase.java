package org.anarres.cpp;

import java.io.*;

import junit.framework.Test;

import static org.anarres.cpp.Token.*;

public class ErrorTestCase extends BaseTestCase {

	private void testError(Source source)
						throws LexerException,
								IOException {
		for (;;) {
			Token	tok = source.token();
			if (tok.getType() == EOF)
				break;
		}

	}

	private void testError(String input) throws Exception {
		StringLexerSource		sl;
		PreprocessorListener	pl;

		/* Without a PreprocessorListener, throws an exception. */
		sl = new StringLexerSource(input, true);
		try {
			testError(sl);
			fail("Lexing succeeded");
		}
		catch (LexerException e) {
			/* ignored */
		}

		/* With a PreprocessorListener, records the error. */
		sl = new StringLexerSource(input, true);
		pl = new PreprocessorListener();
		sl.setListener(pl);
		testError(sl);
		assertTrue(pl.getErrors() > 0);
	}

	public void testErrors() throws Exception {
		testError("\"");
		testError("'");
		testError("''");
	}

}
