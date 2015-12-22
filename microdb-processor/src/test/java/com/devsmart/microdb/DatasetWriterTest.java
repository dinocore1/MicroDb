package com.devsmart.microdb;


import com.devsmart.microdb.generator.DBAnnotationProcessor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class DatasetWriterTest {

    private JavaFileObject simpleDataset = JavaFileObjects.forResource("pkg/project/SimpleDataSet.java");
    private JavaFileObject expectedSimpleProxy = JavaFileObjects.forResource("pkg/project/MicroDBSimpleDataSet.java");


    @Test
    public void compileModel() {
        ASSERT.about(javaSource())
                .that(simpleDataset)
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
                .that(simpleDataset)
                .processedWith(new DBAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSimpleProxy);
    }

}
