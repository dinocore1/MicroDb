

%option noyywrap
%option reentrant
%option prefix="viewquery"
%option bison-bridge
%option bison-locations
%option yylineno

%{

#include <string>

#include "viewquery.h"
#include "viewquery.yy.hpp"


#define SAVE_STRING yylval->sval = new std::string(yytext, yyleng)
#define SAVE_INT yylval->ival = atoi(yytext)

%}

%%

"if"                { return TIF; }
"("                 { return TLPAREN; }
")"                 { return TRPAREN; }
"{"                 { return TLBRACE; }
"}"                 { return TRBRACE; }
"."                 { return TDOT; }
"$"                 { return TPATHSTART; }
[0-9]+              { SAVE_INT; return TINT; }
[a-zA-Z0-9_]+       { SAVE_STRING; return TID; }
[ \n\t]             ;

%%