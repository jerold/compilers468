
public class Parser {
	
	private Token lookAhead;
	private Scanner scanner;
	public Parser(Scanner scanner){
		this.scanner = scanner;
	}
	
	public int run(){
		while (!scanner.endOfFile()) {
			lookAhead = scanner.getToken();
			lookAhead.describe();
			if(lookAhead.getLexeme().equals("program")){
				program();
			}
		}
		lookAhead = new Token("mp_eof", scanner.getFP().getLineNumber(),0,"eof");
		lookAhead.describe();
		return 0;
	}
	

	private void handleError() {
		// TODO Auto-generated method stub
		
	}
	
	private void match(String s){
		if(s.equals(lookAhead.getLexeme())){
			lookAhead = scanner.getToken();
		}
		else {
			handleError();
		}
	}
	
	//David's Section
	private void program(){
		lookAhead.describe();
		switch(lookAhead.getIdentifier()){
		case "mp_program":
			programHeading();
			break;
		default:
			handleError();
		}
		
	}


	private void programHeading() {
		// TODO Auto-generated method stub
		
	}
	
	private void block() {
		
		
	}

	private void variableDeclarationPart() {
		
		
	}
	
	private void procedureAndFunctionDeclarationPart() {
		
		
	}
	private void statementPart() {
		
		
	}
	private void variableDeclaration() {
		
		
	}
	private void type() {
		
		
	}
	private void procedureDeclaration() {
		
		
	}
	private void functionDeclaration() {
		
		
	}
	private void procedureHeading() {
		
		
	}
	private void functionHeading() {
		
		
	}
	private void formalParameterList() {
		
		
	}
	private void formalParameterSection() {
		
		
	}
	//Clark's section
	private void valueParameterSection() {
		switch(lookAhead.getIdentifier()){
		case "mp_identifier":
			identifierList();
			match(":");
			type();
			break;	
		default:
			handleError();
		}
	}
	private void variableParameterSection() {
		switch(lookAhead.getIdentifier()){
		case "mp_var":
			match("var");
			variableParameterSection();
			match(":");
			type();
			break;		
		default:
			handleError();
		}
		
	}
	private void compoundStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_begin":
			match("begin");
			statementSequence();
			match("end");
			break;
		default:
			handleError();
		}
		
	}
	private void statementSequence() {
		switch(lookAhead.getIdentifier()){
		case "mp_begin":
			statement();
			break;
		case "mp_for":
			statement();
			break;
		case "mp_if":
			statement();
			break;
		case "mp_read":
			statement();
			break;
		case "mp_repeat":
			statement();
			break;
		case "mp_while":
			statement();
			break;
		case "mp_write":
			statement();
			break;
		case "mp_identifier":
			statement();
			break;
		case "mp_scolon":
			match(";");
			statement();
			break;
		default:
			handleError();
		}
		
	}
	private void statement() {
		switch(lookAhead.getIdentifier()){
		case "mp_begin":
			compoundStatement();
			break;
		case "mp_for":
			compoundStatement();
			break;
		case "mp_if":
			compoundStatement();
			break;
		case "mp_read":
			simpleStatement();
			break;
		case "mp_repeat":
			compoundStatement();
			break;
		case "mp_while":
			compoundStatement();
			break;
		case "mp_write":
			simpleStatement();
			break;
		case "mp_identifier":
			simpleStatement();
			break;
		default:
			handleError();
		}
		
	}
	private void simpleStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_read":
			readStatement();
			break;
		case "mp_write":
			writeStatement();
			break;
			//seems ambiguous here???
		case "mp_identifier":
			assignmentStatement();
			procedureStatement();
			break;
		case "":
			emptyStatement();
		default:
			handleError();
		}
		
	}
	private void structuredStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_begin":
			compoundStatement();
			break;
		case "mp_for":
			compoundStatement();
			break;
		case "mp_if":
			conditionalStatement();
			break;
		case "mp_repeat":
			compoundStatement();
			break;
		case "mp_while":
			compoundStatement();
			break;		
		default:
			handleError();
		}
	}
	private void conditionalStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_if":
			ifStatement();
			break;
		default:
			handleError();
		}
		
	}
	private void repetitiveStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_while":
			whileStatement();
			break;
		case "mp_repeat":
			repeatStatement();
			break;
		case "mp_for":
			forStatement();
			break;
		default:
			handleError();
		}
		
	}
	private void emptyStatement() {
		switch(lookAhead.getIdentifier()){
		case "":
			match("");
			break;
		default:
			handleError();
		}
		
	}
	private void readStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_read":
			match("read");
			readParameterList();
			break;
		default:
			handleError();
		}
		
	}
	private void writeStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_write":
			match("write");
			writeParameterList();
			break;
		default:
			handleError();
		}
		
	}
	//this seems ambiguous also??
	private void assignmentStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_identifier":
			//on the next two lines
			variable();
			functionIdentifier();
			match(":=");
			expression();
			break;
		default:
			handleError();
		}
		
	}
	private void procedureStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_identifier":
			procedureIdentifier();
			break;
		case "mp_lparen":
			match("(");
			actualParameterList();
			match(")");
			break;
		default:
			handleError();
		}
		
	}
	private void ifStatement() {
		switch(lookAhead.getIdentifier()){
		case "mp_if":
			match("if");
			booleanExpression();
			match("then");
			statement();
			break;
		case "mp_else":
			match("else");
			statement();
		default:
			handleError();
		}
		
	}
	
	//Jerold's section
	private void repeatStatement() {
		
		
	}
	private void whileStatement() {
		
		
	}
	private void forStatement() {
		
		
	}
	private void controlVariable() {
		
		
	}
	private void initialValue() {
		
		
	}
	private void finalValue() {
		
		
	}
	private void expression() {
		
		
	}
	private void simpleExpression() {
		
		
	}
	private void term() {
		
		
	}
	//this is not LL1 for some reason??!!
	private void factor() {
		switch(lookAhead.getIdentifier()){
		case "mp_integer_lit":	
			unsignedInteger();
			break;
		case "mp_identifier":
			variable();
			break;
			//something must go here later the EBNF is WRONG!
//		case "mp_identifier":
//			variable();
//			break;
		case "mp_lparen":
			match("(");
			expression();
			match(")");
			break;
		case "mp_not":	
		default:
			handleError();
		}
		
	}
	private void relationalOperator() {
			
	}
	private void addingOperator() {
		
		
	}
	private void multiplyingOperator() {
		
		
	}
	private void functionDesegnator() {
		
		
	}
	private void variable() {
		
		
	}
	//Logan's section
	private void actualParameterList() {
		
		
	}
	private void actualParameter() {
		
		
	}
	private void readParameterList() {
		
		
	}
	private void readParameter() {
		
		
	}
	private void writeParameterList() {
		
		
	}
	private void writeParameter() {
		
		
	}
	private void booleanExpression() {
		
		
	}
	private void ordinalExpression() {
		
		
	}
	private void variableIdentifier() {
		
		
	}
	private void procedureIdentifier() {
		
		
	}
	private void functionIdentifier() {
		
		
	}
	private void identifierList() {
		
		
	}
	private void identifier() {
		
		
	}
	private void unsignedInteger() {
		
		
	}
	private void signedInteger() {
		
		
	}
	private void under() {
		
		
	}
	private void digitSequence() {
		
		
	}
	private void letter() {
		
		
	}
	private void digit() {
		
		
	}

}
