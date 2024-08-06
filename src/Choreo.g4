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

term: f=IDENT '(' as=args ')' 						# Function
    | x=IDENT 										# Variable
    | '[' m=term ']' k=term 						# MAC
    | '(' m=term ')' 								# TermParen
    ;

args: as+=term (',' as+=term)* 						# Arguments
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
