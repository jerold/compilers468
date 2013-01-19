
public class Scanner {
	static FilePointer fp;
	
	public Scanner() {
		// We don't do anything here
	}
	
	public void openFile(String fileIn) {
		fp = new FilePointer("src/testFile.txt");
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
		char nextChar = fp.peekNext();
		while (nextChar == '\u0000') {
			fp.getNext();
			nextChar = fp.peekNext();
		}
		
		String lexeme = null;
		switch(nextChar) {
			case '(':
				lexeme = fetchLexemeOpenParen();
				break;
			case ')':
				lexeme = fetchLexemeCloseParen();
				break;
			case ';':
				lexeme = fetchLexemeSemiColon();
				break;
			case ':':
				lexeme = fetchLexemeColonOrAssignment();
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
				break;
			default:
				// TODO: other stuff
				break;
		}
		
		// clear whitespace after token (covers us in the event a
		// file ends with white space the driver is made awair of
		// EOF sooner
		nextChar = fp.peekNext();
		while (nextChar == '\u0000' && !fp.endOfFile()) {
			fp.getNext();
			nextChar = fp.peekNext();
		}
		fp.setPeekToBufferColumn();
		
		
		Token t = null;
		if (lexeme != null)
			t = new Token("Token ", getLineNumber(), (getColumnNumber() - lexeme.length()), lexeme);
		
		// build token from returned lexeme
		// return Token
		
		fp.getNext(); // This is not needed once the fetchLexeme methods start advancing columnNumber in fp
		return t;
	}
	
	public String fetchLexemeOpenParen() {
		System.out.println("fetchLexemeOpenParen");
		return null;
	}
	public String fetchLexemeCloseParen() {
		System.out.println("fetchLexemeCloseParen");
		return null;
	}
	public String fetchLexemeSemiColon() {
		System.out.println("fetchLexemeSemiColon");
		return null;
	}
	public String fetchLexemeColonOrAssignment() {
		System.out.println("fetchLexemeColonOrAssignment");
		return null;
	}
	public String fetchLexemeIdentifier() {
		System.out.println("fetchLexemeIdentifier");
		
		return null;
	}
	public String fetchLexemeInteger() {
		System.out.println("fetchLexemeInteger");
		return null;
	}	
	
	public int getLineNumber() {
		return fp.getLineNumber();
	}
	
	public int getColumnNumber() {
		return fp.getColumnNumber();
	}
	
	public boolean endOfFile() {
		return fp.endOfFile();
	}
}
