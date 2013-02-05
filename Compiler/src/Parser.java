
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
	
	//David's Section
	public void program(){
		lookAhead.describe();
		switch(lookAhead.getIdentifier()){
		case "program":
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
	private void factor() {
		
		
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
