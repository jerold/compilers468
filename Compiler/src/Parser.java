import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class Parser {

	private Token lookAhead;
	private Scanner scanner;
	private boolean parseError = false;
	private Table symbolTable;
	//private Table newScope;
	private ArrayList<String> retValues;
	private String type;
	private Compiler compiler;
	private Symbol passSymbol;
	private int ramSize;
	private int memorySize;
	private SR sr;
	
	public Parser(Scanner scanner, Compiler compiler) {
		this.scanner = scanner;
		this.compiler = compiler;
		//this.symbolTable = Table.rootInstance();
		retValues = null;
		ramSize = 100;
	}

	public int run() {
		lookAhead = scanner.getToken();
		int i = 0;
		// lookAhead would be null if comment opens the program
		while (lookAhead == null) {
			lookAhead = scanner.getToken();
		}
		i = start();
		
		if(i == 1){
			System.out.println("File Parsed successfully!");
		} else {
			System.out.println("File Did Not Parse successfully!");
		}
		if (compiler.checkOK()) {
			System.out.println("File Compiled successfully!");
		} else {
			System.out.println("File Did Not Compile successfully...");
		}
		compiler.writeFile();
		return i;
	}

	private void handleError(boolean matchError, String s) {
		compiler.turnOff();
		if (matchError) {
			parseError = true;
			String errorToken = lookAhead.getLexeme();
			System.out.println("Expected \"" + s + "\" but found \"" + errorToken
					+ "\" on line " + lookAhead.getLineNum() + " in column "
					+ lookAhead.getColNum() + ".");
			// get the next token and keep trying
			// at some point we will have to determine the appropriate method to
			// call after getting the next token
			lookAhead = scanner.getToken();
			while (lookAhead == null){
				lookAhead = scanner.getToken();
			}
		} else {
			if (lookAhead == null) {
				// this happens with a valid comment. we just want the next
				// token and continue
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
	
	private void handleErrorUndefined() {
		compiler.turnOff();
		System.out.println("Error: Unedefined variable \""+lookAhead.getLexeme()+"\" on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+".");
	}
	
	private void handleErrorExpected(String expected, String found) {
		compiler.turnOff();
		System.out.println("Error: Variable \""+lookAhead.getLexeme()+"\" of incorrect type on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+".");
		System.out.println("   Expected type \""+expected+"\" but found type \""+found+"\"");
	}
	
	private void handleErrorGeneral(String description) {
		compiler.turnOff();
		System.out.println("Error on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+": "+description);
	}
	
	private void handleWarningGeneral(String description) {
		System.out.println("Warning` on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+": "+description);
	}
	
	private void invalidVariableName(String var) {
		compiler.turnOff();
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.print("Error on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+":");
		System.out.println("   Variable " + var + " has an invalid name.");
		System.out.println("----------------");
	}
	
	private void undeclaredVariableError(String var) {
		compiler.turnOff();
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.print("Error on line "+lookAhead.getLineNum()+" in column "+lookAhead.getColNum()+":");
		System.out.println("   Variable " + var + " has not been declared in this scope.");
		System.out.println("----------------");
	}

	private void match(String s) {
		if (s.equals(lookAhead.getLexeme())) {
			lookAhead = scanner.getToken();
			while (lookAhead == null) {
				lookAhead = scanner.getToken();
			}
			//lookAhead.describe();
		} else {
			handleError(true, s);
		}
	}
	
	public void setRamSize(int ramSize) {
		this.ramSize = ramSize;
	}

	private int start() {
		switch (lookAhead.getIdentifier()) {
		case "mp_program":
			program();
			match("eof");
			// start register D0 at the end of RAM
			//compiler.move("#"+(ramSize-memorySize), "D0");
			//compiler.injectLast(0);
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

	// David's Section
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

	private void block() {
		switch (lookAhead.getIdentifier()) {
			case "mp_var":
				variableDeclarationPart();
				//break;
			case "mp_procedure":
			case "mp_function":
				procedureAndFunctionDeclarationPart();
			case "mp_begin":
				int size = symbolTable.getSize();
				if (symbolTable.getLevel()>0) {
					size++;
				}
				compiler.add("D"+symbolTable.getLevel(), "#"+size, "SP");
				statementPart();
				break;
			default:
				handleError(false, "Block");
		}
	}

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

	private void procedureAndFunctionDeclarationPart() {
		switch (lookAhead.getIdentifier()) {
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
				handleError(false, "Statement Part");
		}

	}

	private void variableDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				retValues = identifierList();
				match(":");
				type = type();
				ListIterator<String> iter = retValues.listIterator();
				while(iter.hasNext()){
					String name = iter.next();
					if(!symbolTable.inTable(name)){
						symbolTable.insert(name,"value", type, null);
					}else {
						invalidVariableName(name);
					}
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default:
				handleError(false, "Variable Declaration");
		}

	}

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

	private void procedureDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_procedure":
				symbolTable = symbolTable.createScope();
				String endprocedure = compiler.skipLabel();
				compiler.branch(endprocedure);
				procedureHeading();
				match(";");
				block();
				compiler.pop("0(D"+symbolTable.getLevel()+")");
				//compiler.move("D"+symbolTable.getLevel(), "SP");
				compiler.add("D"+symbolTable.getLevel(), "#1", "SP");
				compiler.returnCall();
				compiler.label(endprocedure);
				symbolTable = symbolTable.getParent();
				break;
			default:
				handleError(false, "Procedure Declaration");
		}

	}

	private void functionDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_function":
				symbolTable = symbolTable.createScope();
				String endlabel = compiler.skipLabel();
				compiler.branch(endlabel);
				functionHeading();
				match(";");
				block();
				compiler.pop("0(D"+symbolTable.getLevel()+")");
				compiler.add("D"+symbolTable.getLevel(), "#1", "SP");
				Symbol f = symbolTable.findSymbol(symbolTable.getTitle(),"var");
				Symbol p = symbolTable.getParent().findSymbol(symbolTable.getTitle(),"function");
				if (f==null || p==null) {
					handleErrorGeneral("Function not declared in this scope");
				} else {
					compiler.move(f.getAddress(),p.getAddress());
					compiler.returnCall();
					compiler.label(endlabel);
					symbolTable = symbolTable.getParent();
				}
				break;
			default:
				handleError(false, "Function Declaration");
		}
	}

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

	// Clark's section
	private void valueParameterSection() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // valueParameterSection -> IdentifierList, ":", Type
				retValues = identifierList();
				match(":");
				type =  type();
				ListIterator<String> iter = retValues.listIterator();
				while(iter.hasNext()){
					symbolTable.insert(iter.next(),"value", type, null);
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Value Parameter Section");
		}
	}

	private void variableParameterSection() {
		switch (lookAhead.getIdentifier()) {
			case "mp_var": // variableParameterSection -> "var" ,identifierList,
							// ":", Type
				match("var");
				retValues = identifierList();
				match(":");
				type = type();
				ListIterator<String> iter = retValues.listIterator();
				while(iter.hasNext()){
					symbolTable.insert(iter.next(),"var", type, null);
				}
				retValues.clear();  //clear retValues after it is used each time
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Variable Parameter Section");
		}

	}

	private void compoundStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // compoundStatement -> "begin", statementSequence, "end"
				match("begin");
				statementSequence();
				match("end");
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Compound Statement");
		}

	}

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
	
	private void statementTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_scolon":
				match(";");
				statement();
				statementTail();
				break;
			default:
				break;
		}
	}
	
	private void statement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // statement -> structuredStatement
			case "mp_for": // statement -> structuredStatement
			case "mp_if": // statement -> structuredStatement
			case "mp_repeat": // statement -> structuredStatement
			case "mp_while": // statement -> structuredStatement
				structuredStatement();
				statement();
				break;
			case "mp_read": // statement -> simpleStatement
			case "mp_write": // statement -> simpleStatement
			case "mp_writeln":
			case "mp_identifier": // statement -> simpleStatement
			case "mp_scolon":    // statement -> simpleStatement
				simpleStatement();
				break;
			case "mp_else":
			case "mp_until":
			case "mp_end":
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Statement");
		}
	}

	private void simpleStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_read": // simpleStatement -> readStatement
				readStatement();
				break;
			case "mp_write": // simpleStatement -> writeStatement
			case "mp_writeln":
				writeStatement();
				break;
			case "mp_identifier": // simpleStatement -> assignmentStatement and // procedureStatement if lookAhead is identifier
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
			case "mp_scolon": // simpleStatement -> emptyStatement-- not really sure what  epsilon is yet?!
				emptyStatement();
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Simple Statement");
		}

	}

	private void structuredStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_begin": // structuredStatement -> compoundStatement
				compoundStatement();
				break;
			case "mp_for": // structuredStatement -> repetitiveStatement
			case "mp_repeat": // structuredStatement -> repetitiveStatement
			case "mp_while": // structuredStatement -> repetitiveStatement
				repetitiveStatement();
				break;
			case "mp_if": // structuredStatement -> conditionalStatement
				conditionalStatement();
				break;
			
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Structured Statement");
		}
	}

	private void conditionalStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_if": // conditionalStatement -> ifStatement
				ifStatement();
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Conditional Statement");
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
			handleError(false, "Repetitive Statement");
		}

	}

	private void emptyStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_scolon":
				break;
			default:
				handleError(false, "Empty Statement");
		}

	}

	private void readStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_read": // readStatement -> "read", readParameterList
				match("read");
				readParameterList();
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Read Statement");
		}

	}

	private void writeStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_write": // writeStatement -> "write", writeParameterList
				match("write");
				writeParameterList();
				break;
			case "mp_writeln":
				match("writeln");
				writeParameterList();
				compiler.write("#\"\\n\"");
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Write Statement");
		}

	}

	private void assignmentStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // assignmentStatement -> (Variable|FunctionIdentifier), ":=", expression
				Symbol symbol = symbolTable.findSymbol(lookAhead);
				
				if (symbol==null) {
					handleError(false, "Variable not found");
					break;
				}
				
				if (symbol.getToken()=="var" || symbol.getToken()=="value") {
					variable();
				} else {
					undeclaredVariableError(lookAhead.getLexeme());
					lookAhead = scanner.getToken();
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
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Assignment Statement");
		}
	}

	private void ifStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_if": // ifStatement -> "if", booleanExpression, "then", statement, ["else", statement]
				match("if");
				booleanExpression();
				String elselabel = compiler.skipLabel();
				compiler.branchFalseStack(elselabel);
				match("then");
				statement();
				match(";");
				String endlabel = compiler.skipLabel();
				compiler.branch(endlabel);
				compiler.label(elselabel);
				if (lookAhead.getIdentifier().equals("mp_else")) {
					match("else");
					statement();
					match(";");
				}
				compiler.label(endlabel);
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "If Statement");
		}

	}

	// Jerold's section
	private void repeatStatement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_repeat": // repeatStatement -> "repeat", statementSequence, "until", booleanExpression
			match("repeat");
			String startlabel = compiler.label();
			statement();
			if(lookAhead.getIdentifier().equals("mp_scolon")){
				match(";");
			}
			match("until");
			booleanExpression();
			compiler.branchFalseStack(startlabel);
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, "Repeat Statement");
		}
	}

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
				compiler.branch(startlabel);
				compiler.label(endlabel);
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "While Statement");
		}
	}

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
				compiler.push(s.getAddress());
				compiler.push("#1");
				if (add) {
					compiler.addStack();
				} else {
					compiler.subtractStack();
				}
				compiler.pop(s.getAddress());
				compiler.push(s.getAddress());
				compiler.push("#"+finalvalue);
				if (add) {
					compiler.compareLessEqualStack();
					compiler.branchTrueStack(startlabel);
				} else {
					compiler.compareGreaterEqualStack();
					compiler.branchTrueStack(startlabel);
				}
				compiler.label(endforloop);
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "For Statement");
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

	private SR expression() {
		
		SR sr1, sr2;
		boolean error;
		
		switch (lookAhead.getIdentifier()) {
		
			case "mp_plus":
			case "mp_minus":
			case "mp_integer_lit":
			case "mp_fixed_lit":
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_lparen":
			case "mp_true":
			case "mp_false":
				sr = simpleExpression();
				sr = expression();
				break;
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
			default: // optional case statement proceed citizen...
				break;
			
		}
		
		return sr;
		
	}

	private SR simpleExpression() {
		//optional sign
		boolean negative = false;
		if (lookAhead.getIdentifier()=="mp_plus" || lookAhead.getIdentifier()=="mp_minus") {
			sign();
			if (lookAhead.getIdentifier()=="mp_minus") {
				negative = true;
			}
		}
		switch (lookAhead.getIdentifier()) {
			case "mp_string_lit":
			case "mp_lparen":
			case "mp_not":
			case "mp_true":
			case "mp_false":
			case "mp_identifier":
			case "mp_fixed_lit":
			case "mp_integer_lit":
				sr = term();
				break;
			default:
				handleError(false, "simple Expression");
		}
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
			//case "mp_float_lit":
			case "mp_fixed_lit":
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
		while (lookAhead.getIdentifier().equals("mp_times") || lookAhead.getIdentifier().equals("mp_div") || lookAhead.getIdentifier().equals("mp_mod") || lookAhead.getIdentifier().equals("mp_and")) {
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
				// TODO: see if we can't check for division by zero here
				multiplyingOperator();
				SR sr1 = sr;
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
				//compiler.divideStack();
			} else if (lookAhead.getIdentifier().equals("mp_mod")) {
				multiplyingOperator();
				SR sr1 = sr;
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
				compiler.modulusStack();
			} else if (lookAhead.getIdentifier().equals("mp_and")) {
				multiplyingOperator();
				SR sr1 = sr;
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
		    	// WHAT IS THIS???
				multiplyingOperator();
				factor();
			}
		}
		
		return sr;
	}
	
	private void termTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_plus":
			case "mp_minus":
			case "mp_or":
				addingOperator();
				term();
				termTail();
				break;
			default:
				break;
		}
	}

	private SR factor() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				compiler.push("#"+lookAhead.getLexeme());
				sr = unsignedInteger();
				break;
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s!=null) {
					if(s.getToken()=="var" || s.getToken()=="value"){
						sr = variable();
					} else {
						sr = functionDesignator();
					}
				} else {
					this.undeclaredVariableError(lookAhead.getLexeme());
				}
				break;
			//case "mp_float_lit":
			case "mp_fixed_lit":
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
				sr = factor();
				if (sr.checkBool()) {
					// TODO: check if there is a nand op for this not. Otherwise this sucks. maybe I'm just not thinking about it right.
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
	
	private void factorTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_times":
			case "mp_divide":
			case "mp_divide_float":
			case "mp_mod":
			case "mp_and":
				multiplyingOperator();
				factor();
				factorTail();
				break;
			default:
				break;
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
			handleError(false, "Relational Operator");
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
			case "mp_or": // How is this an adding operator? // Answer: i assume this is bitwise operations
				match("or");
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Adding Operator");
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
			handleError(false, "Multiplying Operator");
		}
	}
	
	private void procedureStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // procedureStatement -> procedureIdentifier, // [actualParameterList]
				passSymbol = symbolTable.findSymbol(lookAhead.getLexeme(), "procedure");
				procedureIdentifier();
				break;	
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Procedure Statement");
		}
		if (lookAhead.getIdentifier().equals("mp_lparen")) {
			// here we pass the symbol
			actualParameterList();
		}
		compiler.call(passSymbol.label);
	}

	private SR functionDesignator() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				passSymbol = symbolTable.findSymbol(lookAhead.getLexeme(), "function");
				sr = functionIdentifier();
				if (lookAhead.getIdentifier().equals("mp_lparen")) {
					actualParameterList();
					// leave space for return value (would ideally be pushing NULL here to assure null return)
					compiler.push("#0");
					compiler.call(passSymbol.label);
					compiler.push(passSymbol.getAddress());
				} else if (lookAhead.getIdentifier().equals("mp_assign")) {
					// assignment for return value
					match(":=");
					SR dst = sr;
					SR src = expression();
					if (src.checkIntlit()) {
						if (dst.checkIntlit()) {
							// int := int
							compiler.pop(passSymbol.getAddress());
						} else if (dst.checkFixedlit()) {
							// float := int
							compiler.castStackFloat();
							compiler.pop(passSymbol.getAddress());
						} else {
							// bool || string (don't allow) := int
							handleErrorGeneral("Invalid type cast to int");
						}
					} else if (src.checkFixedlit()) {
						if (dst.checkIntlit()) {
							// int := float
							handleWarningGeneral("Possible loss of precision, attempt to assign float to int");
							compiler.castStackInteger();
							compiler.pop(passSymbol.getAddress());
						} else if (dst.checkFixedlit()) {
							// float := float
							compiler.pop(passSymbol.getAddress());
						} else {
							// bool || string (don't allow) := int
							handleErrorGeneral("Invalid type cast to float");
						}
					} else {
						// currently no boolean or string keywords
						handleErrorGeneral("Cannot assign type boolean");
					}
					
				}
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Function Designator");
		}
		return sr;
	}

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
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Variable");
		}
		return sr;
	}
	
	private void actualParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				// move DX to the current location of SP
				compiler.move("SP","D"+(symbolTable.getLevel()+1));
				// move SP as activation record increases in size
				compiler.add("SP", "#1", "SP");
				match("(");
				int count = 0;
				String attr[] = passSymbol.getAttribute(count);
				// pass by pointer
				if (attr[0].equals("var")) {
					Symbol s = symbolTable.findSymbol(lookAhead);
					if (s.getToken().equals("value")) {
						if (s.getTypeString().equals(attr[1])) {
							// type matches, pass variable's address
							//compiler.add("D"+s.getLevel(), "#"+s.getOffset(), passSymbol.getAttributeAddress(count));
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
							compiler.move(s.getAddress(), passSymbol.getAttributeAddress(count));
							//compiler.push(s.getAddress());
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
				
				compiler.pop(passSymbol.getAttributeAddress(count));
				while (lookAhead.getIdentifier().equals("mp_comma")) {
					count++;
					match(",");
					// move SP as activation record increases in size
					compiler.add("SP", "#1", "SP");
					attr = passSymbol.getAttribute(count);
					// pass by pointer
					if (attr[0].equals("var")) {
						Symbol s = symbolTable.findSymbol(lookAhead);
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
								compiler.move(s.getAddress(), passSymbol.getAttributeAddress(count));
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
					compiler.pop(passSymbol.getAttributeAddress(count));
				}
				match(")");
				break;
			default:
				handleError(false, "Actual Parameter List");
		}
	}

	private void actualParameter() {
		switch (lookAhead.getIdentifier()) {
			case "not":
			case "mp_identifier":
			case "mp_integer_lit":
			case "mp_fixed_lit":
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

	private void readParameter() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s.getToken().equals("var") || s.getToken().equals("value")) {
					if (s.type=="integer") {
						compiler.readInt(s.getAddress());
					} else if (s.type=="float") {
						compiler.readFloat(s.getAddress());
					}
					variable();
				} else {
					undeclaredVariableError(lookAhead.getLexeme());
				}
				break;
			default:
				handleError(false, "Read Parameter");
		}
	}

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

	private void writeParameter() {
		
		switch (lookAhead.getIdentifier()) {
			case "mp_string_lit":
				//compiler.write("#\""+lookAhead.getLexeme()+"\"");
				sr = expression();
				break;
			case "mp_fixed_lit":
			case "mp_integer_lit":
				sr = expression();
				break;
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
				handleError(false, "Boolean Expression");
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
				handleError(false, "Ordinal Expression");
		}
	}

	private SR variableIdentifier() {
		SR sr = null;
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_integer_lit":
			case "mp_fixed_lit":
				sr = identifier();
				break;
			default:
				handleError(false, "Variable Identifier");
		}
		return sr;
	}

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
				sr = SR.fixedlit();
				match(lookAhead.getLexeme());
				break;
			default:
				handleError(false, "Identifier");
		}
		return sr;
	}

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

	private boolean letter(char letter) {
		boolean found = false;
		char letters[] = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
				'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
				'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
				'U', 'V', 'W', 'X', 'Y', 'Z' };
		for (int j = 0; j < letters.length; j++) {
			if (letter == letters[j]) {
				found = true;
				break;
			}
		}
		if (!found)
			handleError(true, "letter");
		return found;
	}

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
