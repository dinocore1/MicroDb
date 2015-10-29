package com.devsmart.microdb.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;


public class DatasetGenerator {

    private final ProcessingEnvironment mEnv;
    private final TypeElement mClassElement;
    private String mClassName;

    public DatasetGenerator(ProcessingEnvironment env, TypeElement classElement) {
        mEnv = env;
        mClassElement = classElement;
    }

    private void error(String message) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void error(String message, Element e) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
    }

    private void note(String message) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    public boolean validate() {
        mClassName = mClassElement.getSimpleName().toString();
        return true;
    }

    public void generate() {

        final String proxyClassName = String.format("MicroDB%s", mClassName);
        PackageElement packageName = mEnv.getElementUtils().getPackageOf(mClassElement);
        final String fqClassName = String.format("%s.%s", packageName, proxyClassName);

        try {

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassName)
                    .superclass(TypeName.get(mClassElement.asType()))
                    .addModifiers(Modifier.PUBLIC);

            JavaFile proxySourceFile = JavaFile.builder(mClassElement.getQualifiedName().toString(), classBuilder.build())
                    .skipJavaLangImports(true)
                    .build();

            JavaFileObject sourceFile = mEnv.getFiler().createSourceFile(fqClassName);
            Writer writer = sourceFile.openWriter();
            proxySourceFile.writeTo(writer);
            writer.close();

        } catch (IOException e) {
            error("error generating proxy class: " + e.getMessage());
        }

    }
}
