
public class Scanner {
	private static FilePointer fp;
	
	public Scanner() {
		// We don't do anything here
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
		switch(nextChar) {
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
			case ':':
				lexeme = fetchLexemeColon();
				if (lexeme.length()==1){
					id = "mp_colon";
				} else {
					id = "mp_assign";
				}
				break;
			case '+':
				lexeme = fetchLexemePlusOperator();
				if (lexeme.length()==1){
					id = "mp_plus";
				} else {
					id = "mp_aassign";
				}
				break;
			case '-':
				lexeme = fetchLexemeMinusOperator();
				if (lexeme.length()==1){
					id = "mp_minus";
				} else {
					id = "mp_sassign";
				}
				break;
			case '*':
				lexeme = fetchLexemeMultiplyOperator();
				if (lexeme.length()==1){
					id = "mp_times";
				} else {
					id = "mp_massign";
				}
				break;
			case '/':
				lexeme = fetchLexemeDivideOperator();
				if (lexeme.length()==1){
					id = "mp_divide";
				} else {
					id = "mp_dassign";
				}
				break;
			case '<':
				lexeme = fetchLexemeOpenCarrot();
				if (lexeme.length()==1){
					id = "mp_lthan";
				} else if (lexeme.endsWith("=")){
					id = "mp_lequal";
				}
				break;
			case '>':
				lexeme = fetchLexemeCloseCarrot();
				if (lexeme.length()==1){
					id = "mp_gthan";
				} else {
					id = "mp_gequal";
				}
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
				lexeme = fetchLexemeIdentifier();
				id = "mp_identifier";
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
				lexeme = fetchLexemeInteger();
				id = "mp_integer_lit";
				break;
			default:
				// System.out.print("DEFAULT");
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
	public boolean endOfFile() {
		return fp.endOfFile();
	}
	
	public String fetchLexemeSymbol() {
		String lex = "" + fp.getNext();
		// System.out.print("fetchLexemeSymbol    :  ");
		return lex;
	}
//	// I like this idea but the doubleSymbol() would match many combinations
//	// that aren't valid ")>" ".:" ...
//	private boolean doubleSymbol(char c){
//		if(c == '>' || c == '<' || c == ':'){
//			char nextChar = fp.peekNext();
//			if (nextChar ==  '=' || nextChar ==  '>'){
//				return true;
//			}
//		}
//		return false;
//	}
	public String fetchLexemeOpenParen() {
		String lex = "" + fp.getNext();
		// System.out.print("fetchLexemeOpenParen:  ");
		return lex;
	}
	public String fetchLexemeCloseParen() {
		String lex = "" + fp.getNext();
		// System.out.print("fetchLexemeCloseParen:  ");
		return lex;
	}
	public String fetchLexemeOpenCarrot() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '=') {
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
	public String fetchLexemeSemiColon() {
		String lex = "" + fp.getNext();
		// System.out.print("fetchLexemeSemiColon:  ");
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
			switch(newChar) {
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
					newChar = fp.getNext();
					lex = lex + newChar;
					break;
				default:
					fp.setPeekToBufferColumn();
					sameToken = false;
					break;
			}
		}
		// System.out.print("fetchLexemeIdentifier:  ");
		return lex;
	}
	public String fetchLexemeInteger() {
		String lex = new String();
		boolean sameToken = true;
		while (sameToken) {
			char newChar = fp.peekNext();
			switch(newChar) {
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
					newChar = fp.getNext();
					lex = lex + newChar;
					break;
				default:
					fp.setPeekToBufferColumn();
					sameToken = false;
					break;
			}
		}
		// System.out.print("fetchLexemeInteger:  ");
		return lex;
	}
	public String fetchLexemePlusOperator() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '+') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else if (newChar == '=') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeColonOrAssignment:  ");
		return lex;
	}
	public String fetchLexemeMinusOperator() {
		String lex = "" + fp.getNext();
		char newChar = fp.peekNext();
		if (newChar == '-') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else if (newChar == '=') {
			newChar = fp.getNext();
			lex = lex + newChar;
		} else {
			fp.setPeekToBufferColumn();
		}
		// System.out.print("fetchLexemeColonOrAssignment:  ");
		return lex;
	}
	public String fetchLexemeMultiplyOperator() {
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
	public String fetchLexemeDivideOperator() {
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
}
