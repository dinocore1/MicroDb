
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
    microdb::FunctionCall* funcall;
    microdb::Statement* stmtval;
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TDOT TPATHSTART TCOMMA
%token TEQUALS
%token <ival> TINT
%token <sval> TID

%type <stmtval> stmt assign
%type <arglist> arglist
%type <funcall> funCall
%type <selval> path var


%start query

%{

#include "viewquery.lex.h"

void viewqueryerror(YYLTYPE* locp, microdb::ParserStruct* ctx, const char* err);

using namespace microdb;

#define scanner ctx->svt


%}


%%


query
    : path { ctx->selector = $1; }
    ;


stmtlist
    : stmtlist stmt
    |
    ;

stmt
    : condition
    | funCall
    | assign
    ;

condition
    : TIF TLPAREN path TRPAREN TLBRACE stmtlist TRBRACE
    ;

funCall
    : TID TLPAREN arglist TRPAREN { $$ = new FunctionCall(*$1, *$3); delete $1; delete $3; }
    ;

arglist
    : path { $$ = new arglist(); $$->push_back($1); }
    | arglist TCOMMA path { $$ = $1; $$->push_back($3); }
    | { $$ = new arglist(); }
    ;

var
    : TID { $$ = new VarSelector(*$1); delete $1; }
    ;

assign
    : TID TEQUALS path { $$ = new Assign($1, $3); delete $1; }
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
        
        YY_BUFFER_STATE buff = viewquery_scan_string(code, parserStr.svt);
        
        viewqueryparse(&parserStr);
        
        viewquery_delete_buffer(buff, parserStr.svt);
        
        viewquerylex_destroy(parserStr.svt);
        
        mSelector = parserStr.selector;
        
        return true;
    }
}
