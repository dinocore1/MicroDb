package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

public class CompilerContext {

    public Map<ParserRuleContext, Nodes.Node> nodeMap = new HashMap<ParserRuleContext, Nodes.Node>();
}
