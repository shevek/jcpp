package org.anarres.cpp;

import java.io.BufferedReader;
import java.io.Reader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class IncludeAbsoluteTest {

    private static final Logger LOG = LoggerFactory.getLogger(IncludeAbsoluteTest.class);

    // TODO: Rewrite this test to get the CWD and read a file with known content in the test suite.
    // Guava (available in test suite) can map a URL to a File resource.
    @Test
    public void testAbsoluteInclude() throws Exception {
        Preprocessor pp = new Preprocessor();
        pp.getSystemIncludePath().add("/usr/include");
        pp.addInput(new StringLexerSource(
                "#include </usr/include/features.h>\n"
                + "", true));
        Reader r = new CppReader(pp);
        // This will error if the file isn't found.
        BufferedReader br = new BufferedReader(r);
        for (int i = 0; i < 10; i++) {
            LOG.info(br.readLine());
        }
        br.close();
    }
}
