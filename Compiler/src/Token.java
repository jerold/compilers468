public class Token {
	
	private static int tokenIdIterator = 0;
	private String identifier, lexeme;
	private int lineNum, colNum;
	
	/* 
	 * constructor for Token class with four parameters needed to 
	 * construct a Token object.
	 */
	public Token(String identifier, int lineNum, int colNum, String lexeme){
		this.identifier = identifier + tokenIdIterator++;
		this.lineNum = lineNum;
		this.colNum = colNum;
		this.lexeme = lexeme;
	}
	
	public void describe() {
		System.out.println("[" + identifier + "] [" + lineNum + "] [" + colNum + "] [" + lexeme + "]");
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