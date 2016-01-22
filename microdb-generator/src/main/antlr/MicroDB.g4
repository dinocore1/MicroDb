grammar MicroDB;

@header {

}

file : pack dbo+
    ;

pack
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
    : (ANNO anno+=ID)* primitiveType ARRAYTYPE?
    ;

primitiveType
    : t=BYTE
    | t=BOOL
    | t=CHAR
    | t=SHORT
    | t=INT
    | t=LONG
    | t=FLOAT
    | t=DOUBLE
    | t=STRING
    | t=ID
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