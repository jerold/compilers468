public class Parser {

	private Token lookAhead;
	private Scanner scanner;

	public Parser(Scanner scanner) {
		this.scanner = scanner;
	}

	public int run() {
		// while (!scanner.endOfFile()) {
		lookAhead = scanner.getToken();
		int i = 0;
		//lookAhead would be null if comment opens the program
		while (lookAhead == null) {
			// lookAhead.describe();
			// if(lookAhead.getLexeme().equals("program")){
			// program();
			// }
			lookAhead = scanner.getToken();	
		}
		i = start();
		// }

		// lookAhead.describe();
		return i;
	}

	private void handleError(boolean matchError, String s) {
		if (matchError) {
			String errorToken = lookAhead.getIdentifier().substring(3);
			System.out.println("Expected \"" + s + "\" but found " + errorToken
					+ " on line " + lookAhead.getLineNum() + " in column "
					+ lookAhead.getColNum() + ".");
			//get the next token and keep trying
			//at some point we will have to determine the appropriate method to 
			//call after getting the next token
			lookAhead = scanner.getToken(); 
		} else {
			if (lookAhead == null) {
				// this happens with a valid comment. we just want the next
				// token and continue
				lookAhead = scanner.getToken();
			} else {
				System.out.println("the other kind of error to be handled here");
			}
		}

	}

	private void match(String s) {
		if (s.equals(lookAhead.getLexeme())) {
			lookAhead.describe();
			lookAhead = scanner.getToken();
			while (lookAhead == null){
				lookAhead = scanner.getToken();
			}
		} else {
			handleError(true, s);
		}
	}

	private int start() {
		switch (lookAhead.getIdentifier()) {
		case "mp_program":
			program();
//			int lineNum = lookAhead.getLineNum() + 1;
//			lookAhead = new Token("mp_eof", lineNum, 0, "eof");
			match("eof");
			return 1;
		default:
			return 0;
		}
	}

	// David's Section---work in process, NOT DONE with my section
	private void program() {
		// lookAhead.describe();
		switch (lookAhead.getIdentifier()) {
		case "mp_program":
			programHeading();
			match(";");
			block();
			match(".");
			break;
		default:
			handleError(false, null);
		}

	}

	private void programHeading() {
		switch (lookAhead.getIdentifier()) {
		case "mp_program":
			match("program");
			identifier();
			break;
		default:
			handleError(false, null);
		}

	}


	// I changed this David. We'll see if it works.
	private void block() {
		variableDeclarationPart();
		procedureAndFunctionDeclarationPart();
		statementPart();
	}


	// I think this works now David. check it out and see what you think
	private void variableDeclarationPart() {
		switch (lookAhead.getIdentifier()) {
		case "mp_var":
			match("var");
			variableDeclaration();
			match(";");
			// break;
			// not sure about this recursion...need to be able to hit
			// variableDeclaration() multiple times...
			// case "mp_identifier":
			if (lookAhead.getIdentifier().equals("mp_identifier")) {
				variableDeclarationPart2();
				// match(";");
				// variableDeclarationPart();
			}
			break;
		default:
			handleError(false, null);
		}
	}

	private void variableDeclarationPart2() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			variableDeclaration();
			match(";");
			variableDeclarationPart2();
		default:
			return;
		}
	}

	private void procedureAndFunctionDeclarationPart() {
		switch (lookAhead.getIdentifier()) {
		// must be able to repeat this || procedureDelcaration()....not sure if this will work
		case "mp_function":
			functionDeclaration(); 
			match(";");	
			procedureAndFunctionDeclarationPart();
			break;
		case "mp_procedure":
			procedureDeclaration();
			match(";");	
			procedureAndFunctionDeclarationPart();
			break;
		default:
			return;
		}

	}

	private void statementPart() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin":
			compoundStatement();
			break;
		default:
			handleError(false, null);
		}

	}

	private void variableDeclaration() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			identifierList();
			match(":");
			type();
			break;
		default:
			handleError(false, null);
		}

	}

	private void type() {
		switch (lookAhead.getIdentifier()) {
		case "mp_integer":
			match("integer");
			break;
		case "mp_float":
			match("float");
			break;
		default:
			handleError(false, null);
		}

	}

	private void procedureDeclaration() {
		switch (lookAhead.getIdentifier()) {
		case "mp_prodedure":
			procedureHeading();
			match(";");
			block();
			break;
		default:
			handleError(false, null);
		}

	}

	private void functionDeclaration() {
		switch (lookAhead.getIdentifier()) {
		case "mp_function":
			functionHeading();
			match(";");
			block();
			break;
		default:
			handleError(false, null);
		}
	}

	private void procedureHeading() {
		switch (lookAhead.getIdentifier()) {
		case "mp_prodecure":
			match("procedure");
			identifier();
			// This if should work assuming identifier is
			// moving lookAhead.getIdentifier() forward
			if (lookAhead.getIdentifier().equals("(")) {
				formalParameterList();
			}
			break;
		default:
			handleError(false, null);
		}

	}

	private void functionHeading() {
		switch (lookAhead.getIdentifier()) {
		case "mp_function":
			match("function");
			identifier();
			// This if should work assuming identifier is
			// moving lookAhead.getIdentifier() forward
			if (lookAhead.getIdentifier().equals("(")) {
				formalParameterList();
			}
			match(":");
			type();
			break;
		default:
			handleError(false, null);
		}

	}

	private void formalParameterList() {
		switch (lookAhead.getIdentifier()) {
		case "mp_lparen":
			match("lparen");
			formalParameterSection();  
			//need to handle { ";" FormalParameterSection } here
			//maybe like this...?
			while(lookAhead.getIdentifier().equals(";")) {
				match(";");
				formalParameterSection();
			}
			match("rparen");
			break;
		default:
			return;
		}
	}

	private void formalParameterSection() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			valueParameterSection();  //check this one 
			break;
		case "mp_var":
			match("var");
			variableParameterSection();
			break;
		default:
			return;
		}
	}

	// Clark's section
	private void valueParameterSection() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier": // valueParameterSection -> IdentifierList, ":",
								// Type
			identifierList();
			match(":");
			type();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void variableParameterSection() {
		switch (lookAhead.getIdentifier()) {
		case "mp_var": // variableParameterSection -> "var" ,identifierList,
						// ":", Type
			match("var");
			identifierList();
			match(":");
			type();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void compoundStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin": // compoundStatement -> "begin", statementSequence,
							// "end"
			match("begin");
			statementSequence();
			match("end");
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void statementSequence() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin": // statementSequence -> statement
			statement();
			break;
		case "mp_for": // statementSequence -> statement
			statement();
			break;
		case "mp_if": // statementSequence -> statement
			statement();
			break;
		case "mp_read": // statementSequence -> statement
			statement();
			break;
		case "mp_repeat": // statementSequence -> statement
			statement();
			break;
		case "mp_while": // statementSequence -> statement
			statement();
			break;
		case "mp_write": // statementSequence -> statement
			statement();
			break;
		case "mp_identifier": // statementSequence -> statement
			statement();
			break;
		case "mp_scolon": // statementSequence -> statement
			match(";");
			statementSequence(); // recursive here
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void statement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin": // statement -> compoundStatement
			compoundStatement();
			break;
		case "mp_for": // statement -> compoundStatement
			compoundStatement();
			break;
		case "mp_if": // statement -> compoundStatement
			compoundStatement();
			break;
		case "mp_read": // statement -> simpleStatement
			simpleStatement();
			break;
		case "mp_repeat": // statement -> compoundStatement
			compoundStatement();
			break;
		case "mp_while": // statement -> compoundStatement
			compoundStatement();
			break;
		case "mp_write": // statement -> simpleStatement
			simpleStatement();
			break;
		case "mp_identifier": // statement -> simpleStatement
			simpleStatement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void simpleStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_read": // simpleStatement -> readStatement
			readStatement();
			break;
		case "mp_write": // simpleStatement -> writeStatement
			writeStatement();
			break;
		// seems ambiguous here???
		case "mp_identifier": // simpleStatement -> assignmentStatement and
								// procedureStatement if lookAhead is identifier
			assignmentStatement();
			procedureStatement();
			break;
		case ";": // simpleStatement -> emptyStatement-- not really sure what
					// epsilon is yet?!
			emptyStatement();
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void structuredStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin": // structuredStatement -> compoundStatement
			compoundStatement();
			break;
		case "mp_for": // structuredStatement -> compoundStatement
			compoundStatement();
			break;
		case "mp_if": // structuredStatement -> conditionalStatement
			conditionalStatement();
			break;
		case "mp_repeat": // structuredStatement -> compoundStatement
			compoundStatement();
			break;
		case "mp_while": // structuredStatement -> compoundStatement
			compoundStatement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void conditionalStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_if": // conditionalStatement -> ifStatement
			ifStatement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void repetitiveStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_while": // repetitiveStatement -> whileStatement
			whileStatement();
			break;
		case "mp_repeat": // repetitiveStatement -> repeatStatement
			repeatStatement();
			break;
		case "mp_for": // repetitiveStatement -> forStatement
			forStatement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void emptyStatement() {
		switch (lookAhead.getIdentifier()) {
		case ";": // not really sure what to do here.
			match(";"); // I'm assuming ;;;; would be empty statements
			break;
		default:
			handleError(false, null);
		}

	}

	private void readStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_read": // readStatement -> "read", readParameterList
			match("read");
			readParameterList();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void writeStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_write": // writeStatement -> "write", writeParameterList
			match("write");
			writeParameterList();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	// this seems ambiguous also??
	private void assignmentStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier": // assignmentStatement ->
								// (Variable|FunctionIdentifier), ":=",
								// expression
			// on the next two lines
			variable();
			functionIdentifier();
			match(":=");
			expression();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void procedureStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier": // procedureStatement -> procedureIdentifier,
								// [actualParameterList]
			procedureIdentifier();
			// break;
			// case "mp_lparen":
			// match("(");
			// actualParameterList();
			// match(")");
			// break;
			if (lookAhead.getIdentifier().equals("mp_lparen")) {
				match("(");
				actualParameterList();
				match(")");
			}
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	private void ifStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_if": // ifStatement -> "if", booleanExpression, "then",
						// statement, ["else", statement]
			match("if");
			booleanExpression();
			match("then");
			statement();
			// break;
			// case "mp_else":
			// match("else");
			// statement();
			if (lookAhead.getIdentifier().equals("mp_else")) {
				match("else");
				statement();
			}
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}

	}

	// Jerold's section
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

	// this is not LL1 for some reason??!!
	private void factor() {
		switch (lookAhead.getIdentifier()) {
		case "mp_integer_lit":
			unsignedInteger();
			break;
		case "mp_identifier":
			variable();
			break;
		// something must go here later the EBNF is WRONG!
		// case "mp_identifier":
		// variable();
		// break;
		case "mp_lparen":
			match("(");
			expression();
			match(")");
			break;
		case "mp_not":
		default:
			handleError(false, null);
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

	// Logan's section
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
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			identifier();
			if (lookAhead.getIdentifier().equals("mp_identifier")) {
				match(",");
				identifierList();
			}
		}
	}

	private void identifier() {
		match(lookAhead.getLexeme());
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
