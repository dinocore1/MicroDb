package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import com.google.common.io.Resources;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;

public class CompilerTest {

    @Test
    public void testBad1Syntax() throws Exception{
        Generator gen = new Generator();
        InputStream in = Resources.getResource("com/devsmart/microdb/bad1.dbo").openStream();
        ANTLRInputStream antlrIn = new ANTLRInputStream(in);
        final boolean compileSuccess = gen.compileInputStream(antlrIn);
        assertFalse(compileSuccess);
    }

    @Test
    public void testBad2Syntax() throws Exception{
        Generator gen = new Generator();
        InputStream in = Resources.getResource("com/devsmart/microdb/bad2.dbo").openStream();
        ANTLRInputStream antlrIn = new ANTLRInputStream(in);
        final boolean compileSuccess = gen.compileInputStream(antlrIn);
        assertFalse(compileSuccess);
    }

    @Test
    public void testBadFieldObject() throws Exception {
        InputStream in = Resources.getResource("com/devsmart/microdb/bad3.dbo").openStream();

        CompilerContext compilerContext = new CompilerContext();

        ANTLRInputStream inputStream = new ANTLRInputStream(in);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);

        MicroDBParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        Nodes.FileNode file = (Nodes.FileNode) semPass1.visitFile(root);
        SemPass2 semPass2 = new SemPass2(compilerContext);
        semPass2.visit(root);

        JavaCodeGenerator generator = new JavaCodeGenerator(file.dboList.get(0), file);
        generator.generateCode();

        assertTrue(compilerContext.hasErrors());
    }

    @Test
    public void testCodeBlock() throws Exception{
        Generator gen = new Generator();
        InputStream in = Resources.getResource("com/devsmart/microdb/codeblock.dbo").openStream();
        ANTLRInputStream antlrIn = new ANTLRInputStream(in);
        final boolean compileSuccess = gen.compileInputStream(antlrIn);
        assertTrue(compileSuccess);
    }
}
