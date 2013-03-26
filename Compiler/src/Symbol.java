import java.util.ArrayList;


public class Symbol {

	String name;
	String token;		// change to enum?
	String type;		// change to enum? rename?
	//String attributes;
	String[][] attributes;
	//ArrayList<String> attributes = new ArrayList<String>();
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
	
	public String getAddress() {
		return offset+"(D"+parent.getLevel()+")";
	}
	
	public String[] getAttribute(int index) {
		return attributes[index];
	}
	
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
		
		if (this.name.equalsIgnoreCase(x.name) && this.token.equalsIgnoreCase(x.token))
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
		if (this.level>0)
			System.out.print("   level:" + this.level);
		System.out.println("");
	}
}
