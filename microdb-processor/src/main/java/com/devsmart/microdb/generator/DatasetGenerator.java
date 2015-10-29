package com.devsmart.microdb.generator;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.annotations.DataSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import com.sun.tools.javac.code.Attribute;


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


    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if(m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
            if(entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void generate() {

        final String proxyClassName = String.format("MicroDB%s", mClassName);
        PackageElement packageName = mEnv.getElementUtils().getPackageOf(mClassElement);
        final String fqClassName = String.format("%s.%s", packageName, proxyClassName);

        try {

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassName)
                    .superclass(TypeName.get(mClassElement.asType()))
                    .addModifiers(Modifier.PUBLIC);

            AnnotationMirror am = getAnnotationMirror(mClassElement, DataSet.class);
            Attribute.Array array = (Attribute.Array) getAnnotationValue(am, "objects");
            for(Attribute attribute : array.getValue()) {
                DeclaredType type = (DeclaredType) attribute.getValue();



            }



            JavaFile proxySourceFile = JavaFile.builder(packageName.toString(), classBuilder.build())
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
