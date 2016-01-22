package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import com.google.common.io.Resources;
import com.google.testing.compile.JavaFileObjects;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.InputStream;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class JavaWriterTest {

    private JavaFileObject expectedSource = JavaFileObjects.forResource("org/example/MyDBObj.java");

    @Test
    public void expectedSourceCompiles() {
        ASSERT.about(javaSource())
                .that(expectedSource)
                .compilesWithoutError();
    }

    @Test
    public void generatedSourceCompiles() throws Exception {


        InputStream dboIn = Resources.getResource("org/example/test.dbo").openStream();
        ANTLRInputStream inputStream = new ANTLRInputStream(dboIn);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);

        NodeVisitor visitor = new NodeVisitor();
        Nodes.FileNode fileNode = (Nodes.FileNode) visitor.visitFile(parser.file());


        JavaCodeGenerator generator = new JavaCodeGenerator(fileNode.dboList.get(0), fileNode);

        StringBuffer sourceBuf = new StringBuffer();
        generator.createJavaFile().writeTo(sourceBuf);

        JavaFileObject generatedSource = JavaFileObjects.forSourceString("org/example/MyDBObj.java", sourceBuf.toString());

        ASSERT.about(javaSource())
                .that(generatedSource)
                .parsesAs(expectedSource);
    }
}
