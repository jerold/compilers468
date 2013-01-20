public class Scanner {
	private static FilePointer fp;
	private static String[] resWords;

	public Scanner() {
		// We don't do anything here
		resWords = new String[] { "and", "begin", "div", "do", "downto",
				"else", "end", "fixed", "float", "for", "function", "if",
				"integer", "mod", "not", "or", "procedure", "procedure",
				"program", "read", "repeat", "then", "to", "until", "var",
				"while", "write", "writeln" };
	}

	public void openFile(String fileIn) {
		fp = new FilePointer(fileIn);
	}

	// Driver which skips white space and then kicks off the right lexeme parser
	// a Token is the constructed from the lexeme and returned to the driver
	public Token getToken() {
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
		case '.':
			lexeme = fetchLexemeSymbol();
			id = "mp_period";
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
			while (fp.peekNext() != '}') {
				fp.getNext();
			}
			break;
		case '\'':
			lexeme = fetchLexemeString();
			id = "mp_String_lit";
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
			fp.getNext();
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
                System.out.print("This is not a valid identifier.");
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
		// System.out.print("fetchLexemeIdentifier:  ");
		return lex;
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

	// I am a God. mp_float_lit in the bag
	public String fetchLexemeFloatLit(String leftSide) {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		// a [-|+] must follow after the [e|E]
		if (newChar == '+' || newChar == '-') {
			newChar = fp.getNext();
			lex = lex + newChar;
			newChar = fp.peekNext();
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
		} else {
			fp.backUp(lex.length());
			return leftSide;
		}
		// System.out.print("fetchLexemeFloatLit:  ");
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

	public String fetchLexemeString() {
		String lex = "" + fp.getNext();
		// char newChar = fp.peekNext();
		while (fp.peekNext() != '\'') {
			lex = lex + fp.getNext();
		}
		lex = lex + fp.getNext();
		return lex;
	}
}
