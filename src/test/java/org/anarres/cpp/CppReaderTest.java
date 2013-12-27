package org.anarres.cpp;

import java.util.Collections;

import java.io.StringReader;
import java.io.BufferedReader;
import org.junit.Test;

public class CppReaderTest {

    private void testCppReader(String in, String out)
            throws Exception {
        System.out.println("Testing " + in + " => " + out);
        StringReader r = new StringReader(in);
        CppReader p = new CppReader(r);
        p.getPreprocessor().setSystemIncludePath(
                Collections.singletonList("src/test/resources")
        );
        p.getPreprocessor().getFeatures().add(Feature.LINEMARKERS);
        BufferedReader b = new BufferedReader(p);

        String line;
        while ((line = b.readLine()) != null) {
            System.out.println(" >> " + line);
        }
    }

    @Test
    public void testCppReader()
            throws Exception {
        testCppReader("#include <test0.h>\n", "ab");
    }

}
