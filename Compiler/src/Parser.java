
public class Parser {
	
	private Token lookAhead;
	private Scanner scanner;
	public Parser(Scanner scanner){
		this.scanner = scanner;
	}
	
	public int run(){
		while (!scanner.endOfFile()) {
			lookAhead = scanner.getToken();
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
	
	//David's Section---work in process, NOT DONE with my section
	private void program(){
		lookAhead.describe();
		switch(lookAhead.getIdentifier()){
		case "mp_program":
			programHeading();
			match(";");
			block();
			match(".");
			break;
		default:
			handleError();
		}
		
	}


	private void programHeading() {
		switch(lookAhead.getIdentifier()) {
		case "mp_program":
			match("program");
			identifier();
			break;
		default:
			handleError();
		}
		
	}
	
	//THIS METHOD IS WRONG....NEEDS TO HIT vDP() and pAFDP() and sP()...see alternative below
	private void block() {
		switch(lookAhead.getIdentifier()) {
		case "mp_var":
			variableDeclarationPart();
			break;
		case "mp_procedure":
			procedureAndFunctionDeclarationPart();
			break;
		case "mp_begin":
			statementPart();
			break;
		default:
			handleError();
		}
		
	}
	
//	private void block() {
//		switch(lookAhead.getIdentifier()) {
//		case "mp_var":
//			variableDeclarationPart();
//			match("var");
//			procedureAndFunctionDeclarationPart();
//			match("procedure");
//			statementPart();
//			match("begin");
//			break;
//		}
//	}

	private void variableDeclarationPart() {
		switch(lookAhead.getIdentifier()) {
		case "mp_var":
			match("var");
			variableDeclaration();
			match(";");
			break;
			//not sure about this recursion...need to be able to hit
			//variableDeclaration() multiple times...
		case "mp_identifier":
			variableDeclaration();
			match(";");
			break;
		default:
			handleError();
		}
		
	}
	
	private void procedureAndFunctionDeclarationPart() {
		switch(lookAhead.getIdentifier()) {
		case "mp_function":
			functionDeclaration(); //must be able to repeat this || procedureDelcaration()
			break;
		case "mp_procedure":
			procedureDeclaration();//must be able to repeat this || functionDelcaration()
			break;
		default:
			handleError();
		}
		
	}
		

	private void statementPart() {
		switch(lookAhead.getIdentifier()) {
		case "mp_begin":
			compoundStatement();
			break;
		default:
			handleError();
		}
		
	}
	private void variableDeclaration() {
		switch(lookAhead.getIdentifier()) {
		case "mp_identifier":
			identifierList();
			match(":");
			type();
			break;
		default:
			handleError();
		}
		
	}
	private void type() {
		switch(lookAhead.getIdentifier()) {
		case "mp_integer_lit":
			match("integer");
			break;
		case "mp_float_lit":
			match("float");
			break;
		default:
			handleError();
		}
		
	}
	private void procedureDeclaration() {
		switch(lookAhead.getIdentifier()) {
		case "mp_prodedure":
			procedureHeading();
			match(";");
			block();
			break;
		default:
			handleError();
		}
		
	}
		
	
	private void functionDeclaration() {
		switch(lookAhead.getIdentifier()) {
		case "mp_function":
			functionHeading();
			match(";");
			block();
			break;
		default:
			handleError();
		}
	}
	
	private void procedureHeading() {
		switch(lookAhead.getIdentifier()) {
		case "mp_prodecure":
			match("procedure");
			identifier();
			//NEED TO ADD OPTIONAL STUFF HERE???
			break;
		default:
			handleError();
		}
		
	}
	private void functionHeading() {
		switch(lookAhead.getIdentifier()) {
		case "mp_function":
			match("function");
			identifier();
			//NEED TO ADD OPTIONAL STUFF HERE???
			match(":");
			type();
			break;
		default:
			handleError();
		}
		
	}
	private void formalParameterList() {
		
		
	}
	private void formalParameterSection() {
		
		
	}
	//Clark's section
	private void valueParameterSection() {
		
		
	}
	private void variableParameterSection() {
		
		
	}
	private void compoundStatement() {
		
		
	}
	private void statementSequence() {
		
		
	}
	private void statement() {
		
		
	}
	private void simpleStatement() {
		
		
	}
	private void structuredStatement() {
		
		
	}
	private void conditionalStatement() {
		
		
	}
	private void repetitiveStatement() {
		
		
	}
	private void emptyStatement() {
		
		
	}
	private void readStatement() {
		
		
	}
	private void writeStatement() {
		
		
	}
	private void assignmentStatement() {
		
		
	}
	private void procedureStatement() {
		
		
	}
	private void ifStatement() {
		
		
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
	private void fuctionIdentifier() {
		
		
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
