/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.cpp;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author shevek
 */
@RunWith(Parameterized.class)
public class RegressionTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegressionTest.class);

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() throws Exception {
        List<Object[]> out = new ArrayList<Object[]>();

        File dir = new File("build/resources/test/regression");
        for (File inFile : dir.listFiles(new PatternFilenameFilter(".*\\.in"))) {
            String name = Files.getNameWithoutExtension(inFile.getName());
            File outFile = new File(dir, name + ".out");
            out.add(new Object[]{name, inFile, outFile});
        }

        return out;
    }

    private final String name;
    private final File inFile;
    private final File outFile;

    public RegressionTest(String name, File inFile, File outFile) {
        this.name = name;
        this.inFile = inFile;
        this.outFile = outFile;
    }

    @Test
    public void testRegression() throws Exception {
        String inText = Files.toString(inFile, Charsets.UTF_8);
        LOG.info("Read " + name + ":\n" + inText);
        CppReader cppReader = new CppReader(new StringReader(inText));
        String cppText = CharStreams.toString(cppReader);
        LOG.info("Generated " + name + ":\n" + cppText);
        if (outFile.exists()) {
            String outText = Files.toString(outFile, Charsets.UTF_8);
            LOG.info("Expected " + name + ":\n" + outText);
            assertEquals(outText, inText);
        }

    }
}
