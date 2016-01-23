package com.devsmart.microdb;


import org.junit.Test;

public class CmdLineTest {

    @Test
    public void testParseCmdLine() {

        String[] args = new String[]{"-d", "genTest", "src/test/resources"};
        Generator.main(args);
    }
}
