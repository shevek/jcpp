package org.anarres.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class IsExpandedFlagTest {

    private Preprocessor preprocessor;

    @Before
    public void setUp() throws Exception {
        preprocessor = new Preprocessor();
    }

    @Test
    public void testSimpleExpansion() throws Exception {
        final List<Token> tokens = getTokens("is_expanded_flag/simple_expansion.c");
        System.out.println(tokens);

        assertEquals(10, tokens.size());

        for (int i = 0; i < 10; ++i) {
            assertEquals(tokens.get(i).toString(), i == 7, tokens.get(i).isExpanded());
        }
    }

    @Test
    public void testPredefinedMacroExpansion() throws Exception {
        preprocessor.addMacro("PREDEFINED", "123");
        final List<Token> tokens = getTokens("is_expanded_flag/predefined_macro_expansion.c");
        System.out.println(tokens);

        assertEquals(9, tokens.size());

        for (int i = 0; i < 9; ++i) {
            assertEquals(tokens.get(i).toString(), i == 6, tokens.get(i).isExpanded());
        }
    }

    @Test
    public void testExpansionWithArgs() throws Exception {
        final List<Token> tokens = getTokens("is_expanded_flag/expansion_with_args.c");
        System.out.println(tokens);

        assertEquals(26, tokens.size());

        for (Token token : tokens) {
            switch (token.getType()) {
                case ':':
                case '?':
                case '>':
                    assertTrue(token.toString(), token.isExpanded());
                    break;
                case Token.WHITESPACE:
                case Token.NL:
                case Token.EOF:
                    // ignore whitespaces, eof
                    break;
                default:
                    assertFalse(token.toString(), token.isExpanded());
                    break;
            }
        }
    }

    @Test
    public void testNestedExpansion() throws Exception {
        final List<Token> tokens = getTokens("is_expanded_flag/nested_expansion.c");
        System.out.println(tokens);

        assertEquals(32, tokens.size());

        for (Token token : tokens) {
            final String text = token.getText();
            switch (token.getType()) {
                case '%':
                case Token.EQ:
                case Token.LAND:
                    assertTrue(token.toString(), token.isExpanded());
                    break;
                case Token.WHITESPACE:
                case Token.NL:
                case Token.EOF:
                    // ignore whitespaces, eof
                    break;
                case Token.NUMBER:
                    if ("0".equals(text)) {
                        assertTrue(token.toString(), token.isExpanded());
                    } else if ("3423".equals(text)) {
                        assertFalse(token.toString(), token.isExpanded());
                    }
                    break;
                default:
                    assertFalse(token.toString(), token.isExpanded());
                    break;
            }
        }
    }

    private List<Token> getTokens(String resourcePath) throws IOException, LexerException {
        final List<Token> result = new ArrayList<>();
        final String filePath = Thread.currentThread()
                .getContextClassLoader()
                .getResource(resourcePath)
                .getFile();
        preprocessor.addInput(new File(filePath));

        Token token = preprocessor.token();
        while (token.getType() != Token.EOF) {
            result.add(token);
            token = preprocessor.token();
        }
        return result;
    }

}
