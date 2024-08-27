grammar Choreo;

DOT : '.' ;
COMMA : ',' ;
COLON : ':' ;
LPAREN : '(' ;
RPAREN : ')' ;
LCURLY : '{' ;
RCURLY : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;

IDENT: [a-zA-Z][a-zA-Z0-9]* ;

WS : [ \r\n\t] + -> skip ;

start: ks+=knwl c=choreo EOF
    ;

knwl: a=IDENT ':' ts+=term (',' ts+=term)* '.' # Knowledge
    ;

term: f=IDENT '(' as+=term (',' as+=term)* ')' 		# Function
    | x=IDENT 										# Variable
    | '[' m=term ']' k=term 						# MAC
    | '(' m=term ')' 								# TermParen
    ;

choreo
    : '0'											# Empty
    | a=IDENT '->' b=IDENT ':' (l=IDENT)? ch=choice	# Message
    | a=IDENT ':' 'new' vars+=IDENT (',' vars+=IDENT)* '.' c=choreo		# Definition
	| '(' c=choreo ')'								# ChoreoParen
    ;

cont: t=term ('.' c=choreo)? 						# Continuation
	;

choice
	: co=cont ('+' ch=choice)?						# Choices
    | '(' co=cont ')' ('+' ch=choice)?				# ChoicesParen
    ;