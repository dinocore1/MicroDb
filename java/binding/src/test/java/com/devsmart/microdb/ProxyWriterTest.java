package com.devsmart.microdb;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class ProxyWriterTest {

    private JavaFileObject simpleModel = JavaFileObjects.forResource("pkg/project/SimpleDBModel.java");
    private JavaFileObject expectedSimpleProxy = JavaFileObjects.forResource("com/devsmart/microdb/SimpleDBModel_pxy.java");


    @Test
    public void compileModel() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .compilesWithoutError();

    }

    @Test
    public void compileProcessedModel() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new DBAnnotationProcessor())
                .compilesWithoutError();
    }

    @Test
    public void expectedSourceCompiles() {
        ASSERT.about(javaSource())
                .that(expectedSimpleProxy)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedModel() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new DBAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSimpleProxy);
    }
}
