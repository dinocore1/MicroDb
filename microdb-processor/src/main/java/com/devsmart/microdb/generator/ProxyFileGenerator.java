package com.devsmart.microdb.generator;


import com.devsmart.microdb.*;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ProxyFileGenerator {

    public static final String MICRODB_PACKAGE = "com.devsmart.microdb";

    private abstract class FieldMethodCodeGen {

        final VariableElement mField;

        public FieldMethodCodeGen(VariableElement field) {
            mField = field;
        }

        abstract void serializeCode(MethodSpec.Builder builder);
        abstract void deserializeCode(MethodSpec.Builder builder);
        void specializedMethods(TypeSpec.Builder builder) {
            builder.addMethod(generateSetterMethod(mField));
        }

        MethodSpec generateGetterMethod(VariableElement field) {
            final String getterName = createGetterName(field);
            return MethodSpec.methodBuilder(getterName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(field.asType()))
                    .addStatement("return $L", field)
                    .build();
        }

        MethodSpec generateSetterMethod(VariableElement field) {
            final String setterName = createSetterName(field);
            return MethodSpec.methodBuilder(setterName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(field.asType()), "value")
                    .addAnnotation(Override.class)
                    .addStatement("super.$L(value)", setterName)
                    .addStatement("mDirty = true")
                    .build();
        }

    }

    private class DatumField extends FieldMethodCodeGen {

        public DatumField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addCode(CodeBlock.builder()
                    .add("{\n")
                    .indent()

                    .addStatement("$T datum = $L()", TypeName.get(mField.asType()), createGetterName(mField))
                    .beginControlFlow("if (datum == null)")
                    .addStatement("data.put($S, $T.createNull())", mField, UBValueFactory.class)
                    .nextControlFlow("else")
                    .addStatement("data.put($S, datum.toUBValue())", mField)
                    .endControlFlow()

                    .unindent()
                    .add("}\n")
                    .build());

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.addCode(CodeBlock.builder()
                    .beginControlFlow("if (obj.containsKey($S))", mField)
                    .addStatement("$T datum", TypeName.get(mField.asType()))
                    .addStatement("$T value = obj.get($S)", UBValue.class, mField)
                    .beginControlFlow("if (value.isNull())")
                    .addStatement("datum = null")
                    .nextControlFlow("else")
                    .addStatement("datum = new $T()", TypeName.get(mField.asType()))
                    .addStatement("datum.fromUBValue(value)")
                    .endControlFlow()
                    .addStatement("super.$L(datum)", createSetterName(mField))
                    .endControlFlow()
                    .build());

        }
    }

    private class GenericUBValueField extends FieldMethodCodeGen {

        public GenericUBValueField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createValueOrNull($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {

            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("$T value = obj.get($S)", UBValue.class, mField);
            builder.beginControlFlow("if(value.isNull())");
            builder.addStatement("super.$L(null)", createSetterName(mField));
            builder.nextControlFlow("else");
            builder.addStatement("super.$L(($T) value)",
                    createSetterName(mField), mField);
            builder.endControlFlow();
            builder.endControlFlow();

        }

    }

    private class StringDBOBjectField extends FieldMethodCodeGen {

        public StringDBOBjectField(VariableElement field) {
            super(field);
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
    }

    private class BoolDBObjectField extends FieldMethodCodeGen {

        public BoolDBObjectField(VariableElement field) {
            super(field);
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

    }

    private class ByteDBObjectField extends FieldMethodCodeGen {

        public ByteDBObjectField(VariableElement field) {
            super(field);
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

    }

    private class ShortDBObjectField extends FieldMethodCodeGen {

        public ShortDBObjectField(VariableElement field) {
            super(field);
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

    }

    private class IntDBOBjectField extends FieldMethodCodeGen {

        public IntDBOBjectField(VariableElement field) {
            super(field);
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
    }

    private class IntArrayDBOBjectField extends FieldMethodCodeGen {

        public IntArrayDBOBjectField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createArrayOrNull($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("$T value = obj.get($S)", UBValue.class, mField);
            builder.beginControlFlow("if(value.isNull())");
            builder.addStatement("super.$L(null)", createSetterName(mField));
            builder.nextControlFlow("else");
            builder.addStatement("super.$L(value.asInt32Array())",
                    createSetterName(mField));
            builder.endControlFlow();
            builder.endControlFlow();

        }

    }

    private class LongDBOBjectField extends FieldMethodCodeGen {

        public LongDBOBjectField(VariableElement field) {
            super(field);
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

    }

    private class FloatDBOBjectField extends FieldMethodCodeGen {

        public FloatDBOBjectField(VariableElement field) {
            super(field);
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

    }

    private class FloatArrayDBOBjectField extends FieldMethodCodeGen {

        public FloatArrayDBOBjectField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createArrayOrNull($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("$T value = obj.get($S)", UBValue.class, mField);
            builder.beginControlFlow("if(value.isNull())");
            builder.addStatement("super.$L(null)", createSetterName(mField));
            builder.nextControlFlow("else");
            builder.addStatement("super.$L(value.asFloat32Array())",
                    createSetterName(mField));
            builder.endControlFlow();
            builder.endControlFlow();

        }

    }

    private class DoubleDBOBjectField extends FieldMethodCodeGen {

        public DoubleDBOBjectField(VariableElement field) {
            super(field);
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
    }

    private class DoubleArrayDBOBjectField extends FieldMethodCodeGen {

        public DoubleArrayDBOBjectField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.addStatement("data.put($S, $T.createArrayOrNull($L()))",
                    mField, UBValueFactory.class, createGetterName(mField));

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if(obj.containsKey($S))", mField);
            builder.addStatement("$T value = obj.get($S)", UBValue.class, mField);
            builder.beginControlFlow("if(value.isNull())");
            builder.addStatement("super.$L(null)", createSetterName(mField));
            builder.nextControlFlow("else");
            builder.addStatement("super.$L(value.asFloat64Array())",
                    createSetterName(mField));
            builder.endControlFlow();
            builder.endControlFlow();

        }
    }

    private class LinkFieldGen extends FieldMethodCodeGen {

        public LinkFieldGen(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {
            builder.beginControlFlow("if($L == null)", mField)
                    .addStatement("data.put($S, $T.createNull())", mField, UBValueFactory.class)
                    .nextControlFlow("else")
                    .addStatement("data.put($S, $L.getId())", mField, mField)
                    .endControlFlow();

        }

        @Override
        public void deserializeCode(MethodSpec.Builder builder) {
            TypeMirror genericType = ((DeclaredType) mField.asType()).getTypeArguments().get(0);
            builder.addStatement("$L = new $T(obj.get($S), this, $T.class)", mField, mField.asType(), mField, createDBObjName(mEnv.getTypeUtils().asElement(genericType)));
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

    private class EmbeddedDBObjectField extends FieldMethodCodeGen {

        public EmbeddedDBObjectField(VariableElement field) {
            super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {

            builder.addCode(CodeBlock.builder()
                    .add("{\n")
                    .indent()

                    .addStatement("$T inst = $N()", TypeName.get(mField.asType()), createGetterName(mField))
                    .beginControlFlow("if (inst == null)")
                    .addStatement("data.put($S, $T.createNull())", mField, UBValueFactory.class)
                    .nextControlFlow("else")
                    .addStatement("$T obj = $T.createObject()", UBObject.class, UBValueFactory.class)
                    .addStatement("inst.writeToUBObject(obj)")
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
                            .addStatement("$T value = obj.get($S)", UBValue.class, mField)
                            .beginControlFlow("if (value.isNull())")
                            .addStatement("super.$L(null)", createSetterName(mField))
                            .nextControlFlow("else")
                            .addStatement("$T tmp = new $T()", proxyClassName, proxyClassName)
                            .addStatement("tmp.init(null, getDB())")
                            .addStatement("tmp.readFromUBObject(value.asObject())")
                            .addStatement("super.$L(tmp)", createSetterName(mField))
                            .endControlFlow()
                            .endControlFlow()
                            .build()
            );

        }
    }

    private class EmbeddedDBObjectArrayField extends FieldMethodCodeGen {

        public EmbeddedDBObjectArrayField(VariableElement field) {
           super(field);
        }

        @Override
        public void serializeCode(MethodSpec.Builder builder) {

            builder.addStatement("data.put($S, $T.createArrayOrNull($L()))",
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
                    .addStatement("$T value = obj.get($S)", UBValue.class, mField)
                    .beginControlFlow("if(value.isNull())")
                    .addStatement("super.$L(null)", createSetterName(mField))
                    .nextControlFlow("else")
                    .addStatement("$T input = value.asArray()", UBArray.class)
                    .addStatement("final int size = input.size()")
                    .addStatement("$T output = new $T[size]", proxyArrayClassName, proxyClassName)
                    .beginControlFlow("for(int i=0;i<size;i++)")
                    .addStatement("$T tmp = new $T()", proxyClassName, proxyClassName)
                    .addStatement("tmp.init(null, getDB())")
                    .addStatement("tmp.readFromUBObject(input.get(i).asObject())")
                    .addStatement("output[i] = tmp")
                    .endControlFlow()
                    .addStatement("super.$L(output)", createSetterName(mField))
                    .endControlFlow()
                    .endControlFlow()
                    .build());

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

        if(!mEnv.getTypeUtils().isAssignable(mClassElement.asType(), toTypeMirror(DBObject.class))) {
            error("MicroDB DBObj must be assignable to " + DBObject.class.getSimpleName(), mClassElement);
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

    private boolean isDatumType(VariableElement field) {
        return mEnv.getTypeUtils().isAssignable(field.asType(), toTypeMirror(Datum.class));
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

    private boolean isIntArrayType(VariableElement field) {
        return mEnv.getTypeUtils().isSameType(field.asType(),
                mEnv.getTypeUtils().getArrayType(mEnv.getTypeUtils().getPrimitiveType(TypeKind.INT)));
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

    private boolean isUBValueType(VariableElement field) {
        return mEnv.getTypeUtils().isSubtype(field.asType(), toTypeMirror(UBValue.class));
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
                                if(isDatumType(field)) {
                                    fields.add(new DatumField(field));
                                } else if(isEmbeddedType(field)) {
                                    fields.add(new EmbeddedDBObjectField(field));
                                } else if(isStringType(field)) {
                                    fields.add(new StringDBOBjectField(field));
                                } else if (isBoolType(field)){
                                    fields.add(new BoolDBObjectField(field));
                                } else if (isByteType(field)){
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
                                } else if(isIntArrayType(field)){
                                    fields.add(new IntArrayDBOBjectField(field));
                                } else if(isFloatArrayType(field)){
                                    fields.add(new FloatArrayDBOBjectField(field));
                                } else if(isDoubleArrayType(field)){
                                    fields.add(new DoubleArrayDBOBjectField(field));
                                } else if(isEmbeddedDBObjectArrayType(field)){
                                    fields.add(new EmbeddedDBObjectArrayField(field));
                                } else if(isUBValueType(field)){
                                    fields.add(new GenericUBValueField(field));
                                } else {
                                    error(String.format("'%s' is not an acceptable type. Persistable objects need to extend DBObject.", field));
                                }
                            }
                        }


                    }
                }
            }

            classBuilder.addField(
                    FieldSpec.builder(UBString.class, "TYPE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$T.createString($S)", UBValueFactory.class, mClassElement.getSimpleName())
                            .build());


            classBuilder.addMethod(generateToUBValueMethod(fields));

            classBuilder.addMethod(generateFromUBValueMethod(fields));

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

    private boolean hasGetterMethod(TypeElement classElement, VariableElement field) {
        final String getterName = createGetterName(field);

        for (Element member : classElement.getEnclosedElements()) {
            if(member.getKind() == ElementKind.METHOD
                    && member.getModifiers().contains(Modifier.PUBLIC)
                    && !member.getModifiers().contains(Modifier.STATIC)){


                ExecutableElement methodElement = (ExecutableElement)member;
                if(!getterName.equals(methodElement.getSimpleName().toString())){
                    continue;
                }
                TypeMirror returnType = methodElement.getReturnType();
                if(!mEnv.getTypeUtils().isAssignable(returnType, field.asType())){
                    continue;
                }

                List<? extends VariableElement> params = methodElement.getParameters();
                if(params.size() == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasSetterMethod(TypeElement classElement, VariableElement field) {
        final String setterName = createSetterName(field);

        for (Element member : classElement.getEnclosedElements()) {
            if(member.getKind() == ElementKind.METHOD
                    && member.getModifiers().contains(Modifier.PUBLIC)
                    && !member.getModifiers().contains(Modifier.STATIC)){

                ExecutableElement methodElement = (ExecutableElement)member;
                if(!setterName.equals(methodElement.getSimpleName().toString())){
                    continue;
                }

                TypeMirror returnType = methodElement.getReturnType();
                if(returnType.getKind() != TypeKind.VOID){
                    continue;
                }

                List<? extends VariableElement> params = methodElement.getParameters();
                if(params.size() == 1 &&
                        mEnv.getTypeUtils().isAssignable(params.get(0).asType(), field.asType())) {
                    return true;
                }
            }
        }

        return false;
    }

    private MethodSpec generateToUBValueMethod(final List<FieldMethodCodeGen> fieldGens) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("writeToUBObject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(UBObject.class, "data")
                .returns(TypeName.VOID);

        builder.addStatement("super.writeToUBObject(data)");
        builder.addStatement("data.put($S, TYPE)", "type");
        for(FieldMethodCodeGen field : fieldGens) {
            field.serializeCode(builder);
        }

        return builder.build();
    }

    private MethodSpec generateFromUBValueMethod(final List<FieldMethodCodeGen> fieldGens) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("readFromUBObject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(UBObject.class, "obj")
                .returns(TypeName.VOID);

        builder.addStatement("super.readFromUBObject(obj)");

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
