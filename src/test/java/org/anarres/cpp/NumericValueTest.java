package org.anarres.cpp;

import java.io.IOException;
import org.junit.Test;
import static org.anarres.cpp.Token.*;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class NumericValueTest {

    private Token testNumericValue(String in) throws IOException, LexerException {
        StringLexerSource s = new StringLexerSource(in);

        Token tok = s.token();
        System.out.println("Token is " + tok);
        assertEquals(NUMBER, tok.getType());

        Token eof = s.token();
        assertEquals("Didn't get EOF, but " + tok, EOF, eof.getType());

        return tok;
    }

    private void testNumericValue(String in, double out) throws IOException, LexerException {
        System.out.println("Testing '" + in + "' -> " + out);
        Token tok = testNumericValue(in);
        assertEquals(in, tok.getText());
        NumericValue value = (NumericValue) tok.getValue();
        assertEquals("Double mismatch", out, value.doubleValue(), 0.01d);
        assertEquals("Float mismatch", (float) out, value.floatValue(), 0.01f);
        assertEquals("Long mismatch", (long) out, value.longValue());
        assertEquals("Integer mismatch", (int) out, value.intValue());
    }

    @Test
    public void testNumericValue() throws Exception {

        // Zero
        testNumericValue("0", 0);

        // Decimal
        testNumericValue("1", 1);
        testNumericValue("1L", 1);
        testNumericValue("12", 12);
        testNumericValue("12L", 12);

        // Hex
        testNumericValue("0xf", 0xf);
        testNumericValue("0xfL", 0xf);
        testNumericValue("0x12", 0x12);
        testNumericValue("0x12L", 0x12);

        // Negative
        testNumericValue("-0", 0);
        testNumericValue("-1", -1);

        // Negative hex
        testNumericValue("-0x56", -0x56);
        testNumericValue("-0x102", -0x102);

        // Octal and negative octal
        testNumericValue("0673", Integer.parseInt("673", 8));
        testNumericValue("-0673", Integer.parseInt("-673", 8));

        // Floating point
        testNumericValue(".0", 0);
        testNumericValue(".00", 0);
        testNumericValue("0.", 0);
        testNumericValue("0.0", 0);
        testNumericValue("00.0", 0);
        testNumericValue("00.", 0);

        // Sign on exponents
        testNumericValue("1e1", 1e1);
        testNumericValue("-1e1", -1e1);
        testNumericValue("1e-1", 1e-1);

        // Based numbers with exponents
        // testNumericValue("012e3", 012e3);    // Fails
        testNumericValue("0x12e3", 0x12e3);
        testNumericValue("0x12p3", 0x12p3);

        // Octal prefix with decimal suffix
        // testNumericValue("067e8", 067e8);    // Fails

    }
}
