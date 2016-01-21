package com.devsmart.microdb;

import com.devsmart.microdb.ast.Nodes;
import org.antlr.v4.runtime.Token;


public class NodeVisitor extends MicroDBBaseVisitor<Nodes.Node> {

    @Override
    public Nodes.Node visitDbo(MicroDBParser.DboContext ctx) {

        String name = ctx.name.getText();
        String extend = ctx.extend != null ? ctx.extend.getText() : "DBObject";

        Nodes.DBONode retval = new Nodes.DBONode(name, extend);
        for(MicroDBParser.FieldContext field : ctx.field()) {
            retval.fields.add((Nodes.FieldNode) visit(field));
        }

        return retval;
    }

    @Override
    public Nodes.Node visitField(MicroDBParser.FieldContext ctx) {

        Nodes.TypeNode type = (Nodes.TypeNode) visit(ctx.type());
        String name = ctx.name.getText();

        return null;
    }

    @Override
    public Nodes.Node visitFile(MicroDBParser.FileContext ctx) {
        for(MicroDBParser.DboContext dbo : ctx.dbo()){
            visit(dbo);
        }
        return null;
    }

    @Override
    public Nodes.Node visitType(MicroDBParser.TypeContext ctx) {
        Nodes.TypeNode retval = (Nodes.TypeNode) visit(ctx.primitiveType());
        for(Token anno : ctx.anno){
            retval.annotations.add(anno.getText());
        }
        retval.isArray = ctx.ARRAYTYPE() != null;
        return retval;

    }

    @Override
    public Nodes.Node visitPrimitiveType(MicroDBParser.PrimitiveTypeContext ctx) {
        switch (ctx.t.getType()) {
            case MicroDBLexer.BYTE:
                return Nodes.NumberType.createInt8();

            case MicroDBLexer.BOOL:
                return Nodes.createBool();

            case MicroDBLexer.SHORT:
                return Nodes.NumberType.createInt16();

            case MicroDBLexer.INT:
                return Nodes.NumberType.createFloat32();

            case MicroDBLexer.FLOAT:
                return Nodes.NumberType.createFloat32();

            case MicroDBLexer.DOUBLE:
                return Nodes.NumberType.createFloat64();

            case MicroDBLexer.STRING:
                return Nodes.createString();

            case MicroDBLexer.ID:
                return new Nodes.ObjType(ctx.t.getText());
        }
        return null;
    }

}
