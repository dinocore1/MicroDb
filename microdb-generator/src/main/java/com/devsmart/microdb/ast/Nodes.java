package com.devsmart.microdb.ast;


import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

public class Nodes {


    public static abstract class Node {

    }

    public static class FileNode extends Node {
        public List<DBONode> dboList = new ArrayList<DBONode>();
        public String packageName;
    }

    public static class DBONode extends Node {
        public final String name;
        public final String extend;
        public final ArrayList<FieldNode> fields = new ArrayList<FieldNode>();
        public final ArrayList<String> codeblocks = new ArrayList<String>();
        public final ArrayList<DBONode> dboThatExtend = new ArrayList<DBONode>();

        public DBONode(String name, String extend) {
            this.name = name;
            this.extend = extend;
        }
    }

    public static class FieldNode extends Node {
        public final TypeNode type;
        public final String name;

        public FieldNode(TypeNode type, String name) {
            this.type = type;
            this.name = name;
        }

    }

    public static class TypeNode extends Node {
        public static final int BOOL = 0;
        public static final int CHAR = 1;
        public static final int INT = 2;
        public static final int FLOAT = 3;
        public static final int STRING = 4;
        public static final int DBO = 5;

        public final int type;
        public ArrayList<String> annotations = new ArrayList<String>();
        public boolean isArray;

        private TypeNode(int type) {
            this.type = type;
        }

    }

    public static TypeNode createBool() {
        return new TypeNode(TypeNode.BOOL);
    }

    public static TypeNode createChar() {
        return new TypeNode(TypeNode.CHAR);
    }

    public static TypeNode createString() {
        return new TypeNode(TypeNode.STRING);
    }

    public static class NumberType extends TypeNode {

        public final int size;

        private NumberType(int type, int size) {
            super(type);
            this.size = size;
        }

        public static NumberType createInt8() {
            return new NumberType(INT, 8);
        }

        public static NumberType createInt16() {
            return new NumberType(INT, 16);
        }

        public static NumberType createInt32() {
            return new NumberType(INT, 32);
        }

        public static NumberType createInt64() {
            return new NumberType(INT, 64);
        }

        public static NumberType createFloat32() {
            return new NumberType(FLOAT, 32);
        }

        public static NumberType createFloat64() {
            return new NumberType(FLOAT, 64);
        }
    }

    public static class ObjType extends TypeNode {
        public String mClassName;
        public String mPackageName;
        public String mSimpleName;


        public ObjType(String className) {
            super(TypeNode.DBO);
            this.mClassName = className;
        }

        public ClassName getClassName() {
            return ClassName.get(mPackageName, mSimpleName);
        }

    }
}

