

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
#define SAVE_FLOAT yylval->fval = atof(yytext);

%}

%%

"if"                { return TIF; }
"("                 { return TLPAREN; }
")"                 { return TRPAREN; }
"{"                 { return TLBRACE; }
"}"                 { return TRBRACE; }
"["                 { return TLSBRACE; }
"]"                 { return TRSBRACE; }
","                 { return TCOMMA; }
"."                 { return TDOT; }
"$"                 { return TPATHSTART; }
"=="                { return TEQUALS; }
"<="                { return TLEQ; }
">="                { return TGEQ; }
"!="                { return TNEQ; }
"<"                 { return TLT; }
">"                 { return TGT; }
"="                 { return TASSIGN; }
\"(\\.|[^"])*\"     { yylval->sval = new std::string(&yytext[1], yyleng-2); return TSTRLITERAL; }
[0-9]+\.[0-9]+      { SAVE_FLOAT; return TFLOAT; }
[0-9]+              { SAVE_INT; return TINT; }
[a-zA-Z0-9_]+       { SAVE_STRING; return TID; }
[ \n\t]             ;

%%
