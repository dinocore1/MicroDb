grammar MicroDB;

@header {

}

file : dbo+
    ;

dbo : DBO ID LPAREN field+ RPAREN
    ;

field
    : type ID SEMI
    ;

type
    : annotation primitiveType
    | annotation primitiveType ARRAYTYPE
    ;

primitiveType
    : 'byte'
    | 'short'
    | 'int'
    | 'int32'
    | 'int64'
    | 'long'
    | 'float'
    | 'float32'
    | 'float64'
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
ID : [a-zA-Z0-9_]+ ;
WS : [ \t\r\n]+ -> skip ;