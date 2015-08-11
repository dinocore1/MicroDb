
%pure-parser
%name-prefix="viewquery"
%locations
%parse-param { microdb::ParserStruct* ctx }
%lex-param { void* scanner }

%{
#include <string>

#include <microdb/value.h>
#include "viewquery.h"


%}

%union {
    std::string* sval;
    int ival;
    double fval;
    microdb::Statement* stmtval;
    microdb::Selector* selval;
    microdb::stmtList* stmtlistval;
    microdb::argList* arglist;
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TLSBRACE TRSBRACE TDOT
%token TPATHSTART TCOMMA TELSE
%token TASSIGN TEQUALS TLEQ TGEQ TGT TLT TNEQ
%token <ival> TINT
%token <fval> TFLOAT
%token <sval> TID TSTRLITERAL

%type <stmtlistval> stmtlist
%type <stmtval> stmt ifstmt assign block
%type <arglist> arglist
%type <selval> path var literal expr condition funCall

%right ELSE TELSE

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

block
    : TLBRACE stmtlist TRBRACE { $$ = new BlockStatement(*$2); delete $2; }
    ;

stmt
    : ifstmt
    | funCall { $$ = $1; }
    | assign
    | block
    ;

ifstmt
    : TIF TLPAREN expr TRPAREN stmt { $$ = new IfStatement($3, $5); } %prec ELSE
    | TIF TLPAREN expr TRPAREN stmt TELSE stmt { $$ = new IfStatement($3, $5, $7); }
    ;


condition
    : expr TEQUALS expr { $$ = new Condition($1, $3, microdb::Condition::Equals); }
    | expr TLEQ expr { $$ = new Condition($1, $3, microdb::Condition::LessThanOrEqual); }
    | expr TGEQ expr { $$ = new Condition($1, $3, microdb::Condition::GreaterOrEqual); }
    | expr TGT expr { $$ = new Condition($1, $3, microdb::Condition::GreaterThan); }
    | expr TLT expr { $$ = new Condition($1, $3, microdb::Condition::LessThan); }
    | expr TNEQ expr { $$ = new Condition($1, $3, microdb::Condition::NotEqual); }
    ;

expr
    : path
    | literal
    | funCall
    | condition
    | TLPAREN expr TRPAREN { $$ = $2; }
    ;

literal
    : TSTRLITERAL { $$ = new StrLiteralSelector(*$1); delete $1; }
    | TINT { $$ = new IntLiteralSelector($1); }
    | TFLOAT { $$ = new FloatLiteralSelector($1); }
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
