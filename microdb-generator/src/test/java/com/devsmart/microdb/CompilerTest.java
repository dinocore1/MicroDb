package com.devsmart.microdb;


import com.google.common.io.Resources;
import org.antlr.v4.runtime.ANTLRInputStream;
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
}
