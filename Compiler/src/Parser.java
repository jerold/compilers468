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

	/**
	 * Matches the left token to lookahead.
	 * If there is a match, the lookahead is moved to the next scanned token
	 * 
	 * @param s		The string to match to
	 * @param test	Whether or not this is a test (if so, suppress errors) (maybe we should keep it from getting next lookahead as well)
	 * @return		Whether or not there was a match
	 */
	private boolean match(String s, boolean test) {
		if (s.equals(lookAhead.getLexeme())) {
			lookAhead.describe();
			lookAhead = scanner.getToken();
			while (lookAhead == null){
				lookAhead = scanner.getToken();
			}
			return true;
		} else {
			if (!test) handleError(true, s);
			return false;
		}
	}
	
	private boolean match(String s) {
		return match(s,false);
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

	// David's Section
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
			match("(");
			formalParameterSection();  
			while(lookAhead.getIdentifier().equals(";")) {
				match(";");
				formalParameterSection();
			}
			match(")");
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
//is this the correct match on var???
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
			statement(); // recursive here
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
		while (lookAhead.getIdentifier().equals("mp_scolon")){
			match(";");
			statementSequence();
		}
	}

	private void statement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_scolon": // statementSequence -> statement
			match(";");
			break;
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
			break;
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
			//match(";");
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
				
				// This is JJ, should we be eating these parens here? Think actualParameter should do that
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
		switch (lookAhead.getIdentifier()) {
		case "mp_repeat": // repeatStatement -> "repeat", statementSequence, "until", booleanExpression
			match("repeat");
			statementSequence();
			match("until");
			booleanExpression();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void whileStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_while": // whileStatement -> "while", booleanExpression, "do" statement
			match("while");
			booleanExpression();
			match("do");
			statement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void forStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_for": // forStatement -> "for", controlVariable, ":=", initialValue, ("to"|"downto"), finalVariable, "do", statement
			match("for");
			controlVariable();
			match(":=");
			initialValue();
			if (lookAhead.getIdentifier().equals("mp_to")) {
				match("to");
			} else if (lookAhead.getIdentifier().equals("mp_downto")) {
				match("downto");
			} else {
				handleError(false, null);
			}
			finalValue();
			match("do");
			statement();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void controlVariable() {
		variableIdentifier();
	}

	private void initialValue() {
		ordinalExpression();
	}

	private void finalValue() {
		ordinalExpression();
	}

	private void expression() {
		// I should be switching on all Factor token id's and sign id's I think...
//		switch (lookAhead.getIdentifier()) {
//		case "mp_equal":
//			simpleExpression();
//			break;
//		default: // optional case statement proceed citizen...
//			break;
//		}
		switch (lookAhead.getIdentifier()) {
		case "mp_equal":
		case "mp_lthan":
		case "mp_gthan":
		case "mp_lequal":
		case "mp_gequal":
		case "mp_nequal":
			relationalOperator();
			simpleExpression();
			break;
		default: // optional case statement proceed citizen...
			break;
		}
	}

	private void simpleExpression() {
		// Not sure what mustaches mean, so I'll wait on this one.
	}

	private void term() {
		// Not sure what mustaches mean, so I'll wait on this one.
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
			// recurses!
		default:
			handleError(false, null);
		}

	}

	private void relationalOperator() {
		switch (lookAhead.getIdentifier()) {
		case "mp_equal":
			match("=");
			break;
		case "mp_lthan":
			match("<");
			break;
		case "mp_gthan":
			match(">");
			break;
		case "mp_lequal":
			match("<=");
			break;
		case "mp_gequal":
			match(">=");
			break;
		case "mp_nequal":
			match("<>");
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void addingOperator() {
		switch (lookAhead.getIdentifier()) {
		case "mp_plus":
			match("+");
			break;
		case "mp_minus":
			match("-");
			break;
		case "mp_or": // How is this an adding operator?
			match("or");
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void multiplyingOperator() {
		switch (lookAhead.getIdentifier()) {
		case "mp_times":
			match("*");
			break;
		case "mp_div":
			match("div");
			break;
		case "mp_mod":
			match("mod");
			break;
		case "mp_and":
			match("and");
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void functionDesegnator() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			functionIdentifier();
			if (lookAhead.getIdentifier().equals("mp_lparen")) {
				actualParameterList();
			}
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	private void variable() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
			variableIdentifier();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, null);
		}
	}

	// Logan's section
	private void actualParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				match("(");
				actualParameter();
				while (match(",",true)) {
					actualParameter();
				}
				match(")");
				break;
			default:
				handleError(false, null);
		}
	}

	private void actualParameter() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				expression();
				break;
			default:
				handleError(false, null);
		}
	}

	private void readParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				match("(");
				readParameter();
				while (match(",",true)) {
					readParameter();
				}
				match(")");
				break;
			default:
				handleError(false, null);
		}
	}

	private void readParameter() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				variable();
				break;
			default:
				handleError(false, null);
		}
	}

	private void writeParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				match("(");
				writeParameter();
				while (match(",",true)) {
					writeParameter();
				}
				match(")");
				break;
			default:
				return;
				//handleError(false, null);
		}
	}

	private void writeParameter() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
			case "mp_string_lit":
				expression();
				break;
			default:
				handleError(false, null);
		}
	}

	private void booleanExpression() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				ordinalExpression();
				break;
			default:
				handleError(false, null);
		}
	}

	private void ordinalExpression() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				expression();
				break;
			default:
				handleError(false, null);
		}
	}

	private void variableIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				identifier();
				break;
			default:
				handleError(false, null);
		}
	}

	private void procedureIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				identifier();
				break;
			default:
				handleError(false, null);
		}
	}

	private void functionIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				identifier();
				break;
			default:
				handleError(false, null);
		}
	}

	private void identifierList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				identifier();
				while (match(",",true)) {
					identifier();
				}
				break;
			default:
				handleError(false, null);
		}
	}

	private void identifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				match(lookAhead.getLexeme());
				break;
			default:
				handleError(false, null);
		}
	}

	private void unsignedInteger() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				digitSequence();
				break;
			default:
				handleError(false, null);
		}
	}

	private void sign() {
		switch (lookAhead.getIdentifier()) {
			case "mp_plus":
				match("+");
				break;
			case "mp_minus":
				match("-");
				break;
			default:
				handleError(false, null);
		}
	}

	/* TODO: We have to take a look at this one */
	private void under() {
		match("_");
	}

	private void digitSequence() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				for (int i=0; i<lookAhead.getIdentifier().length(); i++)
					digit();
				break;
			default:
				handleError(false, null);
		}
	}

	private void letter() {
		boolean found = false;
		String letters[] = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
						   "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		for (int i=0; i<letters.length; i++) {
			found = match(letters[i],true);
			if (found)
				break;
		}
		if (!found)
			handleError(true,"digit");
	}

	private void digit() {
		boolean found = false;
		String digits[] = {"0","1","2","3","4","5","6","7","8","9"};
		for (int i=0; i<digits.length; i++) {
			found = match(digits[i],true);
			if (found)
				break;
		}
		if (!found)
			handleError(true,"digit");
	}

}
