package com.devsmart.microdb;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class ProxyWriterTest {

    private JavaFileObject simpleModel = JavaFileObjects.forResource("pkg/project/SimpleDBModel.java");



    @Test
    public void testWriter() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .compilesWithoutError();

        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new DBAnnotationProcessor())
                .compilesWithoutError();

    }
}
