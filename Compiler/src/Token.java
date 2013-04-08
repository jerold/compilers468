public class Token {
	
	// private static int tokenIdIterator = 0;
	private String identifier, lexeme;
	private int lineNum, colNum;
	
	/* 
	 * constructor for Token class with four parameters needed to 
	 * construct a Token object.
	 */
	public Token(String identifier, int lineNum, int colNum, String lexeme){
		this.identifier = identifier;
		// this.identifier = identifier + tokenIdIterator++; // For unique token id
		this.lineNum = lineNum+1;
		this.colNum = colNum;
		this.lexeme = lexeme;
	}
	
	public void describe() {
		System.out.println("[" + identifier + "] [" + lineNum + "] [" + colNum + "] [" + lexeme + "]");
	}
	
	public boolean compareIdentifier(String comp) {
		if (comp.equals("integer") && identifier.equals("mp_integer_lit")) {
			return true;
		} else if (comp.equals("float") && identifier.equals("mp_fixed_lit")) {
			return true;
		} else if (comp.equals("boolean") && (identifier.equals("mp_true") || identifier.equals("mp_false"))) {
			return true;
		} else if (comp.equals("string") && identifier.equals("mp_string_lit")) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}
	public String getLexeme() {
		return lexeme;
	}
	public int getLineNum() {
		return lineNum;
	}
	public int getColNum() {
		return colNum;
	}
}