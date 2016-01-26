package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.io.PrintStream;
import java.util.*;

public class CompilerContext {

    private static class CompileMessage {
        public static final int TYPE_ERROR = 0;
        public static final int TYPE_WARNING = 1;
        public static final int TYPE_DEBUG = 2;

        final int type;
        final String message;

        private CompileMessage(int type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", getTypeStr(type), message);
        }

        public static String getTypeStr(int type) {
            switch (type) {
                case TYPE_ERROR:
                    return "ERROR";

                case TYPE_WARNING:
                    return "WARNING";

                case TYPE_DEBUG:
                    return "DEBUG";

                default:
                    return "UNKNOWN";
            }
        }
    }

    public Map<ParserRuleContext, Nodes.Node> nodeMap = new HashMap<ParserRuleContext, Nodes.Node>();
    public List<CompileMessage> compileMessages = new ArrayList<CompileMessage>();

    public CompileMessage error(String msg, Token location) {
        if(location != null) {
            msg = String.format("%s %d:%d %s",
                    location.getTokenSource().getSourceName(),
                    location.getLine(), location.getCharPositionInLine(), msg);
        }
        CompileMessage retval = new CompileMessage(CompileMessage.TYPE_ERROR, msg);
        compileMessages.add(retval);
        return retval;
    }

    public CompileMessage warn(String msg, Token location) {
        if(location != null) {
            msg = String.format("%s %d:%d %s",
                    location.getTokenSource().getSourceName(),
                    location.getLine(), location.getCharPositionInLine(), msg);
        }
        CompileMessage retval = new CompileMessage(CompileMessage.TYPE_WARNING, msg);
        compileMessages.add(retval);
        return retval;
    }


    public ANTLRErrorListener parserErrorHandler = new ANTLRErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            error("Syntax error: " + msg, e.getOffendingToken());
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet ambigAlts, ATNConfigSet configs) {
            error("Ambiguity: ", null);
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, ATNConfigSet configs) {
        }

    };

    public boolean hasErrors() {
        for(CompileMessage msg : compileMessages) {
            if(msg.type == CompileMessage.TYPE_ERROR) {
                return true;
            }
        }
        return false;
    }

    public void reportMessages(PrintStream stream) {
        for(CompileMessage msg : compileMessages) {
            stream.println(msg.toString());
        }

    }
}
