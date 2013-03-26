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
	
	public Parser(Scanner scanner, Compiler compiler) {
		this.scanner = scanner;
		this.compiler = compiler;
		//this.symbolTable = Table.rootInstance();
		retValues = null;
		ramSize = 100;
	}

	public int run() {
		// while (!scanner.endOfFile()) {
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
	
	private void invalidVariableName(String var){
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.println("Variable " + var + " has already been declared in this scope.");
		System.out.println("----------------");
	}
	
	private void undeclaredVariableError(String var){
		parseError = true;
		System.out.println();
		System.out.println("----------------");
		System.out.println("Variable " + var + " has not been declared in this scope.");
		System.out.println("----------------");
	}

	private void match(String s) {
		if (s.equals(lookAhead.getLexeme())) {
			//lookAhead.describe();
			lookAhead = scanner.getToken();
			while (lookAhead == null) {
				lookAhead = scanner.getToken();
			}
		} else {
			handleError(true, s);
		}
	}
	
	private void computeMemorySize(Table outerTable) {
		int newsize = symbolTable.getOffset()+symbolTable.getSize();
		if (newsize > memorySize)
			memorySize = newsize;
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
			//this is done at the end now
			compiler.move("#100", "D0");
			symbolTable.setTitle(lookAhead.getLexeme());
			identifier();
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
			//compiler.move("#"+symbolTable.getOffset(),"D"+symbolTable.getLevel());
			procedureAndFunctionDeclarationPart();
			break;
		case "mp_begin":
			//compiler.move("#"+symbolTable.getOffset(),"D"+symbolTable.getLevel());
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
			case "mp_begin":
				statementPart();
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
				if(!symbolTable.inTable(name, "var")){
					symbolTable.insert(name,"var", type, null);
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
			default:
				handleError(false, "Type");
				return null;
		}

	}

	private void procedureDeclaration() {
		switch (lookAhead.getIdentifier()) {
			case "mp_procedure":
				symbolTable = symbolTable.createScope();
				compiler.branch(compiler.getLabel(1));
				procedureHeading();
				match(";");
				block();
				compiler.returnCall();
				compiler.label();
				computeMemorySize(symbolTable);
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
				// put the return value on the top of the stack
				Symbol f = symbolTable.findSymbol(symbolTable.getTitle(),"var");
				//compiler.push(f.getAddress());
				Symbol p = symbolTable.getParent().findSymbol(symbolTable.getTitle(),"function");
				compiler.move(f.getAddress(),p.getAddress());
				compiler.returnCall();
				compiler.label(endlabel);
				computeMemorySize(symbolTable);
				symbolTable = symbolTable.getParent();
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
				identifier();
				if (lookAhead.getIdentifier().equals("mp_lparen")) {
					formalParameterList();
				}
				Symbol s = symbolTable.getParent().insert(symbolTable.getTitle(), "procedure", "no return", getAttributes());
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
			identifier();
			if (lookAhead.getIdentifier().equals("mp_lparen")) {
				formalParameterList();
			}
			match(":");
			type = type();
			Symbol s = symbolTable.getParent().insert(symbolTable.getTitle(), "function", "returns " + type, getAttributes());
			s.label = label;
			s.level = symbolTable.getLevel();
			symbolTable.insert(symbolTable.getTitle(), "var", type, null);
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
			//symbolTable.describe();
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
			//symbolTable.describe();
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
		/*
		//recursive here
		if (lookAhead.getIdentifier().equals("mp_scolon")) {
			match(";");
			statementSequence();
		}
		*/
	}
	
	private void statementTail() {
		switch (lookAhead.getIdentifier()) {
			case "mp_scolon":
				match(";");
				statement();
				statementTail();
				break;
			case "mp_else":
			case "mp_until":
			case "mp_end":
				break;
			default:
				handleError(false, "Statement Tail");
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

	/*
	 * Should be able to begin and end structured statements and keep going
	 */
	/*
	private void statement() {
		switch (lookAhead.getIdentifier()) {
		case "mp_begin": // statement -> structuredStatement
		case "mp_for": // statement -> structuredStatement
		case "mp_if": // statement -> structuredStatement
		case "mp_repeat": // statement -> structuredStatement
		case "mp_while": // statement -> structuredStatement
			structuredStatement();
			break;
		case "mp_read": // statement -> simpleStatement
		case "mp_write": // statement -> simpleStatement
		case "mp_identifier": // statement -> simpleStatement
		case "mp_scolon":    // statement -> simpleStatement
			simpleStatement();
			//match(";");
			break;
		case "mp_else":
		case "mp_end":
		case "mp_until":
			//do nothing
			break;  //this is end the statement calls without an error when reaching an else or until statement
		default: // default case is an invalid lookAhead token in language
			handleError(false, "Statement");
		}

	}
	*/

	private void simpleStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_read": // simpleStatement -> readStatement
				readStatement();
				break;
			case "mp_write": // simpleStatement -> writeStatement
			case "mp_writeln":
				writeStatement();
				break;
			// seems ambiguous here???
			case "mp_identifier": // simpleStatement -> assignmentStatement and // procedureStatement if lookAhead is identifier
				Symbol s = symbolTable.findSymbol(lookAhead);
				if (s.token=="procedure") {
					procedureStatement();
				} else {
					assignmentStatement();
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

	// this seems ambiguous also??
	private void assignmentStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier": // assignmentStatement -> (Variable|FunctionIdentifier), ":=", expression
				Symbol symbol = null;
				if(symbolTable.inTable(lookAhead.getLexeme(), "var")){
					symbol = symbolTable.findSymbol(lookAhead.getLexeme(), "var");
					variable();
				} if (symbolTable.inTable(lookAhead.getLexeme(), "function")) {
					undeclaredVariableError(lookAhead.getLexeme());
					lookAhead = scanner.getToken();
				}
				match(":=");
				expression();
				if(symbol!=null) {
					compiler.pop(symbol.getAddress());
				}
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Assignment Statement");
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
			actualParameterList();
		}
		compiler.call(passSymbol.label);
	}

	// TODO: This if statement is consuming a semicolon and breaking the parse
	private void ifStatement() {
		switch (lookAhead.getIdentifier()) {
			case "mp_if": // ifStatement -> "if", booleanExpression, "then", statement, ["else", statement]
				match("if");
				booleanExpression();
				String elselabel = compiler.skipLabel();
				compiler.branchFalseStack(elselabel);
				match("then");
				// this is a hack to consume ; if it's a single statement
				boolean checkSequence = (lookAhead.getIdentifier().equals("mp_begin"));
				statement();
				if (!checkSequence) match(";");
				String endlabel = compiler.skipLabel();
				compiler.branch(endlabel);
				compiler.label(elselabel);
				if (lookAhead.getIdentifier().equals("mp_else")) {
					match("else");
					statement();
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
		case "mp_repeat": // repeatStatement -> "repeat", statementSequence,
							// "until", booleanExpression
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
			case "mp_for": // forStatement -> "for", controlVariable, ":=", initialValue, ("to"|"downto"), finalVariable, "do", statement
				match("for");
				Symbol s = symbolTable.findSymbol(lookAhead.getLexeme(),"var");
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
				finalValue();
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
				// TODO: decrement SP here so the final value is removed
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

	private void expression() {
		
		switch (lookAhead.getIdentifier()) {
		
			case "mp_plus":
			case "mp_minus":
			case "mp_integer_lit":
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_lparen":
				simpleExpression();
				expression();
				break;
			case "mp_not":
				simpleExpression();
				expression();
				compiler.compareNotEqualStack();
				break;
			case "mp_equal":
				relationalOperator();
				simpleExpression();
				compiler.compareEqualStack();
				break;
			case "mp_lthan":
				relationalOperator();
				simpleExpression();
				compiler.compareLessStack();
				break;
			case "mp_gthan":
				relationalOperator();
				simpleExpression();
				compiler.compareGreaterStack();
				break;
			case "mp_lequal":
				relationalOperator();
				simpleExpression();
				compiler.compareLessEqualStack();
				break;
			case "mp_gequal":
				relationalOperator();
				simpleExpression();
				compiler.compareGreaterEqualStack();
				break;
			case "mp_nequal":
				relationalOperator();
				simpleExpression();
				compiler.compareNotEqualStack();
				break;
			default: // optional case statement proceed citizen...
				break;
			
		}
		
	}

	private void simpleExpression() {
		switch (lookAhead.getIdentifier()) {
			case "mp_plus":
			case "mp_minus":
				sign();
				break;
			case "mp_identifier":
			case "mp_fixed_lit":
			case "mp_integer_lit":
			case "mp_string_lit":
			case "mp_lparen":
				term();
				break;
			default:
				handleError(false, "simple Expression");
		}
		while (lookAhead.getIdentifier().equals("mp_plus") || lookAhead.getIdentifier().equals("mp_minus") || lookAhead.getIdentifier().equals("mp_or")) {
			if (lookAhead.getIdentifier().equals("mp_plus")) {
				addingOperator();
				term();
				compiler.addStack();
			} else if (lookAhead.getIdentifier().equals("mp_minus")) {
				addingOperator();
				term();
				compiler.subtractStack();
			} else {
				addingOperator();
				term();
			}
		}
	}

	private void term() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				Symbol s = symbolTable.findSymbol(lookAhead.getLexeme(),"var");
				if (s==null) {
					s = symbolTable.findSymbol(lookAhead.getLexeme(),"value");
				}
				if (s==null) {
					s = symbolTable.findSymbol(lookAhead.getLexeme(),"function");
				}
				if (s!=null) {
					compiler.push(s.getAddress());
				} else {
					symbolTable.describe();
					handleErrorUndefined();
				}
				factor();
				break;
			//case "mp_float_lit":
			case "mp_fixed_lit":
			case "mp_integer_lit":
			case "mp_string_lit":
			case "mp_lparen":
				factor();
				break;
			default:
				handleError(false, "term");
		}
		while (lookAhead.getIdentifier().equals("mp_times") || lookAhead.getIdentifier().equals("mp_div") || lookAhead.getIdentifier().equals("mp_mod") || lookAhead.getIdentifier().equals("mp_and")) {
			if (lookAhead.getIdentifier().equals("mp_times")) {
				multiplyingOperator();
				factor();
				compiler.multiplyStack();
			} else if (lookAhead.getIdentifier().equals("mp_div")) {
				multiplyingOperator();
				factor();
				compiler.divideStack();
			} else if (lookAhead.getIdentifier().equals("mp_mod")) {
				multiplyingOperator();
				factor();
				compiler.modulusStack();
			} else {
				multiplyingOperator();
				factor();
			}
		}
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

	// this is not LL1 for some reason??!!
	private void factor() {
		switch (lookAhead.getIdentifier()) {
			case "mp_integer_lit":
				compiler.push("#"+lookAhead.getLexeme());
				unsignedInteger();
				break;
			case "mp_identifier":
				// TODO: I didn't know what to do for value types, so I just made it go to variable() as well
				if(symbolTable.inTable(lookAhead.getLexeme(), "var")){
					variable();
				} else if (symbolTable.inTable(lookAhead.getLexeme(), "value")) {
					variable();
				} else {
					functionDesignator();
				}
				break;
			//case "mp_float_lit":
			case "mp_fixed_lit":
				compiler.push("#"+lookAhead.getLexeme());
				identifier();
				break;
			case "mp_string_lit":
				//compiler.push("#\""+lookAhead.getLexeme()+"\"");
				identifier();
				break;
			case "mp_lparen":
				match("(");
				expression();
				match(")");
				break;
			case "mp_not":
				// recurses!
				match("not");
				factor();
				break;
			default:
				handleError(false, "Factor");
		}

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

	private void functionDesignator() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				passSymbol = symbolTable.findSymbol(lookAhead.getLexeme(), "function");
				functionIdentifier();
				if (lookAhead.getIdentifier().equals("mp_lparen")) {
					actualParameterList();
				}
				compiler.call(passSymbol.label);
				compiler.push(passSymbol.getAddress());
				break;
			default: // default case is an invalid lookAhead token in language
				handleError(false, "Function Designator");
		}
	}

	private void variable() {
		switch (lookAhead.getIdentifier()) {
		case "mp_identifier":
		case "mp_string_lit":
		case "mp_int_lit":
		case "mp_fixed_lit":
		case "mp_float_lit":
			variableIdentifier();
			break;
		default: // default case is an invalid lookAhead token in language
			handleError(false, "Variable");
		}
	}

	// Logan's section
	private void actualParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_lparen":
				compiler.move("#"+(symbolTable.getOffset()+symbolTable.getSize()),"D"+(symbolTable.getLevel()+1));
				match("(");
				// should check types against symbol table here
				int count = 0;
				actualParameter();
				compiler.pop(passSymbol.getAttributeAddress(count));
				while (lookAhead.getIdentifier().equals(",")) {
					count++;
					match(",");
					actualParameter();
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
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
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
				while (lookAhead.getIdentifier().equals(",")) {
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
				Symbol s = symbolTable.findSymbol(lookAhead.getLexeme(),"var");
				if (s.type=="integer")
					compiler.readInt(s.getAddress());
				else if (s.type=="float")
					compiler.readFloat(s.getAddress());
				variable();
				break;
			default:
				handleError(false, "Read Parameter");
		}
	}

	private void writeParameterList() {
		switch (lookAhead.getIdentifier()) {
			case "mp_scolon":
				// this should be write line in the future i think, but i was having problems
				compiler.write("#\"\\n\"");
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
		// I'm thinking this can all be done with a write stack at the end, since expression will put the correct element on top
		switch (lookAhead.getIdentifier()) {
			case "mp_string_lit":
				compiler.write("#\""+lookAhead.getLexeme()+"\"");
				expression();
				break;
			case "mp_integer_lit":
				compiler.write("#"+lookAhead.getLexeme());
				expression();
				break;
			case "mp_float_lit":
				compiler.write("#"+lookAhead.getLexeme());
				expression();
				break;
			case "mp_identifier":
			case "not":
			case "mp_lparen":
			case "mp_plus":
			case "mp_minus":
				expression();
				compiler.writeStack();
				break;
			default:
				handleError(false, "Write");
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

	private void variableIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_integer_lit":
			case "mp_fixed_lit":
			//case "mp_float_lit":
				identifier();
				break;
			default:
				handleError(false, "Variable Identifier");
		}
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

	private void functionIdentifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
				if(symbolTable.inTable(lookAhead.getLexeme(), "function"))
					identifier();
				break;
			default:
				handleError(false, "Function Identifier");
		}
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
			//break;
			return varLexemes;
		default:
			handleError(false, "Identifier List");
			return null;
		}
	}

	private void identifier() {
		switch (lookAhead.getIdentifier()) {
			case "mp_identifier":
			case "mp_string_lit":
			case "mp_int_lit":
			case "mp_fixed_lit":
				match(lookAhead.getLexeme());
				break;
			//case "mp_float_lit":
			default:
				handleError(false, "Identifier");
		}
	}

	private void unsignedInteger() {
		switch (lookAhead.getIdentifier()) {
		case "mp_integer_lit":
			digitSequence();
			break;
		default:
			handleError(false, "Unsigned Integer");
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
			handleError(false, "Sign");
		}
	}

	private void under() {
		match("_");
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
