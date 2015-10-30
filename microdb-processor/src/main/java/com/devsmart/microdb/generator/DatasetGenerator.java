package com.devsmart.microdb.generator;

import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.annotations.DataSet;
import com.devsmart.microdb.annotations.Index;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
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


    private static AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
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

    private TypeMirror toTypeMirror(Class<?> type) {
        TypeElement element = toTypeElement(type);
        return element.asType();
    }

    private TypeElement toTypeElement(Class<?> type) {
        TypeElement element = mEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return element;
    }

    private boolean isStringType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), toTypeMirror(String.class));
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
                visitObjectTypes(classBuilder, type);
            }


            generateInstallMethod(classBuilder);


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

    private void generateInstallMethod(TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("installIndex")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(MicroDB.class, "db")
                .returns(TypeName.VOID);

        for(IndexGenCode indexGen : mCodeGen) {
            indexGen.genInstallIndex(builder);
        }

        classBuilder.addMethod(builder.build());
    }

    private abstract class IndexGenCode {
        private final TypeElement mClassElement;
        private final VariableElement mFieldElement;

        public IndexGenCode(TypeElement classElement, VariableElement field) {
            mClassElement = classElement;
            mFieldElement = field;
        }

        public String indexName() {
            return String.format("%s.%s", mClassElement.getSimpleName(), mFieldElement);
        }

        public abstract void genInstallIndex(MethodSpec.Builder builder);
    }

    private class StringIndex extends IndexGenCode {

        public StringIndex(TypeElement classElement, VariableElement field) {
            super(classElement, field);
        }

        @Override
        public void genInstallIndex(MethodSpec.Builder builder) {



            CodeBlock block = CodeBlock.builder()
                    .add("db.addIndex($S, new $T<String>() {", indexName(), MapFunction.class)
                    .indent()

                    .addStatement(")")
                    .unindent()
                    .build();

            builder.addCode(block);


        }
    }

    private List<IndexGenCode> mCodeGen = new ArrayList<IndexGenCode>();

    private void visitObjectTypes(TypeSpec.Builder classBuilder, DeclaredType type) {
        TypeElement classElement = (TypeElement) type.asElement();

        for(Element member : classElement.getEnclosedElements()) {

            AnnotationMirror am;
            if(member.getKind() == ElementKind.FIELD
                    && !member.getModifiers().contains(Modifier.STATIC)
                    && !member.getModifiers().contains(Modifier.TRANSIENT)
                    && (am = getAnnotationMirror(member, Index.class)) != null) {


                VariableElement field = (VariableElement)member;
                if(isStringType(field)) {
                    mCodeGen.add(new StringIndex(classElement, field));
                }

            }
        }
    }


}
