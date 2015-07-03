
%pure-parser
%name-prefix="viewquery"
%locations
%parse-param { microdb::ParserStruct* ctx }
%lex-param { void* scanner }

%{
#include <string>

#include "viewquery.h"


%}

%union {
    std::string* sval;
    int ival;
    microdb::Selector* selval;
    microdb::argList* arglist;
    microdb::Statement* stmtval;
    microdb::stmtList* stmtlistval;
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TDOT TPATHSTART TCOMMA
%token TASSIGN TEQUALS TLEQ TGEQ TGT TLT TNEQ
%token <ival> TINT
%token <sval> TID TSTRLITERAL

%type <stmtlistval> stmtlist
%type <stmtval> stmt ifstmt assign funCall
%type <arglist> arglist
%type <selval> path var literal expr condition


%start query

%{

#include "viewquery.lex.h"

void viewqueryerror(YYLTYPE* locp, microdb::ParserStruct* ctx, const char* err);

using namespace microdb;

#define scanner ctx->svt


%}


%%


query
    : stmtlist { ctx->stmts = *$1; delete $1; }
    ;


stmtlist
    : stmtlist stmt { $$ = $1; $$->push_back($2); }
    | { $$ = new stmtList(); }
    ;

stmt
    : ifstmt
    | funCall
    | assign
    ;

ifstmt
    : TIF TLPAREN condition TRPAREN TLBRACE stmtlist TRBRACE { $$ = new IfStatement($3, *$6); delete $6; }
    ;


condition
    : expr TEQUALS expr { $$ = new Condition($1, $3, microdb::Condition::Equals); }
    | expr TLEQ expr
    | expr TGEQ expr
    | expr TGT expr
    | expr TLT expr
    | expr TNEQ expr
    ;

expr
    : path
    | literal
    | funCall
    ;

literal
    : TSTRLITERAL { $$ = new StrLiteralSelector(*$1); delete $1; }
    | TINT { $$ = new IntLiteralSelector($1); }
    ;

funCall
    : TID TLPAREN arglist TRPAREN { $$ = new FunctionCall(*$1, *$3, ctx->mEnv); delete $1; delete $3; }
    ;

arglist
    : path { $$ = new argList(); $$->push_back($1); }
    | arglist TCOMMA path { $$ = $1; $$->push_back($3); }
    | { $$ = new argList(); }
    ;

assign
    : TID TASSIGN expr { $$ = new Assign(*$1, $3, ctx->mEnv); delete $1; }
    ;


var
    : TID { $$ = new VarSelector(*$1, ctx->mEnv); delete $1; }
    ;

path
    : var { $$ = $1; }
    | path TDOT TID { $$ = new MemberSelector(*$3, $1); delete $3; }
    ;

%%



void viewqueryerror(YYLTYPE* locp, microdb::ParserStruct* ctx, const char* err)
{
    
}

namespace microdb {
    
    bool ViewQuery::compile(const char* code) {
        
        ParserStruct parserStr;
        
        viewquerylex_init(&parserStr.svt);
        parserStr.mEnv = mEnv;
        
        YY_BUFFER_STATE buff = viewquery_scan_string(code, parserStr.svt);
        
        viewqueryparse(&parserStr);
        
        viewquery_delete_buffer(buff, parserStr.svt);
        
        viewquerylex_destroy(parserStr.svt);
        
        mStatements = parserStr.stmts;
        
        return true;
    }
}
