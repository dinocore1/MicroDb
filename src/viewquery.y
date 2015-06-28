
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
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TDOT TPATHSTART
%token <ival> TINT
%token <sval> TID

%type <selval> path


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
    : stmtlist condition
    |
    ;

condition
    : TIF TLPAREN path TRPAREN TLBRACE stmtlist TRBRACE
    ;

path
    : TPATHSTART { $$ = new PathStart(); }
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
