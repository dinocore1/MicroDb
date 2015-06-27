
%pure-parser
%name-prefix="viewquery"
%locations
%parse-param { microdb::Selector** sltr }
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
void viewqueryerror(YYLTYPE* locp, microdb::ViewQuery* ctx, const char* err);

using namespace microdb;

#define scanner ctx->scanner;


%}


%%


query
    : path { *yyextra = $1; }
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

namespace microdb {
    
    bool ViewQuery::compile(const char* code) {
        yyscan_t scan;
        viewquerylex_init(&scan);
        
        YY_BUFFER_STATE buff = viewquery_scan_string(code, scan);
        
        yyparse(scan);
        
        viewquery_delete_buffer(buff, scan);
        
        viewquery_lex_destroy(scan);
        
        return true;
    }
}

void viewqueryerror(YYLTYPE* locp, microdb::ViewQuery* ctx, const char* err)
{
    
}