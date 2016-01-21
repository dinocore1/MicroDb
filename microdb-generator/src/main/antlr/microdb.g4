grammar MicroDB;

@header {

}



file : dbo+
    ;

dbo : DBO name=ID LPAREN field+ RPAREN
    | DBO name=ID 'extends' extend=ID LPAREN field+ RPAREN
    ;

field
    : type ID SEMI
    ;

type
    : annotation primitiveType
    | annotation primitiveType ARRAYTYPE
    ;

primitiveType
    : t=BYTE
    | t=BOOL
    | t=SHORT
    | t=INT
    | t=LONG
    | t=FLOAT
    | t=DOUBLE
    | t=STRING
    | ID
    ;

annotation
    : ANNO ID
    |
    ;

DBO : 'dbo' ;
ARRAYTYPE : '[]' ;
LPAREN : '{' ;
RPAREN : '}' ;
SEMI : ';' ;
ANNO : '@' ;
BYTE : 'byte' ;
BOOL : 'bool' ;
SHORT : 'short' ;
INT : 'int' ;
LONG : 'long';
FLOAT : 'float' ;
DOUBLE : 'double' ;
STRING : 'string' ;
ID : [a-zA-Z0-9_]+ ;
WS : [ \t\r\n]+ -> skip ;