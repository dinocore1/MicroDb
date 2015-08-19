package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBValueFactory;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.TreeMap;

public class ProxyFileGenerator {

    public static final String MICRODB_PACKAGE = "com.devsmart.microdb";

    private final ProcessingEnvironment mEnv;
    private final TypeElement mClassElement;
    private String mSimpleProxyClassName;

    public ProxyFileGenerator(ProcessingEnvironment env, TypeElement classElement) {
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
        // Get the package of the class
        Element enclosingElement = mClassElement.getEnclosingElement();
        if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            error("MicroDB DBObj does not support nested classes", mClassElement);
            return false;
        }

        if (! mClassElement.getSuperclass().toString().equals(DBObject.class.getCanonicalName())) {
            error("MicroDB DBObj must be derived from " + DBObject.class.getSimpleName(), mClassElement);
            return false;
        }

        mSimpleProxyClassName = mClassElement.getSimpleName().toString();


        return true;
    }

    private TypeMirror toTypeMirror(Class<?> type) {
        TypeElement element = mEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return element.asType();
    }

    private boolean isAcceptableType(VariableElement field) {
        TypeMirror fieldType = field.asType();
        final String fqClassName = fieldType.toString();

        if(mEnv.getTypeUtils().isSameType(fieldType, toTypeMirror(String.class))){
            return true;
        } else if(mEnv.getTypeUtils().isSameType(fieldType, toTypeMirror(Link.class))) {
            return true;
        } else if("int".equals(fqClassName)
                || "long".equals(fqClassName)
                || "float".equals(fqClassName)
                || "double".equals(fqClassName)) {
            return true;
        } else {
            return false;
        }

    }

    public void generate() {
        final String proxyClassName = String.format("%s_pxy", mSimpleProxyClassName);
        final String fqClassName = String.format("%s.%s", MICRODB_PACKAGE, proxyClassName);
        try {

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassName)
                    .superclass(TypeName.get(mClassElement.asType()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for(Element member : mClassElement.getEnclosedElements()){

                if(member.getKind() == ElementKind.FIELD
                        && !member.getModifiers().contains(Modifier.TRANSIENT)) {

                    VariableElement field = (VariableElement)member;

                    final String fieldName = field.getSimpleName().toString();
                    if("id".equals(fieldName)) {
                        error("field with name 'id' is reserved");
                    } else {

                        if(!isAcceptableType(field)) {
                            error(String.format("'%s' is not an acceptable type. Persistable objects need to extend DBObject.", field.toString()));
                        } else {

                            MethodSpec toUBObject = generateToUBObjectMethod(fieldName, field);
                            classBuilder.addMethod(toUBObject);

                            MethodSpec getMethod = generateGetMethod(fieldName, field);
                            classBuilder.addMethod(getMethod);

                            MethodSpec setMethod = generateSetMethod(fieldName, field);
                            classBuilder.addMethod(setMethod);
                        }
                    }
                }

            }

            JavaFile proxySourceFile = JavaFile.builder(MICRODB_PACKAGE, classBuilder.build())
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

    private MethodSpec generateToUBObjectMethod(final List<VariableElement> fields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("to")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.get(mClassElement.asType()), "value")
                .returns(TypeName.get(UBObject.class));

        TypeName ubvaluefactory = ClassName.get(UBValueFactory.class);
        TypeName treeMap = ParameterizedTypeName.get(ClassName.get(TreeMap.class), ClassName.get(String.class), ClassName.get(UBValue.class));
        builder.addStatement("$T retval = new $T<>()", treeMap);

        for(VariableElement field : fields) {

            builder.addStatement("retval.put($S, $T.createString(value.$N)",
                    field.getSimpleName(), UBValueFactory.class, field.getSimpleName());

        }


        builder.addStatement("return $T.createObject(retval)", ubvaluefactory);

        return builder.build();
    }

    private MethodSpec generateGetMethod(final String fieldName, final VariableElement field) {
        final String methodName = String.format("get%s%s", fieldName.substring(0, 1).toUpperCase(),
                fieldName.substring(1));

        final TypeMirror fieldType = field.asType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(fieldType));

        if(String.class.getCanonicalName().equals(fieldType.toString())) {
            builder.addStatement("return mData.get($S).asString()", fieldName);
        } else if("int".equals(fieldType.toString())) {
            builder.addStatement("return mData.get($S).asInt()", fieldName);
        }

        return builder.build();
    }

    private MethodSpec generateSetMethod(final String fieldName, final VariableElement field) {
        final String methodName = String.format("set%s%s", fieldName.substring(0, 1).toUpperCase(),
                fieldName.substring(1));

        final TypeMirror fieldType = field.asType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(fieldType), "value")
                .returns(TypeName.VOID);



        if(String.class.getCanonicalName().equals(fieldType.toString())) {
            builder.addStatement("mData.set($S, $T.createString($L))", fieldName, UBValueFactory.class, "value");
        } else if("int".equals(fieldType.toString())) {
            builder.addStatement("mData.set($S, $T.createInt($L))", fieldName, UBValueFactory.class, "value");
        }

        return builder.build();
    }
}
