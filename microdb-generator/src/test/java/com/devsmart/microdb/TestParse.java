package com.devsmart.microdb;


import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.io.InputStream;

public class TestParse {

    @Test
    public void testLex() throws Exception {
        InputStream input = getClass().getResourceAsStream("simpledbmodel.dbo");
        ANTLRInputStream inputStream = new ANTLRInputStream(input);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        Token token = null;
        while((token = lexer.nextToken()) != null) {
            if(token.getType()==Token.EOF){
                break;
            }

            System.out.println(String.format("token %s %s", lexer.getTokenNames()[token.getType()], token.getText()));

        }

    }

    @Test
    public void testParse() throws Exception {

        InputStream input = getClass().getResourceAsStream("simpledbmodel.dbo");
        ANTLRInputStream inputStream = new ANTLRInputStream(input);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);


        MicroDBParser.FileContext f = parser.file();




    }
}
