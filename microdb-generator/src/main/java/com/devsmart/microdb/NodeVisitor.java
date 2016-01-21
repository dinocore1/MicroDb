package com.devsmart.microdb;

import com.devsmart.microdb.ast.Nodes;


public class NodeVisitor extends MicroDBBaseVisitor<Nodes.Node> {

    @Override
    public Nodes.Node visitDbo(MicroDBParser.DboContext ctx) {

        String name = ctx.name.getText();
        String extend = ctx.extend != null ? ctx.extend.getText() : "DBObject";

        Nodes.DBONode retval = new Nodes.DBONode(name, extend);

        return retval;
    }


    @Override
    public Nodes.Node visitFile(MicroDBParser.FileContext ctx) {
        for(MicroDBParser.DboContext dbo : ctx.dbo()){
            visit(dbo);
        }
        return null;
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
        }
        return null;
    }

}
