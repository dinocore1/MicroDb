package com.devsmart.microdb.generator;

import com.devsmart.microdb.*;
import com.devsmart.microdb.annotations.AutoIncrement;
import com.devsmart.microdb.annotations.DataSet;
import com.devsmart.microdb.annotations.Index;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
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

    private boolean isLongType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), toTypeMirror(Long.class))
                || mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.LONG));

    }

    public void generate() {

        final String proxyClassName = String.format("MicroDB%s", mClassName);
        PackageElement packageName = mEnv.getElementUtils().getPackageOf(mClassElement);
        final String fqClassName = String.format("%s.%s", packageName, proxyClassName);

        try {

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassName)
                    .superclass(TypeName.get(mClassElement.asType()))
                    .addModifiers(Modifier.PUBLIC);

            classBuilder.addField(MicroDB.class, "mDb", Modifier.PRIVATE, Modifier.FINAL);

            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(MicroDB.class, "db")
                    .addCode(CodeBlock.builder().addStatement("mDb = db").build())
                    .build());

            AnnotationMirror am = getAnnotationMirror(mClassElement, DataSet.class);
            Attribute.Array array = (Attribute.Array) getAnnotationValue(am, "objects");
            for(Attribute attribute : array.getValue()) {
                DeclaredType type = (DeclaredType) attribute.getValue();
                visitObjectTypes(classBuilder, type);
            }


            generateInstallMethod(classBuilder);

            for(GenCode genCode : mCodeGen) {
                if(genCode instanceof IndexGenCode) {
                    classBuilder.addMethod(((IndexGenCode) genCode).genQueryIndex());
                }
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

    private void generateInstallMethod(TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("install")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(MicroDB.class, "db")
                .returns(TypeName.VOID)
                .addException(IOException.class)
                ;

        builder.addStatement("super.install(db)");

        for(GenCode indexGen : mCodeGen) {
            if(indexGen instanceof IndexGenCode) {
                ((IndexGenCode)indexGen).genInstallIndex(builder);
            } else if(indexGen instanceof IncrementGenCode) {
                ((IncrementGenCode)indexGen).genInstall(builder);
            }
        }

        classBuilder.addMethod(builder.build());
    }

    private abstract class GenCode {
        final TypeElement mClassElement;
        final VariableElement mFieldElement;

        public GenCode(TypeElement classElement, VariableElement field) {
            mClassElement = classElement;
            mFieldElement = field;
        }

        ClassName createProxyClassname() {
            TypeMirror fieldType = mClassElement.asType();
            Element typeElement = mEnv.getTypeUtils().asElement(fieldType);
            String simpleName = typeElement.getSimpleName().toString();
            return ClassName.get(ProxyFileGenerator.MICRODB_PACKAGE, simpleName+"_pxy");
        }
    }

    private abstract class IndexGenCode extends GenCode {

        public IndexGenCode(TypeElement classElement, VariableElement field) {
            super(classElement, field);
        }

        public String indexName() {
            return String.format("%s.%s", mClassElement.getSimpleName(), mFieldElement);
        }

        public abstract void genInstallIndex(MethodSpec.Builder builder);
        public abstract MethodSpec genQueryIndex();
    }

    private abstract class IncrementGenCode extends GenCode {

        public IncrementGenCode(TypeElement classElement, VariableElement field) {
            super(classElement, field);
        }

        public String varName() {
            return String.format("var%s.%s", mClassElement.getSimpleName(), mFieldElement);
        }

        public abstract void genInstall(MethodSpec.Builder builder);
    }

    private class StringIndex extends IndexGenCode {

        public StringIndex(TypeElement classElement, VariableElement field) {
            super(classElement, field);
        }

        @Override
        public void genInstallIndex(MethodSpec.Builder builder) {

            CodeBlock block = CodeBlock.builder()
                    .add("db.addIndex($S, new $T<String>() {\n", indexName(), MapFunction.class)
                    .indent()
                    .add("@$T\npublic void map($T value, $T<String> emitter) {\n", Override.class, UBValue.class, Emitter.class)
                    .indent()
                    .beginControlFlow("if($T.isValidObject(value, $T.TYPE))", Utils.class, createProxyClassname())
                    .addStatement("$T v = value.asObject().get($S)", UBValue.class, mFieldElement)
                    .beginControlFlow("if(v != null && v.isString())")
                    .addStatement("emitter.emit(v.asString())")
                    .endControlFlow()
                    .endControlFlow()
                    .unindent()
                    .add("}\n")
                    .unindent()
                    .addStatement("})")
                    .build();

            builder.addCode(block);

        }

        @Override
        public MethodSpec genQueryIndex() {
            final String name = String.format("query%sBy%s", mClassElement.getSimpleName(), mFieldElement);

            ParameterizedTypeName objItType = ParameterizedTypeName.get(ClassName.get(Iterable.class),
                    TypeName.get(mClassElement.asType()));

            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addException(IOException.class)
                    .returns(objItType)
                    .addParameter(TypeName.get(mFieldElement.asType()), "min")
                    .addParameter(boolean.class, "minInclusive")
                    .addParameter(TypeName.get(mFieldElement.asType()), "max")
                    .addParameter(boolean.class, "maxInclusive")
                    .addCode(CodeBlock.builder()
                            .addStatement("return mDb.queryIndex($S, $T.class, min, minInclusive, max, maxInclusive)",
                                    indexName(), mClassElement.asType())
                            .build())
                    .build();
        }


    }

    private class LongAutoIncrement extends IncrementGenCode {

        public LongAutoIncrement(TypeElement classElement, VariableElement field) {
            super(classElement, field);
        }

        @Override
        public void genInstall(MethodSpec.Builder builder) {
            CodeBlock block = CodeBlock.builder()
                    .add("db.addChangeListener(new $T() {\n", DefaultChangeListener.class)
                    .indent()
                    .add("@$T\npublic void onBeforeInsert($T driver, $T value) {\n", Override.class, Driver.class, UBValue.class)
                    .indent()
                    .beginControlFlow("if($T.isValidObject(value, $T.TYPE))", Utils.class, createProxyClassname())
                    .addStatement("final long longValue = driver.incrementLongField($S)", varName())
                    .addStatement("value.asObject().put($S, $T.createInt(longValue))", mFieldElement, UBValueFactory.class)
                    .endControlFlow()
                    .unindent()
                    .add("}\n")
                    .unindent()
                    .addStatement("})")
                    .build();

            builder.addCode(block);

        }
    }

    private List<GenCode> mCodeGen = new ArrayList<GenCode>();

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


            if(member.getKind() == ElementKind.FIELD
                    && !member.getModifiers().contains(Modifier.STATIC)
                    && !member.getModifiers().contains(Modifier.TRANSIENT)
                    && (am = getAnnotationMirror(member, AutoIncrement.class)) != null) {

                VariableElement field = (VariableElement) member;
                if(isLongType(field)) {
                    mCodeGen.add(new LongAutoIncrement(classElement, field));
                }

            }

        }
    }


}
