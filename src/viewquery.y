
%pure-parser
%name-prefix="viewquery_"
%locations
%parse-param { microdb::ViewQuery* ctx }
%lex-param { void* scanner }

%{
#include <string>
#include "viewqueryparser.h"


%}

%union {
    std::string* sval;
    int ival;
}

%token TIF TLPAREN TRPAREN TLBRACE TRBRACE TDOT
%token <ival> TINT
%token <sval> TID


%start query

%{
int viewquery_lex(YYSTYPE* lvalp, YYLTYPE* llocp, void* scanner);

void viewquery_error(YYLTYPE* locp, microdb::ViewQuery* ctx, const char* err);

#define scanner ctx->scanner

%}


%%


query
    :
    ;

stmtlist
    : stmtlist condition
    |
    ;

condition
    : TIF TLPAREN expr TRPAREN TLBRACE stmtlist TRBRACE
    ;

expr
    : expr TDOT TID
    | TID
    ;

%%

void viewquery_error(YYLTYPE* locp, microdb::ViewQuery* ctx, const char* err)
{
    
}