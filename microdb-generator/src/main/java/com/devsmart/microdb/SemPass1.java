package com.devsmart.microdb;

import com.devsmart.microdb.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;


public class SemPass1 extends MicroDBBaseVisitor<Nodes.Node> {

    private final CompilerContext mContext;
    private Nodes.FileNode mCurrentFile;
    private Nodes.DBONode mCurrentDBO;

    public SemPass1(CompilerContext ctx) {
        mContext = ctx;
    }

    private void error(String msg, Token location) {
        mContext.error(msg, location);
    }

    private void warn(String msg, Token location) {
        mContext.warn(msg, location);
    }

    private Nodes.Node putMap(ParserRuleContext ctx, Nodes.Node node) {
        mContext.nodeMap.put(ctx, node);
        return node;
    }

    @Override
    public Nodes.Node visitDbo(MicroDBParser.DboContext ctx) {
        String name = ctx.name.getText();
        String extend = ctx.extend != null ? ctx.extend.getText() : null;

        mCurrentDBO = new Nodes.DBONode(name, extend);
        visit(ctx.exprlist());
        mContext.allDBO.add(mCurrentDBO);
        return putMap(ctx, mCurrentDBO);
    }

    @Override
    public Nodes.Node visitField(MicroDBParser.FieldContext ctx) {

        Nodes.TypeNode type = (Nodes.TypeNode) visit(ctx.type());
        String name = ctx.name.getText();
        if("id".equals(name)) {
            error("field with name 'id' is reserved", ctx.name);
        }

        Nodes.FieldNode fieldNode = new Nodes.FieldNode(type, name);
        mCurrentDBO.fields.add(fieldNode);

        return fieldNode;
    }

    @Override
    public Nodes.Node visitExprlist(MicroDBParser.ExprlistContext ctx) {
        TerminalNode codeblock = ctx.CODEBLOCK();
        if(codeblock != null) {
            String codeblockStr = codeblock.getText();
            codeblockStr = codeblockStr.substring(7);
            codeblockStr = codeblockStr.substring(0, codeblockStr.length()-7);
            mCurrentDBO.codeblocks.add(codeblockStr);
        }
        return super.visitExprlist(ctx);
    }

    @Override
    public Nodes.Node visitFile(MicroDBParser.FileContext ctx) {
        mCurrentFile = new Nodes.FileNode();
        visit(ctx.header());

        for(MicroDBParser.DboContext dbo : ctx.dbo()){
            mCurrentFile.dboList.add((Nodes.DBONode) visit(dbo));
        }
        return mCurrentFile;
    }

    @Override
    public Nodes.Node visitHeader(MicroDBParser.HeaderContext ctx) {
        super.visitHeader(ctx);
        MicroDBParser.PackageNameContext packagename = ctx.packageName();
        if(packagename != null) {
            mCurrentFile.packageName = packagename.getText();
        } else {
            warn("missing package name", ctx.start);
        }

        return null;
    }

    @Override
    public Nodes.Node visitType(MicroDBParser.TypeContext ctx) {
        Nodes.TypeNode retval = (Nodes.TypeNode) visit(ctx.type1());
        for(Token anno : ctx.anno){
            retval.annotations.add(anno.getText());
        }
        retval.isArray = ctx.ARRAYTYPE() != null;
        return retval;
    }

    @Override
    public Nodes.Node visitPrimitiveType(MicroDBParser.PrimitiveTypeContext ctx) {
        switch (ctx.t.getType()) {

            case MicroDBLexer.BOOL:
                return Nodes.createBool();

            case MicroDBLexer.BYTE:
                return Nodes.NumberType.createInt8();

            case MicroDBLexer.CHAR:
                return Nodes.createChar();

            case MicroDBLexer.SHORT:
                return Nodes.NumberType.createInt16();

            case MicroDBLexer.INT:
                return Nodes.NumberType.createInt32();

            case MicroDBLexer.LONG:
                return Nodes.NumberType.createInt64();

            case MicroDBLexer.FLOAT:
                return Nodes.NumberType.createFloat32();

            case MicroDBLexer.DOUBLE:
                return Nodes.NumberType.createFloat64();

            case MicroDBLexer.STRING:
                return Nodes.createString();
        }
        return null;
    }

    @Override
    public Nodes.Node visitObjType(MicroDBParser.ObjTypeContext ctx) {
        return putMap(ctx, new Nodes.ObjType(ctx.getText()));
    }
}
