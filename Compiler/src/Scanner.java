
public class Scanner {
	private static FilePointer fp;
	
	public Scanner() {
		// We don't do anything here
	}
	
	public void openFile(String fileIn) {
		fp = new FilePointer(fileIn);
		//fp = new FilePointer("src/testFile.txt");
	}
	
	public void test() {
		// An example of how to use the FilePointer
		char newChar = fp.getNext();
		System.out.print(newChar);
		
		// peekNext should be used by the dispatcher
		newChar = fp.peekNext();
		System.out.print("[" + newChar + "]");
		
		newChar = fp.peekNext();
		System.out.print("[" + newChar + "]");
				
		// getNext should be used by the tokenizers
		newChar = fp.getNext();
		System.out.println(newChar);
		
		// Backup will only back up as far as the 0'th char within the current line
		fp.backUp(2);
		
		while (!fp.endOfFile()) {
			newChar = fp.getNext();
			System.out.print(newChar);
		}
		System.out.println("");
	}
	
	// Driver which skips white space and then kicks off the right lexeme parser
	// a Token is the constructed from the lexeme and returned to the driver
	public Token getToken() {
		// skip white space
		fp.skipWhiteSpace();
		
		char nextChar = fp.peekNext();
		String lexeme = null;
		int lineNumber = fp.getLineNumber();
		int columnNumber = fp.getColumnNumber();
		Token t;
		// System.out.println("Next Char [" + nextChar + "]");
		switch(nextChar) {
		//the following cases are all symbols and call the 
		//fetchLexemeSymbol method
			case '.':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_period", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case ',':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_comma", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case '(':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_lparen", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case ')':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_rparen", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case ';':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_scolon", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case '=':	
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_equal", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case ':':
				lexeme = fetchLexemeSymbol();
				if (lexeme.length()==1){
					t = new Token("mp_colon", lineNumber, columnNumber, lexeme);
					//return t;
				} else {
					t = new Token("mp_assign", lineNumber, columnNumber, lexeme);
					//return t;
				}
				break;
			case '>':
				lexeme = fetchLexemeSymbol();
				if (lexeme.length()==1){
					t = new Token("mp_gthan", lineNumber, columnNumber, lexeme);
					//return t;
				} else {
					t = new Token("mp_gequal", lineNumber, columnNumber, lexeme);
					//return t;
				}
				break;
			case '<':
				lexeme = fetchLexemeSymbol();
				if (lexeme.length()==1){
					t = new Token("mp_lthan", lineNumber, columnNumber, lexeme);
					//return t;
				} else if (lexeme.endsWith("=")){
					t = new Token("mp_leqaul", lineNumber, columnNumber, lexeme);
					//return t;
				} else {
					t = new Token("mp_neqaul", lineNumber, columnNumber, lexeme);
					//return t;
				}
				break;
			case '+':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_plus", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case '-':
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_minus", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case '*':	
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_times", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			case '/':	
				lexeme = fetchLexemeSymbol();
				t = new Token("mp_divide", lineNumber, columnNumber, lexeme);
				//return t;
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
				t = new Token("mp_identifier", lineNumber, columnNumber, lexeme);
				//return t;
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
				t = new Token("mp_integer_lit", lineNumber, columnNumber, lexeme);
				//return t;
				break;
			default:
				fp.getNext();
				t = null;
				// TODO: other stuff
				break;
		}
		
		// clear whitespace after token (covers us in the event a
		// file ends with white space the driver is made awair of
		// EOF sooner
		fp.skipWhiteSpace();
		
		
		//Token t = null;
		//if (lexeme != null)
			//t = new Token("Token ", lineNumber, columnNumber, lexeme);
		
		// build token from returned lexeme
		// return Token
		//t = new Token("default", lineNumber, columnNumber, "empty");
		return t;
	}
	
	public String fetchLexemeSymbol() {
		String lex = new String();
		char newChar = fp.getNext();
		lex = lex + newChar;
		if (doubleSymbol(newChar)){
			newChar = fp.getNext();
			lex = lex + newChar;
		}
		System.out.print("fetchLexemeSymbol    :  ");
		return lex;
	}
	
	private boolean doubleSymbol(char c){
		if(c == '>' || c == '<' || c == ':'){
			char nextChar = fp.peekNext();
			if (nextChar ==  '=' || nextChar ==  '>'){
				return true;
			}
		}
		return false;
	}
	
	public String fetchLexemeOpenParen() {
		String lex = new String();
		char newChar = fp.getNext();
		if (newChar == '(') {
			lex = lex + newChar;
		}
		System.out.print("fetchLexemeOpenParen :  ");
		return lex;
	}
	public String fetchLexemeCloseParen() {
		String lex = new String();
		char newChar = fp.getNext();
		if (newChar == ')') {
			lex = lex + newChar;
		}
		System.out.print("fetchLexemeCloseParen:  ");
		return lex;
	}
	public String fetchLexemeSemiColon() {
		String lex = new String();
		char newChar = fp.getNext();
		if (newChar == ';') {
			lex = lex + newChar;
		}
		System.out.print("fetchLexemeSemiColon :  ");
		return lex;
	}
	public String fetchLexemeColonOrAssignment() {
		String lex = new String();
		char newChar = fp.getNext();
		if (newChar == ':') {
			lex = lex + newChar;
		}
		if (fp.peekNext() == '='){
			lex = lex +fp.getNext();
			System.out.print("fetchLexemeAssignment:  ");
			return lex;
		} else {
			System.out.print("fetchLexemeColon     :  ");
			return lex;
		}
	}
	public String fetchLexemeIdentifier() {
		String lex = new String();
		boolean sameToken = true;
		while (sameToken) {
			char newChar = fp.getNext();
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
					lex = lex + newChar;
					break;
				default:
					fp.backUp(1);
					sameToken = false;
					break;
			}
		}
		System.out.print("fetchLexemeIdentifier:  ");
		return lex;
	}
	public String fetchLexemeInteger() {
		String lex = new String();
		boolean sameToken = true;
		while (sameToken) {
			char newChar = fp.getNext();
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
					lex = lex + newChar;
					break;
				default:
					fp.backUp(1);
					sameToken = false;
					break;
			}
		}
		System.out.print("fetchLexemeInteger   :  ");
		return lex;
	}	
	
	public boolean endOfFile() {
		return fp.endOfFile();
	}
}
