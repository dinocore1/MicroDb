grammar MicroDB;

@header {

}

file : header dbo+
    ;

header
    : PACKAGE packageName SEMI
    ;

packageName
    : ID (DOT ID)*
    ;

dbo : DBO name=ID LPAREN field+ RPAREN
    | DBO name=ID 'extends' extend=ID LPAREN field+ RPAREN
    ;

field
    : type name=ID SEMI
    ;

type
    : (ANNO anno+=ID)* type1 ARRAYTYPE?
    ;

type1
    : t=BYTE        #primitiveType
    | t=BOOL        #primitiveType
    | t=CHAR        #primitiveType
    | t=SHORT       #primitiveType
    | t=INT         #primitiveType
    | t=LONG        #primitiveType
    | t=FLOAT       #primitiveType
    | t=DOUBLE      #primitiveType
    | t=STRING      #primitiveType
    | packageName   #objType
    ;


DBO : 'dbo' ;
ARRAYTYPE : '[]' ;
LPAREN : '{' ;
RPAREN : '}' ;
SEMI : ';' ;
ANNO : '@' ;
BYTE : 'byte' ;
BOOL : 'bool' ;
CHAR : 'char' ;
SHORT : 'short' ;
INT : 'int' ;
LONG : 'long';
FLOAT : 'float' ;
DOUBLE : 'double' ;
STRING : 'string' ;
PACKAGE : 'package' ;
DOT : '.' ;
ID : [a-zA-Z0-9_]+ ;
WS : [ \t\r\n]+ -> skip ;