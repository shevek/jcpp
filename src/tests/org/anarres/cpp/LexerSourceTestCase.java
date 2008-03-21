package org.anarres.cpp;

import java.io.StringReader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import static org.anarres.cpp.Token.*;

public class LexerSourceTestCase extends BaseTestCase implements Test {

	private void testLexerSource(String in, int[] out)
						throws Exception {
		System.out.println("Testing '" + in + "' => " +
						Arrays.toString(out));
		StringLexerSource	s = new StringLexerSource(in);

		for (int i = 0; i < out.length; i++) {
			Token	tok = s.token();
			System.out.println("Token is " + tok);
			assertEquals(out[i], tok.getType());
		}
		assertEquals(EOF, s.token().getType());
	}

	public void testJoinReader()
						throws Exception {

		testLexerSource("int a = 5;", new int[] {
			IDENTIFIER, WHITESPACE, IDENTIFIER, WHITESPACE,
			'=', WHITESPACE, INTEGER, ';', EOF
		});

		testLexerSource("# # foo", new int[] {
			HASH, WHITESPACE, '#', WHITESPACE, IDENTIFIER
		});

	}

}
