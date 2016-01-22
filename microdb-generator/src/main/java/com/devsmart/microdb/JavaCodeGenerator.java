package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValueFactory;
import com.google.common.base.Charsets;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;

import java.io.*;
import java.util.ArrayList;

public class JavaCodeGenerator {

    private static final String MICRODB_PACKAGE = "com.devsmart.microdb";

    private final Nodes.DBONode mDBO;
    private final Nodes.FileNode mFileCtx;

    public JavaCodeGenerator(Nodes.DBONode dbo, Nodes.FileNode ctx) {
        mDBO = dbo;
        mFileCtx = ctx;
    }

    public JavaFile createJavaFile() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(mDBO.name);
        if(mDBO.extend != null) {
            classBuilder.superclass(ClassName.get(mFileCtx.packageName, mDBO.extend));
        } else {
            classBuilder.superclass(ClassName.get(MICRODB_PACKAGE, "DBObject"));
        }

        classBuilder.addField(
                FieldSpec.builder(UBString.class, "TYPE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.createString($S)", UBValueFactory.class, mDBO.name)
                        .build());

        ArrayList<FieldCodeGen> fieldCodeGane = new ArrayList<FieldCodeGen>();

        for(Nodes.FieldNode field : mDBO.fields) {
            fieldCodeGane.add(new PrimitiveFieldCodeGen(field));
        }

        for(FieldCodeGen codeGen : fieldCodeGane) {
            classBuilder.addField(codeGen.genField());
            classBuilder.addMethod(codeGen.genGetterMethod());
            classBuilder.addMethod(codeGen.genSetterMethod());
        }

        JavaFile proxySourceFile = JavaFile.builder(mFileCtx.packageName, classBuilder.build())
                .skipJavaLangImports(true)
                .build();

        return proxySourceFile;
    }

    static TypeName getTypeName(Nodes.TypeNode type) {

        switch(type.type) {
            case Nodes.TypeNode.BOOL:
                return TypeName.BOOLEAN;

            case Nodes.TypeNode.STRING:
                return ClassName.get(String.class);

            case Nodes.TypeNode.INT: {
                Nodes.NumberType numberType = (Nodes.NumberType) type;
                switch(numberType.size) {
                    case 8:
                        return TypeName.BYTE;
                    case 16:
                        return TypeName.SHORT;
                    case 32:
                        return TypeName.INT;
                    case 64:
                        return TypeName.LONG;
                }
            }

            case Nodes.TypeNode.FLOAT: {
                Nodes.NumberType numberType = (Nodes.NumberType) type;
                switch (numberType.size) {
                    case 32:
                        return TypeName.FLOAT;

                    case 64:
                        return TypeName.DOUBLE;
                }
            }


        }

        return null;

    }

    private abstract class FieldCodeGen {
        Nodes.FieldNode mField;

        FieldCodeGen(Nodes.FieldNode field) {
            mField = field;
        }

        String createGetterName() {
            final String methodName = String.format("get%s%s", mField.name.substring(0, 1).toUpperCase(),
                    mField.name.substring(1));
            return methodName;
        }

        String createSetterName() {
            final String methodName = String.format("set%s%s", mField.name.substring(0, 1).toUpperCase(),
                    mField.name.substring(1));
            return methodName;
        }

        FieldSpec genField() {
            FieldSpec retval = FieldSpec.builder(getTypeName(mField.type), mField.name, Modifier.PRIVATE)
                    .build();
            return retval;
        }
        abstract void genReadFromUBObject();
        abstract void genWriteToUBObject();
        MethodSpec genGetterMethod() {
            final String getterName = createGetterName();
            return MethodSpec.methodBuilder(getterName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getTypeName(mField.type))
                    .addStatement("return $L", mField.name)
                    .build();
        }
        MethodSpec genSetterMethod() {
            final String setterName = createSetterName();
            return MethodSpec.methodBuilder(setterName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addParameter(getTypeName(mField.type), "value")
                    .addStatement("this.$L = value", mField.name)
                    .addStatement("setDirty()")
                    .build();
        }

    }

    class PrimitiveFieldCodeGen extends FieldCodeGen {

        PrimitiveFieldCodeGen(Nodes.FieldNode field) {
            super(field);
        }

        @Override
        void genReadFromUBObject() {

        }

        @Override
        void genWriteToUBObject() {

        }


    }



}
