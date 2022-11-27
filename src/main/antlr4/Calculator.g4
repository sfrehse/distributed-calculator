grammar Calculator;

prog: expr EOF;

expr: left=expr operator=(MULTIPLY|DIVIDE) right=expr
    | left=expr operator=(PLUS|MINUS) right=expr
    | INT
    | '(' expr ')'
    | fnName=FN_NAME '(' (arguments+=expr (',' arguments+=expr)* ')' )
    ;

INT: [0-9]+;
FN_NAME: [a-zA-Z][a-zA-Z]*;
NEWLINE: [\r\n]+ -> skip;



MULTIPLY: '*';
DIVIDE: '/';
PLUS: '+';
MINUS: '-';