package com.devsmart.microdb.generator;


import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.Link;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.microdb.ubjson.UBArray;
import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBString;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBValueFactory;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ProxyFileGenerator {

    public static final String MICRODB_PACKAGE = "com.devsmart.microdb";

    private interface FieldMethodCodeGen {

        void serializeCode(MethodSpec.Builder builder);
        void deserializeCode(MethodSpec.Builder builder);
        void specializedMethods(TypeSpec.Builder builder);

    }

    private class StringDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public StringDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createStringOrNull($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asString())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class BoolDBObjectField implements FieldMethodCodeGen {
        private final VariableElement mField;

        public BoolDBObjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createBool($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));
        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asBool())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class ByteDBObjectField implements FieldMethodCodeGen {
        private final VariableElement mField;

        public ByteDBObjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createInt($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asByte())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class ShortDBObjectField implements FieldMethodCodeGen {
        private final VariableElement mField;

        public ShortDBObjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createInt($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asShort())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class IntDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public IntDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createInt($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asInt())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class LongDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public LongDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createInt($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asLong())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class FloatDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public FloatDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createFloat32($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asFloat32())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class FloatArrayDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public FloatArrayDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createArray($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asFloat32Array())",
                    createSetterName(mField), mField);
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class DoubleDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public DoubleDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createFloat64($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asFloat64())",
                    createSetterName(mField), mField.getSimpleName());
            builder.endControlFlow();
        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class DoubleArrayDBOBjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public DoubleArrayDBOBjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createArray($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("super.$L(obj.get($S).asFloat64Array())",
                    createSetterName(mField), mField.getSimpleName());
            builder.endControlFlow();

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class LinkFieldGen implements FieldMethodCodeGen {
        private final VariableElement mField;

        public LinkFieldGen(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $L.getId())", mField, mField);

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            TypeMirror genericType = ((DeclaredType) mField.asType()).getTypeArguments().get(0);
            builder.addStatement("$L = new $T(obj.get($S), db, $T.class)", mField, mField.asType(), mField, createDBObjName( mEnv.getTypeUtils().asElement(genericType) ));
        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {

        }
    }

    private ClassName createDBObjName(Element field) {
        TypeMirror fieldType = field.asType();
        Element typeElement = mEnv.getTypeUtils().asElement(fieldType);
        String simpleName = typeElement.getSimpleName().toString();
        return ClassName.get(MICRODB_PACKAGE, simpleName+"_pxy");
    }

    private class EmbeddedDBObjectField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public EmbeddedDBObjectField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {

            builder.addCode(CodeBlock.builder()
                    .add("{\n")
                    .indent()

                    .addStatement("$T inst = $N()", TypeName.get(mField.asType()), createGetterName(mField))
                    .beginControlFlow("if(inst != null)")
                    .addStatement("$T obj = new $T()", UBObject.class, UBObject.class)
                    .addStatement("inst.writeUBObject(obj)")
                    .addStatement("data.put($S, obj)", mField)
                    .endControlFlow()
                    .unindent()
                    .add("}\n")
                    .build());

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {

            ClassName proxyClassName = createDBObjName(mField);
            builder.addCode(CodeBlock.builder()
                            .beginControlFlow("if(obj.containsKey($S))", mField)
                            .addStatement("$T tmp = new $T()", proxyClassName, proxyClassName)
                            .addStatement("tmp.init(obj.get($S).asObject(), db)", mField)
                            .addStatement("super.$L(tmp)", createSetterName(mField))
                            .endControlFlow()
                            .build()
            );

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );

        }
    }

    private class EmbeddedDBObjectArrayField implements FieldMethodCodeGen {

        private final VariableElement mField;

        public EmbeddedDBObjectArrayField(VariableElement field) {
            mField = field;
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {

            builder.addStatement("data.put($S, $T.toUBArray($L()))",
                    mField, Utils.class, createGetterName(mField));
        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            TypeMirror fieldType = ((ArrayType) mField.asType()).getComponentType();
            Element typeElement = mEnv.getTypeUtils().asElement(fieldType);
            String simpleName = typeElement.getSimpleName().toString();
            ClassName proxyClassName = ClassName.get(MICRODB_PACKAGE, simpleName+"_pxy");
            ArrayTypeName proxyArrayClassName = ArrayTypeName.of(proxyClassName);


            builder.addCode(CodeBlock.builder()
                    .beginControlFlow("if(obj.containsKey($S))", mField)
                    .addStatement("$T input = obj.get($S).asArray()", UBArray.class, mField)
                    .addStatement("final int size = input.size()")
                    .addStatement("$T output = new $T[size]", proxyArrayClassName, proxyClassName)
                    .beginControlFlow("for(int i=0;i<size;i++)")
                    .addStatement("$T tmp = new $T()", proxyClassName, proxyClassName)
                    .addStatement("tmp.init(input.get(i).asObject(), db)")
                    .addStatement("output[i] = tmp")
                    .endControlFlow()
                    .addStatement("super.$L(output)", createSetterName(mField))
                    .endControlFlow()
                    .build());

        }

        @Override
        public void specializedMethods(TypeSpec.Builder builder) {
            final String setterName = createSetterName(mField);
            builder.addMethod(MethodSpec.methodBuilder(setterName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(TypeName.get(mField.asType()), "value")
                            .addStatement("super.$L(value)", setterName)
                            .addStatement("mDirty = true")
                            .build()
            );
        }
    }


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
        TypeElement element = toTypeElement(type);
        return element.asType();
    }

    private TypeElement toTypeElement(Class<?> type) {
        TypeElement element = mEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return element;
    }


    private boolean isLinkType(VariableElement field) {
        DeclaredType linkType = mEnv.getTypeUtils().getDeclaredType(toTypeElement(Link.class), mEnv.getTypeUtils().getWildcardType(toTypeMirror(DBObject.class), null));
        return mEnv.getTypeUtils().isAssignable(field.asType(), linkType);
    }

    private boolean isEmbeddedType(VariableElement field) {
        return mEnv.getTypeUtils().isSubtype(field.asType(), toTypeMirror(DBObject.class));
    }

    private boolean isStringType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), toTypeMirror(String.class));
    }

    private boolean isBoolType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN));
    }

    private boolean isByteType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.BYTE));
    }

    private boolean isShortType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.SHORT));
    }

    private boolean isIntType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.INT));
    }

    private boolean isLongType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.LONG));
    }

    private boolean isFloatType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.FLOAT));
    }

    private boolean isFloatArrayType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(),
                mEnv.getTypeUtils().getArrayType(mEnv.getTypeUtils().getPrimitiveType(TypeKind.FLOAT)));
    }

    private boolean isDoubleType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(), mEnv.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE));
    }

    private boolean isDoubleArrayType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(),
                mEnv.getTypeUtils().getArrayType(mEnv.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE)));
    }

    private boolean isEmbeddedDBObjectArrayType(VariableElement field) {
        return mEnv.getTypeUtils().isSubtype(field.asType(),
                mEnv.getTypeUtils().getArrayType(toTypeMirror(DBObject.class)));
    }

    public void generate() {
        final String proxyClassName = String.format("%s_pxy", mSimpleProxyClassName);
        final String fqClassName = String.format("%s.%s", MICRODB_PACKAGE, proxyClassName);
        try {

            ArrayList<FieldMethodCodeGen> fields = new ArrayList<FieldMethodCodeGen>();

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(proxyClassName)
                    .superclass(TypeName.get(mClassElement.asType()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for(Element member : mClassElement.getEnclosedElements()){

                if(member.getKind() == ElementKind.FIELD
                        && !member.getModifiers().contains(Modifier.STATIC)
                        && !member.getModifiers().contains(Modifier.TRANSIENT)) {


                    VariableElement field = (VariableElement)member;

                    final String fieldName = field.getSimpleName().toString();
                    if("id".equals(fieldName)) {
                        error("field with name 'id' is reserved");
                    } else if("type".equals(fieldName)) {
                        error("field with name 'type' is reserved");
                    } else {

                        if(isLinkType(field)){
                            if(!field.getModifiers().contains(Modifier.PUBLIC)) {
                                error("Link fields must be public");
                            } else {
                                fields.add(new LinkFieldGen(field));
                            }
                        } else {
                            if(!field.getModifiers().contains(Modifier.PRIVATE)) {
                                error(String.format("'%s' field must be private", field));
                            } else {
                                if(isEmbeddedType(field)) {
                                    fields.add(new EmbeddedDBObjectField(field));
                                } else if(isStringType(field)) {
                                    fields.add(new StringDBOBjectField(field));
                                } else if(isBoolType(field)){
                                    fields.add(new BoolDBObjectField(field));
                                } else if(isByteType(field)){
                                    fields.add(new ByteDBObjectField(field));
                                } else if (isShortType(field)) {
                                    fields.add(new ShortDBObjectField(field));
                                } else if (isIntType(field)) {
                                    fields.add(new IntDBOBjectField(field));
                                } else if (isLongType(field)) {
                                    fields.add(new LongDBOBjectField(field));
                                } else if (isFloatType(field)) {
                                    fields.add(new FloatDBOBjectField(field));
                                } else if (isDoubleType(field)) {
                                    fields.add(new DoubleDBOBjectField(field));
                                } else if(isFloatArrayType(field)){
                                    fields.add(new FloatArrayDBOBjectField(field));
                                } else if(isDoubleArrayType(field)){
                                    fields.add(new DoubleArrayDBOBjectField(field));
                                } else if(isEmbeddedDBObjectArrayType(field)){
                                    fields.add(new EmbeddedDBObjectArrayField(field));
                                } else {
                                    error(String.format("'%s' is not an acceptable type. Persistable objects need to extend DBObject.", field));
                                }
                            }
                        }


                    }
                }
            }

            classBuilder.addField(
                    FieldSpec.builder(UBString.class, "TYPE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$T.createString($S)", UBValueFactory.class, mClassElement.getSimpleName())
                            .build());


            classBuilder.addMethod(generateToUBValueMethod(fields));

            classBuilder.addMethod(generateInitMethod(fields));

            for(FieldMethodCodeGen fieldGen : fields) {
                fieldGen.specializedMethods(classBuilder);
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

    private MethodSpec generateToUBValueMethod(final List<FieldMethodCodeGen> fieldGens) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("writeUBObject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(UBObject.class, "data")
                .returns(TypeName.VOID);

        builder.addStatement("super.writeUBObject(data)");
        builder.addStatement("data.put($S, TYPE)", "type");
        for(FieldMethodCodeGen field : fieldGens) {
            field.serializeCode(builder);
        }

        return builder.build();
    }

    private MethodSpec generateInitMethod(final List<FieldMethodCodeGen> fieldGens) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(UBObject.class, "obj")
                .addParameter(MicroDB.class, "db")
                .returns(TypeName.VOID);

        builder.addStatement("super.init(obj, db)");

        for(FieldMethodCodeGen fieldGen : fieldGens) {
            fieldGen.deserializeCode(builder);
        }

        return builder.build();
    }



    private String createGetterName(VariableElement field) {
        final String fieldName = field.getSimpleName().toString();
        final String methodName = String.format("get%s%s", fieldName.substring(0, 1).toUpperCase(),
                fieldName.substring(1));
        return methodName;

    }

    public String createSetterName(VariableElement field) {
        final String fieldName = field.getSimpleName().toString();
        final String methodName = String.format("set%s%s", fieldName.substring(0, 1).toUpperCase(),
                fieldName.substring(1));
        return methodName;
    }

}
