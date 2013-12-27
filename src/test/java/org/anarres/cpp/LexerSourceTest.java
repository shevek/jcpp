package org.anarres.cpp;

import java.util.Arrays;
import org.junit.Test;
import static org.anarres.cpp.Token.*;
import static org.junit.Assert.*;

public class LexerSourceTest {

    private void testLexerSource(String in, int... out)
            throws Exception {
        System.out.println("Testing '" + in + "' => "
                + Arrays.toString(out));
        StringLexerSource s = new StringLexerSource(in);

        int col = 0;
        for (int i = 0; i < out.length; i++) {
            Token tok = s.token();
            System.out.println("Token is " + tok);
            assertEquals(out[i], tok.getType());
            // assertEquals(col, tok.getColumn());
            col += tok.getText().length();
        }

        Token tok = s.token();
        System.out.println("Token is " + tok);
        assertEquals(EOF, tok.getType());
    }

    @Test
    public void testLexerSource()
            throws Exception {

        testLexerSource("int a = 5;",
                IDENTIFIER, WHITESPACE, IDENTIFIER, WHITESPACE,
                '=', WHITESPACE, NUMBER, ';', EOF
        );

        // \n is WHITESPACE because ppvalid = false
        testLexerSource("# #   \r\n\n\r \rfoo",
                HASH, WHITESPACE, '#', WHITESPACE, IDENTIFIER
        );

        testLexerSource("%:%:", PASTE);
        testLexerSource("%:?", '#', '?');
        testLexerSource("%:%=", '#', MOD_EQ);
        testLexerSource("0x1234ffdUL 0765I",
                NUMBER, WHITESPACE, NUMBER);

        testLexerSource("+= -= *= /= %= <= >= >>= <<= &= |= ^= x",
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

        testLexerSource("/**/", CCOMMENT);
        testLexerSource("/* /**/ */", CCOMMENT, WHITESPACE, '*', '/');
        testLexerSource("/** ** **/", CCOMMENT);
        testLexerSource("//* ** **/", CPPCOMMENT);
        testLexerSource("'\\r' '\\xf' '\\xff' 'x' 'aa' ''",
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                CHARACTER, WHITESPACE,
                SQSTRING, WHITESPACE,
                SQSTRING);

        testLexerSource("1i1I1l1L1ui1ul",
                NUMBER, NUMBER,
                NUMBER, NUMBER,
                NUMBER, NUMBER);

        testLexerSource("'' 'x' 'xx'",
                SQSTRING, WHITESPACE, CHARACTER, WHITESPACE, SQSTRING);
    }

}
