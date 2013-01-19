public class Token {
	
	private String identifier, lexeme;
	private int lineNum, colNum;
	
	/* 
	 * constructor for Token class with no parameters. 
	 * This constructs a "default" Token object.
	 */
	public Token(){
		identifier = "id";
		lexeme = "Test";
		lineNum = 1;
		colNum = 1;
	}
	/* 
	 * constructor for Token class with four parameters needed to 
	 * construct a Token object.
	 */
	public Token(String identifier, int lineNum, int colNum, String lexeme){
		this.identifier = identifier;
		this.lineNum = lineNum;
		this.colNum = colNum;
		this.lexeme = lexeme;
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