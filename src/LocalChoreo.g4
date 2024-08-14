grammar LocalChoreo;

DOT : '.' ;
COMMA : ',' ;
COLON : ':' ;
LPAREN : '(' ;
RPAREN : ')' ;
LCURLY : '{' ;
RCURLY : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;

IDENT: [a-zA-Zπ][a-zA-Z0-9]* ;

WS : [ \r\n\t] + -> skip ;

start: k=knwl s+=stmt* EOF                          # Local
    ;

knwl: IDENT ':' ts+=term (',' ts+=term)*            # Knowledge
    ;

stmt: '0'                                                       #End
    | t=term '.'                                                #Statement
    | 'if' '(' t=term ')' 'then' s1=stmt 'else' s2=stmt         #Check
    | 'try' var=IDENT '=' t=term 'in' s1=stmt 'catch' s2=stmt   #Try
    ;

term: f=IDENT '(' as=args ')' 						# Function
    | x=IDENT 										# Variable
    | '[' m=term ']' k=term 						# MAC
    | '(' m=term ')' 								# TermParen
    ;

args: as+=term (',' as+=term)* 						# Arguments
	;
