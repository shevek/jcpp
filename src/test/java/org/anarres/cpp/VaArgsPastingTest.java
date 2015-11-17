/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.cpp;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.Reader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class VaArgsPastingTest {

    private static final Logger LOG = LoggerFactory.getLogger(VaArgsPastingTest.class);

    @Test
    public void testWhitespacePasting() throws IOException {
        String input
                = "#define REGULAR_ARGS(x, y) foo(x, y)\n"
                + "#define REGULAR_ELLIPSIS(x, y...) foo(x, y)\n"
                + "#define REGULAR_VAARGS(x, ...) foo(x, __VA_ARGS__)\n"
                + "#define PASTE_ARGS(x, y) foo(x, ## y)\n"
                + "#define PASTE_ELLIPSIS(x, y...) foo(x, ## y)\n"
                + "#define PASTE_VAARGS(x, ...) foo(x, ## __VA_ARGS__)\n"
                + ""
                + "REGULAR_ARGS(a, b) // REGULAR_ARGS 2\n"
                + "REGULAR_ELLIPSIS(a, b) // REGULAR_ELLIPSIS 2\n"
                + "REGULAR_ELLIPSIS(a) // REGULAR_ELLIPSIS 1\n"
                + "REGULAR_VAARGS(a, b) // REGULAR_VAARGS 2\n"
                + "REGULAR_VAARGS(a) // REGULAR_VAARGS 1\n"
                + ""
                + "PASTE_ARGS(a, b) // PASTE_ARGS 2\n"
                + "PASTE_ELLIPSIS(a, b) // PASTE_ELLIPSIS 2\n"
                + "PASTE_ELLIPSIS(a) // PASTE_ELLIPSIS 1\n"
                + "PASTE_VAARGS(a, b) // PASTE_VAARGS 2\n"
                + "PASTE_VAARGS(a) // PASTE_VAARGS 1\n";
        LOG.info("Input is:\n" + input);
        Preprocessor pp = new Preprocessor();
        pp.addFeature(Feature.KEEPCOMMENTS);
        pp.addInput(new StringLexerSource(input, true));
        Reader r = new CppReader(pp);
        String output = CharStreams.toString(r).trim();
        LOG.info("Output is:\n" + output);
        assertEquals("foo(a, b) // REGULAR_ARGS 2\n"
                + "foo(a, b) // REGULAR_ELLIPSIS 2\n"
                + "foo(a, ) // REGULAR_ELLIPSIS 1\n"
                + "foo(a, b) // REGULAR_VAARGS 2\n"
                + "foo(a, ) // REGULAR_VAARGS 1\n"
                + "foo(a,b) // PASTE_ARGS 2\n"   // cpp outputs a warning and a space after the comma, similar below.
                + "foo(a,b) // PASTE_ELLIPSIS 2\n"
                + "foo(a) // PASTE_ELLIPSIS 1\n"
                + "foo(a,b) // PASTE_VAARGS 2\n"
                + "foo(a) // PASTE_VAARGS 1", output);
    }
}
