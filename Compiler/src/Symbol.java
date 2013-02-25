
public class Symbol {

	String name;
	String token;		// change to enum?
	String type;		// change to enum? rename?
	String attributes;
	int offset;
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 * @param offset		The offset within the scope
	 */
	public Symbol(String name, String token, String type, String attributes, int offset) {
		this.name = name;
		this.token = token;
		this.type = type;
		this.offset = offset;
		this.attributes = attributes;
	}
	
	/**
	 * Checks the name and token type to determine whether or not they were equivelent.
	 * 
	 * @param x	The symbol to compare to
	 * @return	Whether or not the symbols were equal
	 */
	public boolean equals(Symbol x) {
		if (this.name == x.name && this.token == x.token)
			return true;
		else
			return false;
	}
	
}
