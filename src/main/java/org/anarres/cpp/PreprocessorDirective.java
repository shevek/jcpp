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
    private PreprocessorCommand command;

    public PreprocessorDirective() {
    }

    public  void addToken(@Nonnull Token token) {
        directiveTokens.add(token);
    }

    public void setCommand(PreprocessorCommand cmd) {
        this.command = cmd;
    }

    public PreprocessorCommand getCommand() {
        return this.command;
    }

    @Nonnull
    public List<Token> getTokenList() { return this.directiveTokens; }

    public boolean isActiveBlock() {
        return this.activeBlock;
    }

    public void setInactiveBlock() {
        this.activeBlock = false;
    }

    @Override
    public String toString() {
        String result = new String();
        if (!activeBlock)
            result += "inactive block ";
        for (Token tok : directiveTokens) {
            result += " " + tok.getText();
        }
        return result;
    }
}
