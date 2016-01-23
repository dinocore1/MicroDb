package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;

public class SemPass2 extends MicroDBBaseVisitor<Void> {

    private final CompilerContext mContext;
    private String mCurrentPackage;

    public SemPass2(CompilerContext ctx) {
        mContext = ctx;
    }

    @Override
    public Void visitHeader(MicroDBParser.HeaderContext ctx) {
        mCurrentPackage = ctx.packageName().getText();

        return super.visitHeader(ctx);
    }

    @Override
    public Void visitObjType(MicroDBParser.ObjTypeContext ctx) {
        Nodes.ObjType objNode = (Nodes.ObjType) mContext.nodeMap.get(ctx);

        final int doti = objNode.mClassName.lastIndexOf('.');
        if(doti > 0) {
            objNode.mPackageName = objNode.mClassName.substring(0, doti);
            objNode.mSimpleName = objNode.mClassName.substring(doti+1);
        } else {
            objNode.mPackageName = mCurrentPackage;
            objNode.mSimpleName = objNode.mClassName;
        }

        return super.visitObjType(ctx);
    }
}
