package org.anarres.cpp;

import javax.annotation.Nonnull;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents all the tokens that form a single preprocessor directive.
 * Additionally informs if the block after
 */
public class PreprocessorDirective {
    private final List<Token> directiveTokens = new ArrayList<>();
    private boolean activeBlock = true;

    public PreprocessorDirective() {
    }

    public  void addToken(@Nonnull Token token) {
        directiveTokens.add(token);
    }

    @Nonnull
    public List<Token> getTokenList() {
        return directiveTokens;
    }

    public boolean isActiveBlock() {
        return this.activeBlock;
    }

    public void setInactiveBlock() {

    }

    @Override
    public String toString() {
        String result = new String();
        for (Token tok : directiveTokens) {
            result += " " + tok.getText();
        }
        return result;
    }
}
