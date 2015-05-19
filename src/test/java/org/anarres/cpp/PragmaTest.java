/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.cpp;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class PragmaTest {

    private static final Logger LOG = LoggerFactory.getLogger(PragmaTest.class);

    @Test
    public void testPragma() throws Exception {
        File file = new File("build/resources/test/pragma.c");
        assertTrue(file.exists());

        CharSource source = Files.asCharSource(file, Charsets.UTF_8);
        CppReader r = new CppReader(source.openBufferedStream());
        r.getPreprocessor().setListener(new DefaultPreprocessorListener());
        String output = CharStreams.toString(r);
        r.close();
        LOG.info("Output: " + output);
        // assertTrue(output.contains("absolute-result"));
    }
}
