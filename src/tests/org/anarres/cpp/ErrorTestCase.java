package org.anarres.cpp;

import java.io.*;

import junit.framework.Test;

import static org.anarres.cpp.Token.*;

public class ErrorTestCase extends BaseTestCase {

	private void testError(Preprocessor p)
						throws LexerException,
								IOException {
		for (;;) {
			Token	tok = p.token();
			if (tok.getType() == EOF)
				break;
			else if (tok.getType() == ERROR)
				throw new LexerException("Error token: " + tok);
		}

	}

	private void testError(String input) throws Exception {
		StringLexerSource		sl;
		PreprocessorListener	pl;
		Preprocessor			p;

		/* Without a PreprocessorListener, throws an exception. */
		sl = new StringLexerSource(input, true);
		p = new Preprocessor();
		p.addInput(sl);
		try {
			testError(p);
			fail("Lexing succeeded unexpectedly on " + input);
		}
		catch (LexerException e) {
			/* ignored */
		}

		/* With a PreprocessorListener, records the error. */
		sl = new StringLexerSource(input, true);
		p = new Preprocessor();
		p.addInput(sl);
		pl = new PreprocessorListener();
		p.setListener(pl);
		assertNotNull("CPP has listener", p.getListener());
		testError(p);
		assertTrue("Listener has errors", pl.getErrors() > 0);
	}

	public void testErrors() throws Exception {
		testError("\"");
		testError("'");
		testError("''");
	}

}
