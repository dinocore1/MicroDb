
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
    microdb::Statement* stmtval;
    microdb::Selector* selval;
    microdb::stmtList* stmtlistval;
    microdb::argList* arglist;
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TLSBRACE TRSBRACE TDOT
%token TPATHSTART TCOMMA
%token TASSIGN TEQUALS TLEQ TGEQ TGT TLT TNEQ
%token <ival> TINT
%token <sval> TID TSTRLITERAL

%type <stmtlistval> stmtlist
%type <stmtval> stmt ifstmt assign
%type <arglist> arglist
%type <selval> path var literal expr condition funCall


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
    | funCall { $$ = $1; }
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
    : TID TLPAREN arglist TRPAREN { $$ = new FunctionCall(*$1, *$3); delete $1; delete $3; }
    ;

arglist
    : arglist TCOMMA expr { $$ = $1; $$->push_back($3); }
    | expr { $$ = new argList(); $$->push_back($1); }
    | { $$ = new argList(); }
    ;

assign
    : TID TASSIGN expr { $$ = new Assign(*$1, $3); delete $1; }
    ;


var
    : TID { $$ = new VarSelector(*$1); delete $1; }
    ;

path
    : var { $$ = $1; }
    | path TDOT TID { $$ = new MemberSelector(*$3, $1); delete $3; }
    | path TLSBRACE expr TRSBRACE { $$ = new ArraySelector($3, $1); }
    ;

%%



void viewqueryerror(YYLTYPE* locp, microdb::ParserStruct* ctx, const char* err)
{
    ctx->mParseSuccess = false;
}

namespace microdb {

    bool ViewQuery::compile(const char* code) {

        ParserStruct parserStr;
        parserStr.mParseSuccess = true;

        viewquerylex_init(&parserStr.svt);

        YY_BUFFER_STATE buff = viewquery_scan_string(code, parserStr.svt);

        viewqueryparse(&parserStr);

        viewquery_delete_buffer(buff, parserStr.svt);

        viewquerylex_destroy(parserStr.svt);

        mStatements = parserStr.stmts;

        return parserStr.mParseSuccess;
    }
}
