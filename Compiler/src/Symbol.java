

public class Symbol {

	String name;
	String token;
	String type;	
	String[][] attributes;
	int offset;
	String label;
	int level;
	Table parent;
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 * @param offset		The offset within the scope
	 */
	public Symbol(Table parent, String name, String token, String type, String[][] attributes, int offset) {
		this.parent = parent;
		this.name = name;
		this.token = token;
		this.type = type;
		this.offset = offset;
		this.attributes = attributes;
		this.label = null;
	}
	
	/**
	 * get the lexeme of the symbol
	 * @return the lexeme
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * get the token(var, function...) of the symbol
	 * @return the token type
	 */
	public String getToken() {
		return this.token;
	}
	
	/**
	 * get the address(offset of runtime memory stack) of the symbol
	 * call helper method
	 * @return string representing the address offset
	 */
	public String getAddress() {
		return getAddress(true);
	}
	
	/**
	 * get the address(offset of runtime memory stack) of the symbol
	 * dereferenced immediately if it is of type var
	 * @return string representing the address offset
	 */
	public String getAddress(boolean dereferenced) {
		String address = offset+"(D"+parent.getLevel()+")";
		if (token=="var" && dereferenced) {
			address = "@"+address;
		}
		return address;
	}
	
	/**
	 * get the level of the symbol
	 * @return the level
	 */
	public int getLevel() {
		return parent.getLevel();
	}
	
	/**
	 * get the offset of the symbol
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * get the attributes of the symbol
	 * @return the attributes
	 */
	public String[] getAttribute(int index) {
		if (attributes.length>index)
			return attributes[index];
		else
			return null;
	}
	
	/**
	 * get the attribute address of the symbol
	 * @return the String representing the nesting level
	 */
	public String getAttributeAddress(int index) {
		return index+"(D"+level+")";
	}
	
	/**
	 * Checks the name and token type to determine whether or not they were equivalent.
	 * 
	 * @param x	The symbol to compare to
	 * @return	Whether or not the symbols were equal
	 */
	public boolean equals(Symbol x) {
		if (x==null && this.type==null && this.attributes==null)
			return false;
		
		if (this.name.equalsIgnoreCase(x.name) && x.token==null)
			return true;
		else if (this.name.equalsIgnoreCase(x.name) && this.token.equalsIgnoreCase(x.token))
			return true;
		else
			return false;
	}
	
	/**
	 * prints out the name, token, type, attributes, and offset of a symbol
	 */
	public void describe(){
		System.out.print(this.name + "   " + this.token + "   " + this.type + "   ");
		if (this.attributes==null) {
			System.out.print("no attributes");
		} else {
			for (int i=0; i<this.attributes.length; i++) {
				if (i>0) System.out.print(", ");
				System.out.print(this.attributes[i][0]+":"+this.attributes[i][1]);
			}
		}
		System.out.print("   " + this.offset);
		if (this.label!=null) 
			System.out.print("   " + this.label);
		System.out.print("   level:" + this.getLevel());
		System.out.println("");
	}
	
	/**
	 * get the type of the symbol as a string
	 * @return the type
	 */
	public String getTypeString() {
		return type;
	}
	
	/**
	 * get the enumerated type of the symbol as a 
	 * semantic record type
	 * @return the SR type
	 */
	public SR getType() {
		SR sr = null;
		switch(type) {
			case "integer":
				sr = SR.intlit();
				break;
			case "float":
				sr = SR.fixedlit();
				break;
			case "string":
				sr = SR.stringlit();
				break;
			case "boolean":
				sr = SR.bool();
				break;
			default:
				sr = null;
				break;
		}
		return sr;
	}
}
