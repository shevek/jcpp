package org.anarres.cpp;

import java.util.Arrays;
import org.junit.Test;
import static org.anarres.cpp.Token.*;
import static org.junit.Assert.*;

public class LexerSourceTest {

    private void testLexerSource(String in, boolean textmatch, int... out)
            throws Exception {
        System.out.println("Testing '" + in + "' => "
                + Arrays.toString(out));
        StringLexerSource s = new StringLexerSource(in);

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < out.length; i++) {
            Token tok = s.token();
            System.out.println("Token is " + tok);
            assertEquals(out[i], tok.getType());
            // assertEquals(col, tok.getColumn());
            buf.append(tok.getText());
        }

        Token tok = s.token();
        System.out.println("Token is " + tok);
        assertEquals(EOF, tok.getType());

        if (textmatch)
            assertEquals(in, buf.toString());
    }

    @Test
    public void testLexerSource()
            throws Exception {

        testLexerSource("int a = 5;", true,
                IDENTIFIER, WHITESPACE, IDENTIFIER, WHITESPACE,
                '=', WHITESPACE, NUMBER, ';'
        );

        // \n is WHITESPACE because ppvalid = false
        testLexerSource("# #   \r\n\n\r \rfoo", true,
                HASH, WHITESPACE, '#', WHITESPACE, IDENTIFIER
        );

        // No match - trigraphs
        testLexerSource("%:%:", false, PASTE);
        testLexerSource("%:?", false, '#', '?');
        testLexerSource("%:%=", false, '#', MOD_EQ);

        testLexerSource("0x1234ffdUL 0765I", true,
                NUMBER, WHITESPACE, NUMBER);

        testLexerSource("+= -= *= /= %= <= >= >>= <<= &= |= ^= x", true,
                PLUS_EQ, WHITESPACE,
                SUB_EQ, WHITESPACE,
                MULT_EQ, WHITESPACE,
                DIV_EQ, WHITESPACE,
                MOD_EQ, WHITESPACE,
                LE, WHITESPACE,
                GE, WHITESPACE,
                RSH_EQ, WHITESPACE,
                LSH_EQ, WHITESPACE,
                AND_EQ, WHITESPACE,
                OR_EQ, WHITESPACE,
                XOR_EQ, WHITESPACE,
                IDENTIFIER);

        testLexerSource("/**/", true, CCOMMENT);
        testLexerSource("/* /**/ */", true, CCOMMENT, WHITESPACE, '*', '/');
        testLexerSource("/** ** **/", true, CCOMMENT);
        testLexerSource("//* ** **/", true, CPPCOMMENT);
        testLexerSource("'\\r' '\\xf' '\\xff' 'x' 'aa' ''", true,
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                SQSTRING, WHITESPACE,
                SQSTRING);

        testLexerSource("1i1I1l1L1ui1ul", true,
                NUMBER, NUMBER,
                NUMBER, NUMBER,
                NUMBER, NUMBER);

        testLexerSource("'' 'x' 'xx'", true,
                SQSTRING, WHITESPACE, CHARACTER, WHITESPACE, SQSTRING);
    }

    @Test
    public void testNumbers() throws Exception {
        testLexerSource("0", true, NUMBER);
        testLexerSource("045", true, NUMBER);
        testLexerSource("45", true, NUMBER);
        testLexerSource("0.45", true, NUMBER);
        testLexerSource("1.45", true, NUMBER);
        testLexerSource("1e6", true, NUMBER);
        testLexerSource("1.45e6", true, NUMBER);
        testLexerSource(".45e6", true, NUMBER);
        testLexerSource("-6", true, '-', NUMBER);
    }
}
