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

    private JavaFileObject myDBObjJavaSource = JavaFileObjects.forResource("org/example/MyDBObj.java");
    private JavaFileObject myExtendObjJavaSource = JavaFileObjects.forResource("org/example/ExtendObj.java");

    private static String generateJava(InputStream dboIn, int dboindex) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(dboIn);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);

        NodeVisitor visitor = new NodeVisitor();
        Nodes.FileNode fileNode = (Nodes.FileNode) visitor.visitFile(parser.file());


        JavaCodeGenerator generator = new JavaCodeGenerator(fileNode.dboList.get(dboindex), fileNode);

        StringBuffer sourceBuf = new StringBuffer();
        generator.createJavaFile().writeTo(sourceBuf);

        return sourceBuf.toString();
    }

    @Test
    public void expectedMyDBOBJSourceCompiles() {
        ASSERT.about(javaSource())
                .that(myDBObjJavaSource)
                .compilesWithoutError();
    }

    @Test
    public void expectedExtendObJSourceCompiles() {
        ASSERT.about(javaSource())
                .that(myExtendObjJavaSource)
                .compilesWithoutError();
    }

    @Test
    public void generatedMyDBObjSourceMatchesExpected() throws Exception {

        InputStream dboIn = Resources.getResource("org/example/test.dbo").openStream();
        String javaSource = generateJava(dboIn, 0);
        dboIn.close();

        JavaFileObject generatedSource = JavaFileObjects.forSourceString("org/example/MyDBObj.java", javaSource);

        ASSERT.about(javaSource())
                .that(generatedSource)
                .parsesAs(myDBObjJavaSource);
    }

    @Test
    public void generatedExtendObjSourceMatchesExpected() throws Exception {
        InputStream dboIn = Resources.getResource("org/example/test.dbo").openStream();
        String javaSource = generateJava(dboIn, 1);
        dboIn.close();

        JavaFileObject generatedSource = JavaFileObjects.forSourceString("org/example/ExtendDBObj.java", javaSource);

        ASSERT.about(javaSource())
                .that(generatedSource)
                .parsesAs(myExtendObjJavaSource);
    }
}
