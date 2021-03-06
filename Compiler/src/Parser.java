import java.util.ArrayList;
import java.util.ListIterator;

public class Parser {

	private Token lookAhead;
	private Scanner scanner;
	private boolean parseError = false;
	private Table symbolTable;
	private ArrayList<String> retValues;
	private String type;
	private Compiler compiler;
	private Symbol passSymbol;
	private SR sr;
	
	/**
	 * Constructor for the parser
	 * 
	 * @param scanner	A scanner object for the language
	 * @param compiler	A compiler object for the language
	 */
	public Parser(Scanner scanner, Compiler compiler) {
		this.scanner = scanner;
		this.compiler = compiler;
		retValues = null;
	}

	/**
	 * Starts parsing and semantic analysis given the program scanned in by the scanner.
	 * 
	 * @return	code indicating success or failure
	 */
	public int run() {
		
		// reset the symbol table and get the first token
		Table.clear();
		lookAhead = scanner.getToken();
		
		// skip to first valid lookahead (start comment returns null)
		while (lookAhead == null) {
			lookAhead = scanner.getToken();
		}
		
		// call root of CFG
		int i = start();
		
		// print errors or write file if successful
		if(i != 1) {
			System.out.println("File Did Not Parse successfully!");
		} else if (!compiler.checkOK()) {
			System.out.println("File Did Not Compile successfully...");
		} else {
			compiler.writeFile();
		}
		
		// return error code
		return i;
	}

	
	/**
	 * Error handler used during parse.
	 * 
	 * @param matchError	Flag indicating whether or not the error is of type match
	 * @param s				The string to append to the error
	 */
	private void handleError(boolean matchError, String s) {
		compiler.turnOff();
		if (matchError) {
			parseError = true;
			String errorToken = lookAhead.getLexeme();
			System.out.println("Expected \"" + s + "\" but found \"" + errorToken
					+ "\" on line " + lookAhead.getLineNum() + " in column "
					+ lookAhead.getColNum() + ".");
			lookAhead = scanner.getToken();
			while (lookAhead == null){
				lookAhead = scanner.getToken();
			}
		} else {
			if (lookAhead == null) {
				lookAhead = scanner.getToken();
			} else {
				parseError = true;
				System.out.println("Expected syntax of type \"" + s
						+ "\" on line " + lookAhead.getLineNum()
						+ " in column " + lookAhead.getColNum() + ".");
				System.out.println("  - Found "+lookAhead.getIdentifier());
			}
		}

	}
	
	/**
	 * Error handler specifically for undefined variables.
	 */
	private void handleErrorUndefined() {
		compiler.turnOff();
		System.out.println("Error: Unedefined variable \""+
							lookAhead.getLexeme()+"\" on line "+
							lookAhead.getLineNum()+" in column "+
							lookAhead.getColNum()+".");
	}
	
	/**
	 * General error handler used as a catch-all.
	 * 
	 * @param description	The text to append to error and line number
	 */
	private void handleErrorGeneral(String description) {
		compiler.turnOff();
		System.out.println("Error on line "+lookAhead.getLineNum()+
							" in column "+lookAhead.getColNum()+
							": "+description);
	}
	
	/**
	 * General warning used as a catch-all.
	 * 
	 * @param description	The text to append to warning and line number
	 */
	private void handleWarningGeneral(String description) {
		System.out.println("Warning` on line "+lookAhead.getLineNum()+
							" in column "+lookAhead.getColNum()+
							": "+description);
	}
	
	/**
	 * Error used for invalid variable names.
	 * 
	 * @param var	Name of the variable with invalid name
	 */
	private void invalidVariableName(String var) {
		compiler.turnOff();
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.print("Error on line "+lookAhead.getLineNum()+
						" in column "+lookAhead.getColNum()+":");
		System.out.println("   Variable " + var + " has an invalid name.");
		System.out.println("----------------");
	}
	
	/**
	 * Error used for undeclared variables.
	 * 
	 * @param var	The name of the variable that was not declared
	 */
	private void undeclaredVariableError(String var) {
		compiler.turnOff();
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.print("Error on line "+lookAhead.getLineNum()+
						" in column "+lookAhead.getColNum()+":");
		System.out.println("   Variable " + var + 
							" has not been declared in this scope.");
		System.out.println("----------------");
	}

	/**
	 * Matches the lexeme if lookahead matches, otherwise throws an error.
	 * 
	 * @param s	The lexeme that should be matched
	 */
	private void match(String s) {
		if (s.equals(lookAhead.getLexeme())) {
			lookAhead = scanner.getToken();
			while (lookAhead == null) {
				lookAhead = scanner.getToken();
			}
		} else {
			handleError(true, s);
		}
	}

	/**
	 * >> Program EOF
	 * 
	 * @return code indicating success or failure
	 */
	private int start() {
		switch (lookAhead.getIdentifier()) {
			case "mp_program":
				compiler.move("#0", "D0");
				compiler.move("#0", "D1");
				compiler.move("#0", "D2");
				compiler.move("#0", "D3");
				compiler.move("#0", "D4");
				compiler.move("#0", "D5");
				compiler.move("#0", "D6");
				compiler.move("#0", "D7");
				compiler.move("#0", "D8");
				compiler.move("#0", "D9");
				program();
				match("eof");
				compiler.halt();
				if(parseError){
					return 0;
				} else {
					return 1;
				}
			default:
				return 0;
		}
	}

	/**
	 * >> ProgramHeading ";" Block "."
	 */
	private void program() {
		switch (lookAhead.getIdentifier()) {
			case "mp_program":
				symbolTable = Table.rootInstance();
				programHeading();
				match(";");
				block();
				match(".");
				break;
			default:
				handleError(false, "Program");
		}

	}

	/**
	 * >> "program" Identifier
	 */
	private void programHeading() {
		switch (lookAhead.getIdentifier()) {
		case "mp_program":
			match("program");
			compiler.move("SP", "D0");
			symbolTable.setTitle(lookAhead.getLexeme());
			sr = identifier();
			break;
		default:
			handleError(false, "Program Heading");
		}

	}

	/**
	 * >> [ VariableDeclarationPart ] [ ProcedureAndFunctionDeclarationPart ] StatementPart
	 */
	private void block() {
		switch (lookAhead.getIdentifier()) {
			case "mp_var":
				variableDeclarationPart();
			case "mp_procedure":
			case "mp_function":
				procedureAndFunctionDeclarationPart();
			case "mp_begin":
				// move SP to end of activation record
				compiler.add("D"+symbolTable.getLevel(), "#"+symbolTable.getSize(), "SP");
				statementPart();
				break;
			default:
				handleError(false, "Block");
		}
	}

	/**
	 * >> "var" VariableDeclaration ";" [ VariableDeclaration ";" ]*
	 * >> ProcedureAndFunctionDeclarationPart
	 */
	private void variableDeclarationPart() {
		switch (lookAhead.getIdentifier()) {
			case "mp_var":
				match("var");
				variableDeclaration();
				match(";");
				while (lookAhead.getIdentifier().equals("mp_identifier")) {
					variableDeclaration();
					 match(";");
				}
				break;
			case "mp_procedure":
			case "mp_function":
			case "mp_begin":
				procedureAndFunctionDeclarationPart();
				break;
			default:
				handleError(false, "Variable Declaration Part");
		}
	}
	
	/**
	 * >> IdentifierList ":" Type
	 */
	private void variableDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				retValues = identifierList();
				match(":");
				type = type();
				ListIterator<String> iter = retValues.listIterator();
				// add variables to symbol table
				while(iter.hasNext()){
					String name = iter.next();
					if(!symbolTable.inTable(name,true)) {
						symbolTable.insert(name,"value", type, null);
					} else {
						invalidVariableName(name);
					}
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default:
				handleError(false, "Variable Declaration");
		}

	}

	/**
	 * >>  ProcedureDeclaration ProcedureAndFunctionDeclarationPart
	 * >>  FunctionDeclaration ProcedureAndFunctionDeclarationPart
	 * >>  empty
	 */
	private void procedureAndFunctionDeclarationPart() {
		switch (lookAhead.getIdentifier()) {
			case "mp_function":
				functionDeclaration();
				procedureAndFunctionDeclarationPart();
				break;
			case "mp_procedure":
				procedureDeclaration();
				procedureAndFunctionDeclarationPart();
				break;
			default:
				return;
		}
	}

	/**
	 * >> CompoundStatement
	 */
	private void statementPart() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin":
				compoundStatement();
				break;
			default:
				handleError(false, "Statement Part");
		}
	}

	/**
	 * >> "mp_integer"
	 * >> "mp_float"
	 * >> "mp_string"
	 * >> "mp_boolean"
	 * 
	 * @return	The variable type that was matched
	 */
	private String type() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer":
				match("integer");
				return "integer";
			case "mp_float":
				match("float");
				return "float";
			case "mp_string":
				match("string");
				return "string";
			case "mp_boolean":
				match("boolean");
				return "boolean";
			default:
				handleError(false, "Type");
				return null;
		}
	}

	/**
	 * >> ProcedureHeading ";" Block
	 */
	private void procedureDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_procedure":
				// compile code
				symbolTable = symbolTable.createScope();
				String endprocedure = compiler.skipLabel();
				compiler.branch(endprocedure);
				// continue parsing
				procedureHeading();
				match(";");
				block();
				// compile code
				compiler.move("D"+symbolTable.getLevel(), "SP");
				compiler.pop("D"+symbolTable.getLevel());
				compiler.returnCall();
				compiler.label(endprocedure);
				symbolTable = symbolTable.getParent();
				break;
			default:
				handleError(false, "Procedure Declaration");
		}
	}

	/**
	 * >>  FunctionHeading ";" Block
	 */
	private void functionDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_function":
				// compile code
				symbolTable = symbolTable.createScope();
				String endlabel = compiler.skipLabel();
				compiler.branch(endlabel);
				// continue parsing
				functionHeading();
				// find the return variable and store address of behind PC
				Symbol f = symbolTable.findSymbol(symbolTable.getTitle(),"var");
				compiler.subtract("D"+symbolTable.getLevel(), 
								"#3", 
								f.getOffset()+"(D"+symbolTable.getLevel()+")");
				// continue parsing
				match(";");
				block();
				// compile code
				Symbol p = symbolTable.getParent().findSymbol(symbolTable.getTitle(),"function");
				if (f==null || p==null) {
					handleErrorGeneral("Function not declared in this scope");
				} else {
					compiler.move("D"+symbolTable.getLevel(), "SP");
					compiler.pop("D"+symbolTable.getLevel());
					compiler.returnCall();
					
					compiler.label(endlabel);
					symbolTable = symbolTable.getParent();
				}
				break;
			default:
				handleError(false, "Function Declaration");
		}
	}

	/**
	 * >>  "procedure" Identifier [ FormalParameterList ]
	 */
	private void procedureHeading() {
		switch (lookAhead.getIdentifier()) {
			case "mp_procedure":
				String label = compiler.label();
				match("procedure");
				symbolTable.setTitle(lookAhead.getLexeme());
				sr = identifier();
				if (lookAhead.getIdentifier().equals("mp_lparen")) {
					formalParameterList();
				}
				Symbol s = symbolTable.getParent().insert(symbolTable.getTitle(), "procedure", "none", getAttributes());
				s.label = label;
				s.level = symbolTable.getLevel();
				break;
			default:
				handleError(false, "Procedure Heading");
		}

	}

	/**
	 * >>  "function" Identifier [ FormalParameterList ] ":" Type
	 */
	private void functionHeading() {
		switch (lookAhead.getIdentifier()) {
			case "mp_function":
				String label = compiler.label();
				match("function");
				symbolTable.setTitle(lookAhead.getLexeme());
				sr = identifier();
				if (lookAhead.getIdentifier().equals("mp_lparen")) {
					formalParameterList();
				}
				match(":");
				type = type();
				Symbol s = symbolTable.getParent().insert(symbolTable.getTitle(), "function", type, getAttributes());
				if (s==null) {
					handleErrorGeneral("Variable already defined");
				} else {
					s.label = label;
					s.level = symbolTable.getLevel();
					symbolTable.insert(symbolTable.getTitle(), "var", type, null);
				}
				break;
			default:
				handleError(false, "Function Heading");
		}
	}

	/**
	 * >>  "(" FormalParameterSection [ ";" FormalParameterSection ]* ")"
	 */
	private void formalParameterList() {
		switch (lookAhead.getIdentifier()) {
		case "mp_lparen":
			match("(");
			formalParameterSection();
			while (lookAhead.getIdentifier().equals("mp_scolon")) {
				match(";");
				formalParameterSection();
			}
			match(")");
			break;
		default:
			handleError(false, "Formal Parameter List");
		}
	}

	/**
	 * >>  ValueParameterSection
	 * >>  VariableParameterSection 
	 */
	private void formalParameterSection() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				valueParameterSection(); 
				break;
			case "mp_var":
				variableParameterSection();
				break;
			default:
				handleError(false, "Formal Parameter Section");
		}
	}

	/**
	 * >> IdentifierList ":" Type
	 */
	private void valueParameterSection() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // valueParameterSection -> IdentifierList, ":", Type
				retValues = identifierList();
				match(":");
				type =  type();
				// insert variables into symbol table
				ListIterator<String> iter = retValues.listIterator();
				while(iter.hasNext()){
					symbolTable.insert(iter.next(),"value", type, null);
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default:
				handleError(false, "Value Parameter Section");
		}
	}

	/**
	 * >>  "var" IdentifierList ":" Type            
	 */
	private void variableParameterSection() {
		switch (lookAhead.getIdentifier()) {
			case "mp_var":
				match("var");
				retValues = identifierList();
				match(":");
				type = type();
				// insert variables into symbol table
				ListIterator<String> iter = retValues.listIterator();
				while(iter.hasNext()){
					symbolTable.insert(iter.next(),"var", type, null);
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default:
				handleError(false, "Variable Parameter Section");
		}

	}

	/**
	 * >> begin StatementSequence "end" [ ";" ]
	 */
	private void compoundStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // compoundStatement -> "begin", statementSequence, "end"
				match("begin");
				statementSequence();
				match("end");
				if(lookAhead.getIdentifier().equalsIgnoreCase("mp_scolon")) {
					match(";");
				}
				break;
			default:
				handleError(false, "Compound Statement");
		}

	}

	/**
	 * >> Statement StatementTail
	 * >> empty
	 */
	private void statementSequence() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // statementSequence -> statement
			case "mp_for": // statementSequence -> statement
			case "mp_if": // statementSequence -> statement
			case "mp_read": // statementSequence -> statement
			case "mp_repeat": // statementSequence -> statement
			case "mp_while": // statementSequence -> statement
			case "mp_write": // statementSequence -> statement
			case "mp_writeln":
			case "mp_identifier": // statementSequence -> statement
			case "mp_scolon": // statementSequence -> statement
				statement();
				statementTail();
				break;
			case "mp_end":
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Statement Sequence");
		}
	}
	
	/**
	 * >> Statement StatementTail
	 * >> ";" Statement StatementTail
	 * >> empty
	 */
	private void statementTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // statementTail -> statement
			case "mp_for": // statementTail -> statement
			case "mp_if": // statementTail -> statement
			case "mp_read": // statementTail -> statement
			case "mp_repeat": // statementTail -> statement
			case "mp_while":  // statementTail -> statement
			case "mp_write":  // statementTail -> statement
			case "mp_writeln": // statementTail -> statement
			case "mp_identifier": // statementTail -> statement
				statement();
				statementTail();
				break;
			case "mp_scolon":
				match(";");
				statement();
				statementTail();
				break;
			case "mp_until":
			case "mp_end":
			case "mp_else":
			case "mp_period":
				break;
			default:
				handleError(false, "statementTail");
		}
	}
	
	/**
	 * >>  CompoundStatement
	 * >>  ReadStatement
	 * >>  WriteStatement
	 * >>  AssignmentStatement
	 * >>  IfStatement
	 * >>  WhileStatement
	 * >>  RepeatStatement
	 * >>  ForStatement
	 * >>  ProcedureStatement
	 * >>  FunctionDesignator
	 * >>  empty
	 */
	private void statement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // statement -> statementSequence
				compoundStatement();
				break;
			case "mp_for": // statement -> forStatement
				forStatement();
				break;
			case "mp_if": // statement -> ifStatement
				ifStatement();
				break;
			case "mp_repeat": // statement -> repeatStatement
				repeatStatement();
				break;
			case "mp_while": // statement -> whileStatement
				whileStatement();
				break;
			case "mp_read": // statement -> readStatement
				readStatement();
				break;
			case "mp_write": // statement -> writeStatement
			case "mp_writeln":
				writeStatement();
				break;
			case "mp_identifier": // statement -> simpleStatement
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s!=null) {
					if (s.token=="procedure") {
						procedureStatement();
					} else if (s.token=="function") {
						functionDesignator();
					} else {
						assignmentStatement();
					}
				} else {
					handleErrorGeneral("Undefined identifier");
				}
				break;
			case "mp_scolon":    // statement -> emptyStatement
			case "mp_else":
			case "mp_until":
			case "mp_end":
				break;
			default:
				handleError(false, "Statement");
		}
	}
	
	/**
	 * >> [ ";" ]*
	 */
	private void emptyStatement() {
		while(lookAhead.getIdentifier().equals("mp_scolon")){
			match(";");
		}
	}

	/**
	 * >>  "read" ReadParameterList
	 */
	private void readStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_read": // readStatement -> "read", readParameterList
				match("read");
				readParameterList();
				break;
			default:
				handleError(false, "Read Statement");
		}
	}

	/**
	 * >>  "write" WriteParameterList
	 * >>  "writeln" WriteParameterList
	 */
	private void writeStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_write":
				match("write");
				writeParameterList();
				break;
			case "mp_writeln":
				match("writeln");
				writeParameterList();
				compiler.write("#\"\\n\"");
				break;
			default:
				handleError(false, "Write Statement");
		}
	}

	/**
	 * >>  Variable ":=" Expression
	 * >>  Variable FunctionDesignatorTail
	 */
	private void assignmentStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // assignmentStatement -> (Variable|FunctionIdentifier), ":=", expression
				
				Symbol symbol = symbolTable.findSymbol(lookAhead);
				if (symbol==null) {
					handleError(false, "Variable not found");
					break;
				}
				String lex = lookAhead.getLexeme();
				
				// found a variable
				if (symbol.getToken()=="var" || symbol.getToken()=="value") {
					variable();
				} else {
					undeclaredVariableError(lookAhead.getLexeme());
					lookAhead = scanner.getToken();
				}
				// not actually an assignment, just a stand-alone recursive function call
				if (lookAhead.getLexeme().equals("(")) {
					passSymbol = symbolTable.findSymbol(lex,"var");
					functionDesignatorTail();
					break;
				}
				match(":=");
				sr = expression();
				SR src = sr;
				SR dst = symbol.getType();
				if (dst==null) {
					handleErrorGeneral("Variable type not recognized.");
				}
				
				if (src.checkIntlit()) {
					if (dst.checkIntlit()) {
						// int := int
						compiler.pop(symbol.getAddress());
					} else if (dst.checkFixedlit()) {
						// float := int
						compiler.castStackFloat();
						compiler.pop(symbol.getAddress());
					} else {
						// bool || string (don't allow) := int
						handleErrorGeneral("Invalid type cast to int");
					}
				} else if (src.checkFixedlit()) {
					if (dst.checkIntlit()) {
						// int := float
						handleWarningGeneral("Possible loss of precision, attempt to assign float to int");
						compiler.castStackInteger();
						compiler.pop(symbol.getAddress());
					} else if (dst.checkFixedlit()) {
						// float := float
						compiler.pop(symbol.getAddress());
					} else {
						// bool || string (don't allow) := float
						handleErrorGeneral("Invalid type cast to float");
					}
				} else if (src.checkStringlit()) {
					if (dst.checkStringlit()) {
						// string := string
						compiler.pop(symbol.getAddress());
					} else {
						// can only assign strings to strings
						handleErrorGeneral("Invalid type cast to string");
					}
				} else if (src.checkBool()) {
					if (dst.checkBool()) {
						// bool := bool
						compiler.pop(symbol.getAddress());
					} else {
						// can only assign strings to strings
						handleErrorGeneral("Invalid type cast to boolean");
					}
				} else {
					handleErrorGeneral("Unrecognized type");
				}
				
				break;
			default:
				handleError(false, "Assignment Statement");
		}
	}

	/**
	 * >> "if" BooleanExpression "then" Statement EmptyStatement [ "else" Statement EmptyStatement ]
	 */
	private void ifStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_if": // ifStatement -> "if", booleanExpression, "then", statement, ["else", statement]
				match("if");
				booleanExpression();
				String elselabel = compiler.skipLabel();
				compiler.branchFalseStack(elselabel);
				match("then");
				statement();
				emptyStatement();
				String endlabel = compiler.skipLabel();
				compiler.branch(endlabel);
				compiler.label(elselabel);
				if (lookAhead.getIdentifier().equals("mp_else")) {
					match("else");
					statement();
					emptyStatement();
				}
				compiler.label(endlabel);
				break;
			default:
				handleError(false, "If Statement");
		}
	}

	/**
	 * >> "repeat" StatementSequence [ ";" ] "until" BooleanExpression
	 */
	private void repeatStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_repeat": // repeatStatement -> "repeat", statementSequence, "until", booleanExpression
			match("repeat");
			String startlabel = compiler.label();
			statementSequence();
			if(lookAhead.getIdentifier().equals("mp_scolon")){
				match(";");
			}
			match("until");
			booleanExpression();
			compiler.branchFalseStack(startlabel);
			break;
		default:
			handleError(false, "Repeat Statement");
		}
	}

	/**
	 * >> "while" BooleanExpression "do" Statement EmptyStatement
	 */
	private void whileStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_while": // whileStatement -> "while", booleanExpression, "do"
				match("while");
				String startlabel = compiler.label();
				booleanExpression();
				String endlabel = compiler.skipLabel();
				compiler.branchFalseStack(endlabel);
				match("do");
				statement();
				emptyStatement();
				compiler.branch(startlabel);
				compiler.label(endlabel);
				break;
			default:
				handleError(false, "While Statement");
		}
	}

	/**
	 * >> "for" ControlVariable ":=" InitialValue ("to"|"downto") FinalValue "do" Statement EmptyStatement
	 */
	private void forStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_for": // forStatement -> "for", controlVariable, ":=", initialValue, ("to"|"downto"), finalVariable, "do", statement\
				match("for");
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s==null || (s.getToken()!="var" && s.getToken()!="value")) {
					undeclaredVariableError(lookAhead.getLexeme());
					break;
				}
				controlVariable();
				match(":=");
				initialValue();
				compiler.pop(s.getAddress());
				boolean add = true;
				if (lookAhead.getIdentifier().equals("mp_to")) {
					match("to");
				} else if (lookAhead.getIdentifier().equals("mp_downto")) {
					match("downto");
					add = false;
				} else {
					handleError(false, "For Statement Argument");
				}
				String finalvalue = lookAhead.getLexeme();
				compiler.push(s.getAddress());
				finalValue();
				String endforloop = compiler.skipLabel();
				if (add) {
					compiler.compareGreaterStack();
					compiler.branchTrueStack(endforloop);
				} else {
					compiler.compareLessStack();
					compiler.branchTrueStack(endforloop);
				}
				match("do");
				String startlabel = compiler.label();
				statement();
				emptyStatement();
				compiler.push(s.getAddress());
				compiler.push("#1");
				if (add) {
					compiler.addStack();
				} else {
					compiler.subtractStack();
				}
				compiler.pop(s.getAddress());
				compiler.push(s.getAddress());
				Symbol f = symbolTable.findSymbol(finalvalue,"var");
				if (f==null) f = symbolTable.findSymbol(finalvalue,"value");
				if (f==null) {
					compiler.push("#"+finalvalue);
				} else {
					compiler.push(f.getAddress());
				}
				if (add) {
					compiler.compareLessEqualStack();
					compiler.branchTrueStack(startlabel);
				} else {
					compiler.compareGreaterEqualStack();
					compiler.branchTrueStack(startlabel);
				}
				compiler.label(endforloop);
				break;
			default:
				handleError(false, "For Statement");
		}
	}

	/**
	 * >>  VariableIdentifier
	 */
	private void controlVariable() {
		variableIdentifier();
	}

	/**
	 * >>  OrdinalExpression
	 */
	private void initialValue() {
		ordinalExpression();
	}

	/**
	 * >>  OrdinalExpression
	 */
	private void finalValue() {
		ordinalExpression();
	}

	/**
	 * >> SimpleExpression Expression
	 * >> RelationalOperator SimpleExpression
	 * 
	 * @return	Type of variable left on stack
	 */
	private SR expression() {
		
		SR sr1, sr2;
		boolean error;
		
		switch (lookAhead.getIdentifier()) {
		
			case "mp_plus":
			case "mp_minus":
			case "mp_integer_lit":
			case "mp_fixed_lit":
			case "mp_float_lit":
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_lparen":
			case "mp_true":
			case "mp_false":
			case "mp_not":
				sr = simpleExpression();
				sr = expression();
				break;
			case "mp_equal":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int = int
						compiler.compareEqualStack();
					} else if (sr2.checkFixedlit()) {
						// int = float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float = int
						compiler.castStackFloat();
						compiler.compareEqualStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float = float
						compiler.compareEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkBool()) {
					if (sr2.checkBool()) {
						compiler.compareEqualStack();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform equals comparison on non-numeric or boolean values");
				}
				sr = SR.bool();
				break;
			case "mp_lthan":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int < int
						compiler.compareLessStack();
					} else if (sr2.checkFixedlit()) {
						// int < float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareLessStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float < int
						compiler.castStackFloat();
						compiler.compareLessStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float < float
						compiler.compareLessStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform less-than comparison on non-numeric values");
				}
				sr = SR.bool();
				break;
			case "mp_gthan":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int > int
						compiler.compareGreaterStack();
					} else if (sr2.checkFixedlit()) {
						// int > float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareGreaterStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float > int
						compiler.castStackFloat();
						compiler.compareLessStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float > float
						compiler.compareGreaterStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform greater-than comparison on non-numeric values");
				}
				sr = SR.bool();
				break;
			case "mp_lequal":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int <= int
						compiler.compareLessEqualStack();
					} else if (sr2.checkFixedlit()) {
						// int <= float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareLessEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float <= int
						compiler.castStackFloat();
						compiler.compareLessEqualStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float <= float
						compiler.compareLessEqualStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform less-than-or-equal-to comparison on non-numeric values");
				}
				sr = SR.bool();
				break;
			case "mp_gequal":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int <= int
						compiler.compareGreaterEqualStack();
					} else if (sr2.checkFixedlit()) {
						// int <= float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareGreaterEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float <= int
						compiler.castStackFloat();
						compiler.compareGreaterEqualStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float <= float
						compiler.compareGreaterEqualStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform greater-than-or-equal-to comparison on non-numeric values");
				}
				sr = SR.bool();
				break;
			case "mp_nequal":
				relationalOperator();
				sr1 = sr;
				sr2 = simpleExpression();
				error = false;
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int <> int
						compiler.compareNotEqualStack();
					} else if (sr2.checkFixedlit()) {
						// int <> float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.compareNotEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float <> int
						compiler.castStackFloat();
						compiler.compareNotEqualStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float <> float
						compiler.compareNotEqualStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkBool()) {
					if (sr2.checkBool()) {
						compiler.compareNotEqualStack();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform not-equal comparison on non-numeric values");
				}
				sr = SR.bool();
				break;
			default: // optional case statement, proceed
				break;
			
		}
		
		return sr;
		
	}

	/**
	 * >> [ Sign ] Term [ AddingOperator Term ]*
	 * 
	 * @return	Type of variable left on stack
	 */
	private SR simpleExpression() {
		// optional sign
		boolean negative = false;
		if (lookAhead.getIdentifier()=="mp_plus" || lookAhead.getIdentifier()=="mp_minus") {
			if (lookAhead.getIdentifier()=="mp_minus") {
				negative = true;
			}
			sign();
		}
		switch (lookAhead.getIdentifier()) {
			case "mp_string_lit":
			case "mp_lparen":
			case "mp_not":
			case "mp_true":
			case "mp_false":
			case "mp_identifier":
			case "mp_fixed_lit":
			case "mp_float_lit":
			case "mp_integer_lit":
				sr = term();
				break;
			default:
				handleError(false, "simple Expression");
		}
		// if the sign was negative
		if (negative) {
			if (sr.checkIntlit()) {
				compiler.negateStack();
			} else if (sr.checkFixedlit()) {
				compiler.negateStackFloat();
			}
		}
		while (lookAhead.getIdentifier().equals("mp_plus") || lookAhead.getIdentifier().equals("mp_minus") || lookAhead.getIdentifier().equals("mp_or")) {
			if (lookAhead.getIdentifier().equals("mp_plus")) {
				addingOperator();
				SR sr1 = sr;
				SR sr2 = term();
				boolean error = false;
				// by default, set result to float
				sr = SR.fixedlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int + int
						compiler.addStack();
						sr = SR.intlit();
					} else if (sr2.checkFixedlit()) {
						// int + float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.addStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float + int
						compiler.castStackFloat();
						compiler.addStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float + float
						compiler.addStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform addition on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_minus")) {
				addingOperator();
				SR sr1 = sr;
				SR sr2 = term();
				boolean error = false;
				// by default, set result to float
				sr = SR.fixedlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int + int
						compiler.subtractStack();
						sr = SR.intlit();
					} else if (sr2.checkFixedlit()) {
						// int + float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.subtractStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float + int
						compiler.castStackFloat();
						compiler.subtractStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float + float
						compiler.subtractStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform subtraction on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_or")) {
				addingOperator();
				SR sr1 = sr;
				SR sr2 = term();
				boolean error = false;
				// result is boolean
				sr = SR.bool();
				if (sr1.checkBool()) {
					if (sr2.checkBool()) {
						// bool or bool
						compiler.orStack();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform or operator on non-boolean values");
				}
			}
		}
		
		return sr;
	}

	/**
	 * >> Factor [ MultiplyingOperator Factor ]*
	 * 
	 * @return	Type of variable left on stack
	 */
	private SR term() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s!=null) {
					compiler.push(s.getAddress());
				} else {
					handleErrorUndefined();
				}
				sr = factor();
				break;
			case "mp_fixed_lit":
			case "mp_float_lit":
			case "mp_integer_lit":
			case "mp_string_lit":
			case "mp_lparen":
			case "mp_true":
			case "mp_false":
			case "mp_not":
				sr = factor();
				break;
			default:
				handleError(false, "term");
		}
		while (lookAhead.getIdentifier().equals("mp_times") || lookAhead.getIdentifier().equals("mp_div") || lookAhead.getIdentifier().equals("mp_mod") || lookAhead.getIdentifier().equals("mp_and") || lookAhead.getIdentifier().equals("mp_divide_float")) {
			if (lookAhead.getIdentifier().equals("mp_times")) {
				multiplyingOperator();
				SR sr1 = sr;
				if (lookAhead.getIdentifier().equals("mp_identifier")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s!=null) {
						compiler.push(s.getAddress());
					} else {
						handleErrorUndefined();
					}
				}
				SR sr2 = factor();
				boolean error = false;
				// by default, set result to float
				sr = SR.fixedlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int * int
						compiler.multiplyStack();
						// set result to int
						sr = SR.intlit();
					} else if (sr2.checkFixedlit()) {
						// int * float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.multiplyStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float * int
						compiler.castStackFloat();
						compiler.multiplyStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float * float
						compiler.multiplyStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform multiplication on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_div")) {
				multiplyingOperator();
				SR sr1 = sr;
				if (lookAhead.getIdentifier().equals("mp_identifier")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s!=null) {
						compiler.push(s.getAddress());
					} else {
						handleErrorUndefined();
					}
				}
				SR sr2 = factor();
				boolean error = false;
				// all results are floats
				sr = SR.fixedlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int / int
						compiler.divideStack();
						// set result to int
						sr = SR.intlit();
					} else if (sr2.checkFixedlit()) {
						// int / float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.divideStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float / int
						compiler.castStackFloat();
						compiler.divideStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float / float
						compiler.divideStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform division on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_divide_float")) {
				multiplyingOperator();
				SR sr1 = sr;
				if (lookAhead.getIdentifier().equals("mp_identifier")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s!=null) {
						compiler.push(s.getAddress());
					} else {
						handleErrorUndefined();
					}
				}
				SR sr2 = factor();
				boolean error = false;
				// all results are floats
				sr = SR.fixedlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int / int
						compiler.castStackFloat();
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.divideStackFloat();
						// set result to int
						sr = SR.intlit();
					} else if (sr2.checkFixedlit()) {
						// int / float
						compiler.subtract("SP", "#1", "SP");
						compiler.castStackFloat();
						compiler.add("SP", "#1", "SP");
						compiler.divideStackFloat();
					} else {
						error = true;
					}
				} else if (sr1.checkFixedlit()) {
					if (sr2.checkIntlit()) {
						// float / int
						compiler.castStackFloat();
						compiler.divideStackFloat();
					} else if (sr2.checkFixedlit()) {
						// float / float
						compiler.divideStackFloat();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform division on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_mod")) {
				multiplyingOperator();
				SR sr1 = sr;
				if (lookAhead.getIdentifier().equals("mp_identifier")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s!=null) {
						compiler.push(s.getAddress());
					} else {
						handleErrorUndefined();
					}
				}
				SR sr2 = factor();
				boolean error = false;
				// only allows integers
				sr = SR.intlit();
				if (sr1.checkIntlit()) {
					if (sr2.checkIntlit()) {
						// int % int
						compiler.modulusStack();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform modulus on non-numeric values");
				}
			} else if (lookAhead.getIdentifier().equals("mp_and")) {
				multiplyingOperator();
				SR sr1 = sr;
				if (lookAhead.getIdentifier().equals("mp_identifier")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s!=null) {
						compiler.push(s.getAddress());
					} else {
						handleErrorUndefined();
					}
				}
				SR sr2 = factor();
				boolean error = false;
				// all are booleans
				sr = SR.bool();
				if (sr1.checkBool()) {
					if (sr2.checkBool()) {
						// int * int
						compiler.andStack();
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					this.handleErrorGeneral("Cannot perform and operator on non-boolean values");
				}
		    } else {
				multiplyingOperator();
				factor();
			}
		}
		
		return sr;
	}

	/**
	 * >> UnsignedInteger
	 * >> Variable [ FunctionDesignatorTail ]
	 * >> FunctionDesignator
	 * >> Identifier
	 * >> not SimpleExpression
	 * >> "(" Expression ")"
	 * 
	 * @return	Type of variable left on stack
	 */
	private SR factor() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				compiler.push("#"+lookAhead.getLexeme());
				sr = unsignedInteger();
				break;
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s!=null) {
					if(s.getToken()=="var" || s.getToken()=="value") {
						String lex = lookAhead.getLexeme();
						sr = variable();
						if (lookAhead.getLexeme().equals("(")) {
							passSymbol = symbolTable.findSymbol(lex, "var");
							sr = passSymbol.getType();
							// back up SP (address of return variable was already pushed)
							compiler.subtract("SP","#1","SP");
							functionDesignatorTail();
						}
					} else {
						sr = functionDesignator();
					}
				} else {
					this.undeclaredVariableError(lookAhead.getLexeme());
				}
				break;
			case "mp_fixed_lit":
			case "mp_float_lit":
				compiler.push("#"+lookAhead.getLexeme());
				sr = identifier();
				break;
			case "mp_string_lit":
				compiler.push("#\""+lookAhead.getLexeme()+"\"");
				sr = identifier();
				break;
			case "mp_lparen":
				match("(");
				sr = expression();
				match(")");
				break;
			case "mp_not":
				match("not");
				sr = simpleExpression();
				if (sr.checkBool()) {
					String toZero = compiler.skipLabel();
					String flipDone = compiler.skipLabel();
					compiler.push("#0");
					compiler.compareGreaterStack();
					compiler.branchTrueStack(toZero);
					compiler.push("#1");
					compiler.branch(flipDone);
					compiler.label(toZero);
					compiler.push("#0");
					compiler.label(flipDone);
					sr = SR.bool();
				} else {
					handleErrorGeneral("Cannot perform not operator on non-boolean value");
				}
				break;
			case "mp_true":
				match("true");
				compiler.push("#1");
				sr = SR.bool();
				break;
			case "mp_false":
				match("false");
				compiler.push("#0");
				sr = SR.bool();
				break;
			default:
				handleError(false, "Factor");
		}
		
		return sr;

	}

	/**
	 * >> "mp_equal"
	 * >> "mp_lthan"
	 * >> "mp_gthan"
	 * >> "mp_lequal"
	 * >> "mp_gequal"
	 * >> "mp_nequal"
	 */
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
		default:
			handleError(false, "Relational Operator");
		}
	}

	/**
	 * >> "mp_plus"
	 * >> "mp_minus"
	 * >> "mp_or"
	 */
	private void addingOperator() {
		switch (lookAhead.getIdentifier()) {
			case "mp_plus":
				match("+");
				break;
			case "mp_minus":
				match("-");
				break;
			case "mp_or":
				match("or");
				break;
			default:
				handleError(false, "Adding Operator");
		}
	}

	/**
	 * >> "mp_times"
	 * >> "mp_div"
	 * >> "mp_divide_float"
	 * >> "mp_mod"
	 * >> "mp_and"
	 */
	private void multiplyingOperator() {
		switch (lookAhead.getIdentifier()) {
			case "mp_times":
				match("*");
				break;
			case "mp_div":
				match("div");
				break;
			case "mp_divide_float":
				match("/");
				break;
			case "mp_mod":
				match("mod");
				break;
			case "mp_and":
				match("and");
				break;
			default:
				handleError(false, "Multiplying Operator");
		}
	}
	
	/**
	 * >> ProcedureIdentifier [ ActualParameterList ]
	 */
	private void procedureStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				passSymbol = symbolTable.findSymbol(lookAhead.getLexeme(), "procedure");
				procedureIdentifier();
				break;	
			default:
				handleError(false, "Procedure Statement");
		}
		
		String register = "D"+(passSymbol.getLevel()+1);
		// leave space for PC pushed by call later
		compiler.add("SP","#1","SP");
		compiler.push(register);
		
		int count = 0;
		if (lookAhead.getIdentifier().equals("mp_lparen")) {
			count = actualParameterList();
		}
		
		// back up DX to point to beginning of activation record (had to wait because of recursive calls needing reference to current DX)
		compiler.subtract("SP", "#"+count, register);
		
		compiler.subtract(register, "#2", "SP");
		compiler.call(passSymbol.label);
	}

	/**
	 * >> FunctionIdentifier FunctionDesignatorTail
	 * 
	 * @return	The return type of the function left on the stack
	 */
	private SR functionDesignator() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				passSymbol = symbolTable.findSymbol(lookAhead.getLexeme(), "function");
				sr = functionIdentifier();
				functionDesignatorTail();
				break;
			default:
				handleError(false, "Function Designator");
		}
		return sr;
	}
	
	/**
	 * >> [ ActualParameterList ]
	 */
	private void functionDesignatorTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
			case "mp_rparen":
			case "mp_scolon":
			case "mp_comma":
			case "mp_lthan":
			case "mp_gthan":
			case "mp_equals":	
			case "not":
			case "mp_plus":
			case "mp_minus":
			case "mp_time":
			case "mp_div":
			case "mp_/":
				int level = passSymbol.getLevel();
				if (passSymbol.getToken().equals("function")) {
					level++;
				}
				String register = "D"+level;
				// leave 2 places for PC and return var
				compiler.add("SP","#2","SP");
				compiler.push(register);
				int count = 0;
				if(lookAhead.getLexeme().equals("(")){
						count = actualParameterList();
				}
				
				// back up DX to point to beginning of activation record (had to wait because of recursive calls needing reference to current DX)
				compiler.subtract("SP", "#"+count, register);
				// go to where PC should be placed
				compiler.subtract(register, "#3", "SP");
				// push the return value
				compiler.push("#0"); // like setting result to null, kind of
				passSymbol = symbolTable.findSymbol(passSymbol.getName(),"function");
				compiler.call(passSymbol.label);
				
				break;
			default:
				handleError(false, "Function Designator Tail");
		}
	}

	/**
	 * >> VariableIdentifier
	 * 
	 * @return
	 */
	private SR variable() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_int_lit":
			case "mp_fixed_lit":	
			case "mp_float_lit":
				sr = variableIdentifier();
				break;
			default:
				handleError(false, "Variable");
		}
		return sr;
	}
	
	/**
	 * >>  "(" ActualParameter [ "," ActualParameter ]* ")"
	 * 
	 * @return	number of parameters seen
	 */
	private int actualParameterList() {
		int count = 0;
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				match("(");
				String attr[];
				if (passSymbol.getToken().equals("var")) {
					attr = symbolTable.findSymbol(passSymbol.getName(),"function").getAttribute(count);
				} else {
					attr = passSymbol.getAttribute(count);
				}
				
				// pass by pointer
				if (attr[0].equals("var")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s==null) {
						handleErrorGeneral("Must pass an existing variable to pointer argument.");
						break;
					}
					if (s.getToken().equals("value")) {
						if (s.getTypeString().equals(attr[1])) {
							// type matches, pass variable's address
							compiler.push("D"+s.getLevel());
							compiler.push("#"+s.getOffset());
							compiler.addStack();
							match(lookAhead.getLexeme());
						} else {
							handleErrorGeneral("Argument type mismatch");
						}
					} else if (s.getToken().equals("var")) {
						if (s.getTypeString().equals(attr[1])) {
							// type matches, pass pointer directly
							compiler.push(s.getAddress(false));
							match(lookAhead.getLexeme());
						} else {
							handleErrorGeneral("Argument type mismatch");
						}
					} else {
						handleErrorGeneral("Pointer must be passed as a variable name.");
					}
				// pass by value
				} else {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (lookAhead.compareIdentifier(attr[1]) || (lookAhead.getIdentifier().equals("mp_identifier") && s.getTypeString().equals(attr[1]))) {
						// matches correct type
						actualParameter();
					} else {
						handleErrorGeneral("Argument type mismatch");
					}
				}
				
				while (lookAhead.getIdentifier().equals("mp_comma")) {
					count++;
					match(",");
					// move SP as activation record increases in size
					if (passSymbol.getToken().equals("var")) {
						attr = symbolTable.findSymbol(passSymbol.getName(),"function").getAttribute(count);
					} else {
						attr = passSymbol.getAttribute(count);
					}
					// pass by pointer
					if (attr[0].equals("var")) {
						Symbol s = symbolTable.findSymbol(lookAhead);
						if (s==null) {
							handleErrorGeneral("Must pass an existing variable to pointer argument.");
							break;
						}
						if (s.getToken().equals("value")) {
							if (s.getTypeString().equals(attr[1])) {
								// type matches, pass variable's address
								compiler.push("D"+s.getLevel());
								compiler.push("#"+s.getOffset());
								compiler.addStack();
								match(lookAhead.getLexeme());
							} else {
								handleErrorGeneral("Argument type mismatch");
							}
						} else if (s.getToken().equals("var")) {
							if (s.getTypeString().equals(attr[1])) {
								// type matches, pass pointer directly
								compiler.push(s.getAddress(false));
								match(lookAhead.getLexeme());
							} else {
								handleErrorGeneral("Argument type mismatch");
							}
						} else {
							handleErrorGeneral("Pointer must be passed as a variable name.");
						}
					// pass by value
					} else {
						Symbol s = symbolTable.findSymbol(lookAhead);
						if (lookAhead.compareIdentifier(attr[1]) || (lookAhead.getIdentifier().equals("mp_identifier") && s.getTypeString().equals(attr[1]))) {
							// matches correct type
							actualParameter();
						} else {
							handleErrorGeneral("Argument type mismatch");
						}
					}
				}
				match(")");
				// count is indexed starting at zero, increment it up to provide an actual count of params
				count++;
				break;
			case "mp_scolon":
				break;
			default:
				handleError(false, "Actual Parameter List");
		}
		
		return count;
	}
	
	/**
	 * >> Expression
	 */
	private void actualParameter() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_fixed_lit":
			case "mp_float_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
			case "mp_true":
			case "mp_false":
			case "mp_string_lit":
				expression();
				break;
			default:
				handleError(false, "Actual Parameter");
		}
	}

	/**
	 * >> "(" ReadParameter [ "," ReadParameter ]* ")"
	 */
	private void readParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				match("(");
				readParameter();
				while (lookAhead.getIdentifier().equals("mp_comma")) {
					match(",");
					readParameter();
				}
				match(")");
				break;
			default:
				handleError(false, "Read Parameter List");
		}
	}

	/**
	 * >> VariableIdentifier
	 */
	private void readParameter() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s.getToken().equals("var") || s.getToken().equals("value")) {
					if (s.type=="integer") {
						compiler.readInt(s.getAddress());
					} else if (s.type=="float") {
						compiler.readFloat(s.getAddress());
					} else if(s.type=="string"){
						compiler.readString(s.getAddress());	
					}
					match(lookAhead.getLexeme());
				}else {
					undeclaredVariableError(lookAhead.getLexeme());
				}
				break;
			default:
				handleError(false, "Read Parameter");
		}
	}

	/**
	 *  >> "(" WriteParameter [ "," WriteParameter ]* ")"
	 */
	private void writeParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_scolon":
				break;
			case "mp_lparen":
				match("(");
				writeParameter();
				while (lookAhead.getIdentifier().equals("mp_comma")) {
					match(",");
					writeParameter();
				}
				match(")");
				break;
			default:
				handleError(false, "Write Parameter List");
		}
		
	}

	/**
	 *   >> Expression
	 */
	private void writeParameter() {
		switch (lookAhead.getIdentifier()) {
			case "mp_string_lit":
			case "mp_fixed_lit":
			case "mp_float_lit":
			case "mp_integer_lit":
			case "mp_identifier":
			case "not":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
			case "mp_true":
			case "mp_false":
				sr = expression();
				break;
			default:
				handleError(false, "Write");
		}
		
		// perform write
		if (sr.checkBool()) {
			compiler.push("#1");
			compiler.compareEqualStack();
			String trueLabel = compiler.skipLabel();
			String endLabel = compiler.skipLabel();
			compiler.branchTrueStack(trueLabel);
			compiler.write("#\"FALSE\"");
			compiler.branch(endLabel);
			compiler.label(trueLabel);
			compiler.write("#\"TRUE\"");
			compiler.label(endLabel);
		} else {
			// int, float, or string
			compiler.writeStack();
		}
		
	}

	/**
	 *  >> OrdinalExpression
	 */
	private void booleanExpression() {
		switch (lookAhead.getIdentifier()) {
			case "mp_not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				ordinalExpression();
				break;
			default:
				handleError(false, "Boolean Expression");
		}
	}
	
	/**
	 *  >> Expression
	 */
	private void ordinalExpression() {
		switch (lookAhead.getIdentifier()) {
			case "mp_not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				expression();
				break;
			default:
				handleError(false, "Ordinal Expression");
		}
	}

	/**
	 *  >> Identifier
	 * 
	 * @return	The type of the variable that was found
	 */
	private SR variableIdentifier() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_integer_lit":
			case "mp_fixed_lit":
			case "mp_float_lit":
				sr = identifier();
				break;
			default:
				handleError(false, "Variable Identifier");
		}
		return sr;
	}

	/**
	 *  >> [ Identifier ]
	 */
	private void procedureIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				if(symbolTable.inTable(lookAhead.getLexeme(), "procedure")) {
					identifier();
				}	
				break;
			default:
				handleError(false, "Procedure Identifier");
		}
	}
	
	/**
	 *  >> [ Identifier ]
	 * 
	 * @return	The return type of the function found
	 */
	private SR functionIdentifier() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				if(symbolTable.inTable(lookAhead.getLexeme(), "function"))
					sr = identifier();
				break;
			default:
				handleError(false, "Function Identifier");
		}
		return sr;
	}

	/**
	 *  >> Identifier [ "," Identifer ]*
	 * 
	 * @return	The list of lexemes seen in the list
	 */
	private ArrayList<String> identifierList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				ArrayList<String> varLexemes = new ArrayList<String>();
				varLexemes.add(lookAhead.getLexeme());
				identifier();
				while (lookAhead.getIdentifier().equals("mp_comma")) {
					match(",");
					varLexemes.add(lookAhead.getLexeme());
					identifier();
				}
				return varLexemes;
			default:
				handleError(false, "Identifier List");
				return null;
		}
	}

	/**
	 * >> "mp_identifier"
	 * >> "mp_string_lit"
	 * >> "mp_int_lit"
	 * >> "mp_fixed_lit"
	 * >> "mp_float_lit"
	 * 
	 * @return	The type of the identifier seen
	 */
	private SR identifier() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s!=null) {
					sr = s.getType();
				}
				match(lookAhead.getLexeme());
				break;
			case "mp_string_lit":
				sr = SR.stringlit();
				match(lookAhead.getLexeme());
				break;
			case "mp_int_lit":
				sr = SR.intlit();
				match(lookAhead.getLexeme());
				break;
			case "mp_fixed_lit":
			case "mp_float_lit":
				sr = SR.fixedlit();
				match(lookAhead.getLexeme());
				break;
			default:
				handleError(false, "Identifier");
		}
		return sr;
	}

	/**
	 * >> "mp_integer_lit"
	 * 
	 * @return	A type of integer
	 */
	private SR unsignedInteger() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				sr = SR.intlit();
				digitSequence();
				break;
			default:
				handleError(false, "Unsigned Integer");
		}
		return sr;
	}

	/**
	 * >> "mp_plus"
	 * >> "mp_minus"
	 */
	private void sign() {
		switch (lookAhead.getIdentifier()) {
			case "mp_plus":
				match("+");
				break;
			case "mp_minus":
				match("-");
				compiler.push("#-1");
				break;
			default:
				handleError(false, "Sign");
		}
	}
	
	/**
	 * >> "mp_integer_lit"
	 */
	private void digitSequence() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				boolean fail = false;
				for (int i = 0; i < lookAhead.getLexeme().length(); i++) {
					if (!digit(lookAhead.getLexeme().charAt(i))) {
						fail = true;
						break;
					}
				}
				if (!fail) {
					match(lookAhead.getLexeme());
				}
				break;
			default:
				handleError(false, "Digit Sequence");
		}
	}

	/**
	 * Helper function for digitSequence
	 * 
	 * @param digit	The digit to search for in character form
	 * @return		The digit in numerical format
	 */
	private boolean digit(char digit) {
		boolean found = false;
		char digits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		for (int j = 0; j < digits.length; j++) {
			if (digit == digits[j]) {
				found = true;
				break;
			}
		}
		if (!found)
			handleError(true, "digit");
		return found;
	}
	
	/**
	 * Retrieves the attributes of all symbols currently in the symbol table.
	 * 
	 * @return	A two dimensional array containing the token and type of each symbol
	 */
	private String[][] getAttributes(){
		String[][] params = new String[symbolTable.getSize()][2];
		for(int i = 0; i<symbolTable.getSize(); i++){
			Symbol s = symbolTable.getSymbol(i);
			String[] param = {s.token,s.type};
			params[i] = param;
		}
		return params;
	}

}
