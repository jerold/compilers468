public class Scanner {
	private static FilePointer fp;
	private static String[] resWords;
	private boolean error;
	//didn't need this with the solution David and Clark came up with
	//private boolean warning;
	private static boolean recCall;  //flag for recursive call in findLexemeString()
	public Scanner() {
		// array containing the reserved words of the language
		resWords = new String[] { "and", "begin", "div", "do", "downto",
				"else", "end", "fixed", "float", "for", "function", "if",
				"integer", "mod", "not", "or", "procedure",
				"program", "read", "repeat", "then", "to", "until", "var",
				"while", "write", "writeln", "true", "false", "string", "boolean", 
				"type", "vector", "array", "..", "of" };
	}

	public void openFile(String fileIn) {
		fp = new FilePointer(fileIn);
	}
	
	public FilePointer getFP(){
		return fp;
	}

	// Driver which skips white space and then kicks off the right lexeme parser
	// a Token is the constructed from the lexeme and returned to the driver
	public Token getToken() {
		//set error flag to false
		error = false;
		//warning = false;
		// skip white space
		fp.skipWhiteSpace();

		char nextChar = fp.peekNext();
		// System.out.println("Next Char [" + nextChar + "]");

		String lexeme = null;
		String id = null;
		int lineNumber = fp.getLineNumber();
		int columnNumber = fp.getColumnNumber();
		// Single char length symbols can all be matched
		// with fetchLexemeSymbol()
		switch (nextChar) {
			case '[':
				lexeme = fetchLexemeSymbol();
				id = "mp_lbracket";
				break;
			case ']':
				lexeme = fetchLexemeSymbol();
				id = "mp_rbracket";
				break;
			case '.':
				lexeme = fetchLexemePeriod();
				if (lexeme.length() == 1) {
					id = "mp_period";
				} else {
					id = "mp_span";
				}
				break;
			case ',':
				lexeme = fetchLexemeSymbol();
				id = "mp_comma";
				break;
			case '(':
				lexeme = fetchLexemeSymbol();
				id = "mp_lparen";
				break;
			case ')':
				lexeme = fetchLexemeSymbol();
				id = "mp_rparen";
				break;
			case ';':
				lexeme = fetchLexemeSymbol();
				id = "mp_scolon";
				break;
			case '=':
				lexeme = fetchLexemeSymbol();
				id = "mp_equal";
				break;
			case '+':
				lexeme = fetchLexemeSymbol();
				id = "mp_plus";
				break;
			case '-':
				lexeme = fetchLexemeSymbol();
				id = "mp_minus";
				break;
			case '*':
				lexeme = fetchLexemeSymbol();
				id = "mp_times";
				break;
			case ':':
				lexeme = fetchLexemeColon();
				if (lexeme.length() == 1) {
					id = "mp_colon";
				} else {
					id = "mp_assign";
				}
				break;
			case '>':
				lexeme = fetchLexemeCloseCarrot();
				if (lexeme.length() == 1) {
					id = "mp_gthan";
				} else {
					id = "mp_gequal";
				}
				break;
			case '<':
				lexeme = fetchLexemeOpenCarrot();
				if (lexeme.length() == 1) {
					id = "mp_lthan";
				} else if (lexeme.endsWith("=")) {
					id = "mp_lequal";
				} else if (lexeme.endsWith(">")) {
					id = "mp_nequal";
				}
				break;
			// handles a comment
			case '{':
				// nextChar = fp.getNext();
				lexeme = fetchLexemeComment();
				// should return a warning vs an error in the future, now just an error
				if (error) {
					id = "mp_run_comment";
				}
				//now handle this in the fetchLexemeComment() function to print the error directly
				//} else if (warning) {
				//	id = "mp_run_comment";
					
				//}
				break;
			case '}':
				lexeme = "" + fp.getNext();
				id = "mp_error";
				break;
			case '/':
				lexeme = fetchLexemeSymbol();
				id = "mp_divide_float";
				break;
			case '\'':
				recCall = false;
				lexeme = fetchLexemeString();
				id = "mp_string_lit";
				if (error) {
					id = "mp_run_string";
				}
				if(recCall){
					lexeme = lexeme.substring(1, lexeme.length() - 1);
				}
				fp.peekColumn--;
				break;
			case '\u0000':
				lexeme = "eof";
				id = "mp_eof";
				break;
			case 'a':
			case 'b': 
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case '_':
				lexeme = fetchLexemeIdentifier();
	            if(identifyResWord(lexeme) >= 0){
	                id = "mp_" + lexeme;
	            } else{
	                id = "mp_identifier";
	            }
	            if (error){
					id = "mp_error";
				}
	            break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				lexeme = fetchLexemeNumber();
				if (lexeme.contains(".")) {
					id = "mp_fixed_lit";
					if (lexeme.contains("E") || lexeme.contains("e")) {
						id = "mp_float_lit";
					}
				} else {
					id = "mp_integer_lit";
					if (lexeme.contains("E") || lexeme.contains("e")) {
						id = "mp_float_lit";
					}
				}
				break;
			default:
				// This is very strange...
				//package the next char(not valid in the language) as an error token and 
				//pass it to the parser
				lexeme = "" + fp.getNext();
				id = "mp_error";
				break;
		}

		// clear whitespace after token (covers us in the event a
		// file ends with white space the driver is made aware of
		// EOF sooner
		fp.skipWhiteSpace();

		// build token from returned lexeme
		Token t = null;
		if (lexeme != null && lexeme.length() > 0)
			t = new Token(id, lineNumber, columnNumber, lexeme);
		return t;
	}

	// Mystery Method
	public boolean endOfFile() {
		return fp.endOfFile();
	}

	public String fetchLexemeSymbol() {
		String lex = "" + fp.getNext();
		// System.out.print("fetchLexemeSymbol    :  ");
		return lex;
	}

	public String fetchLexemeOpenCarrot() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '=') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else if (newChar == '>') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeOpenCarrot:  ");
		return lex;
	}

	public String fetchLexemeCloseCarrot() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '=') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeCloseCarrot:  ");
		return lex;
	}
	
	public String fetchLexemePeriod() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '.') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeColonOrAssignment:  ");
		return lex;
	}

	public String fetchLexemeColon() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '=') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeColonOrAssignment:  ");
		return lex;
	}

	public String fetchLexemeIdentifier() {
		String lex = "" + fp.getNext();
		boolean sameToken = true;
		while (sameToken) {
			char newChar = fp.peekNext();
			//check for two underscores in a row
            if (lex.endsWith("_") && newChar == '_'){
                //System.out.print("This is not a valid identifier.");
            	error = true;
            }
			if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
					.contains("" + newChar)) {
				newChar = fp.getNext();
				lex = lex + newChar;
			} else {
				fp.setPeekToBufferColumn();
				sameToken = false;
			}
		}
		if (lex.endsWith("_")){
			error = true;
		}
		// System.out.print("fetchLexemeIdentifier:  ");
		// send all identifiers to the parser in lower case
		//because language is case insensitive
		return lex.toLowerCase();
	}

	public String fetchLexemeNumber() {
		String lex = "" + fp.getNext();
		boolean sameToken = true;
		boolean decimalPointUsed = false;
		while (sameToken) {
			char newChar = fp.peekNext();
			if ("0123456789.".contains("" + newChar)) {
				if (newChar == '.') {
					if (decimalPointUsed) {
						fp.setPeekToBufferColumn();
						sameToken = false;
					} else {
						newChar = fp.getNext();
						lex = lex + newChar;
						decimalPointUsed = true;
					}
				} else {
					newChar = fp.getNext();
					lex = lex + newChar;
				}
			} else {
				if (newChar == 'E' || newChar == 'e') {
					lex = fetchLexemeFloatLit(lex);
				}
				fp.setPeekToBufferColumn();
				sameToken = false;
			}
		}
		// System.out.print("fetchLexemeInteger:  ");
		return lex;
	}

	public String fetchLexemeFloatLit(String leftSide) {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		// a [-|+] must follow after the [e|E]
		if (newChar == '+' || newChar == '-') {
			newChar = fp.getNext();
			lex = lex + newChar;
			newChar = fp.peekNext();
		}
		// A digit must follow after the [e|E][-|+]
		if (!"0123456789".contains("" + newChar)) {
			fp.backUp(lex.length());
			return leftSide;
		} else {
			newChar = fp.getNext();
			lex = lex + newChar;
		}
		boolean sameToken = true;
		while (sameToken) {
			newChar = fp.peekNext();
			if ("0123456789".contains("" + newChar)) {
				newChar = fp.getNext();
				lex = lex + newChar;
			} else {
				fp.setPeekToBufferColumn();
				sameToken = false;
			}
		}
		return leftSide + lex;
	}

	public int identifyResWord(String s) {
		for (int i = 0; i < resWords.length; i++) {
			if (s.toLowerCase().compareTo(resWords[i]) == 0) {
				return i;
			}
		}
		return -1;
	}
	/*
	 * Strings now handled. There are three cases handled here
	 * 1--the string is legal and is a string_lit
	 * 2--the string is legal with escape char used if apostrophe used in string
	 * 	  the function is recursive if this is the case
	 * 3--the escape char was not used if apostrophe used in string
	 * 	  in this case, the apostrophe is used to close the string, creating a string_lit
	 * 	  the remainder of the string is scanned as if not a string and the parser deals with
	 *    any tokens sent to it(most likely not valid) when the intended string closing apostrophe 
	 *    is reached end of line will be reached and run_string token will be sent
	 *    this case is now the first line in testFile.txt
	 */
	public String fetchLexemeString() {
		String lex = "" + fp.getNext();
		while (fp.peekNext() != '\'') {
			if (fp.endOfLine() || fp.endOfFile()) {
				error = true;
				return lex.substring(1,lex.length() - 1);
			} 
			lex = lex + fp.getNext();
		}
		
		// this handles the escape char '' inside a string.
		lex = lex + fp.getNext();
		if (fp.peekNext() == '\'') {
			recCall = true;		//set recCall to true if a recursive call is made here
			lex = lex + fetchLexemeString().substring(1);  //the .substring(1) peels off the escape char'
		} 
		if(recCall) {
			return lex; //this is the proper return if recursive call was made
		} else {
			//this is the proper return if a recursive call was not made
			return lex.substring(1,lex.length() - 1);
		}
	}
	
	public String fetchLexemeComment() {
		String lex = "" + fp.getNext();
		while (fp.peekNext() != '}') {
			if (fp.peekNext() == '{') {
				//comments can be on multiple lines but perhaps
				//the comment was not closed properly
				//warning = true;
				System.out.println("WARNING: Found '{' inside a comment. ");
				System.out.println("In line " + fp.getLineNumber() + " column " + fp.getColumnNumber());
				//return lex.substring(0,lex.length()-1);
				fp.setPeekToBufferColumn();
			} else if (fp.endOfFile()) {
				error = true;
				return lex.substring(0,lex.length()-1);
			}
			lex = lex + fp.getNext();
		}
		fp.getNext();
		//fp.getNext();
		return null;
	}
	
}
