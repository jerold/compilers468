import java.util.ArrayList;
import java.util.ListIterator;

public class Table {

	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	private Table parent = null;
	private String title= null;
	private static Table root = new Table(null);
	private int level = 0;
	private int address;
	
	/**
	 * singleton constructor for the Table
	 * 
	 * @param parent	referenced parent table
	 */
	private Table(Table parent) {
		this.parent = parent;  
	}
	
	/**
	 * get the root instance of the Table
	 * @return the root of the Table
	 */
	
	public static Table rootInstance() {
		return root;
	}
	
	/**
	 * destroys the symbol table -- needed to test 
	 * multiple files in the test suite
	 */
	public static void clear() {
		root = new Table(null);
	}
	
	/**
	 * add a new table to the table hierarchy
	 * @return the full table hierarchy
	 */
	public Table createScope() {
		Table table = new Table(this);
		table.level = (this.level+1);
		return table;
	}
	
	/**
	 * get the level of the symbol table
	 * @return the level of the symbol table currently in scope
	 */
	public int getLevel() {
		return this.level;
	}
	
	/**
	 * set the contents of the register for the current level
	 * @param address	the address of a variable
	 */
	public void setAddress(int address) {
		this.address = address;
	}
	
	/**
	 * get the contents of the register for the current level
	 * @return	the address of a variable
	 */
	public int getAddress() {
		return address;
	}
	
	/**
	 * 
	 * @param index		The index of the symbol to return
	 * @return			The symbol that was found
	 */
	public Symbol getSymbol(int index) {
		if(index > symbols.size()){
			return null;
		} else {
			return symbols.get(index);
		}
	}
	
	/**
	 * find the element in a table by searching on type
	 * @param lookAhead the token being searched for
	 * @return	the symbol associated with parameter Token
	 */
	public Symbol findSymbol(Token lookAhead) {
		Symbol s = findSymbol(lookAhead.getLexeme(), "var");
		if (s==null)
			s =  findSymbol(lookAhead.getLexeme(), "value");
		if (s==null)
			s =  findSymbol(lookAhead.getLexeme(), "procedure");
		if (s==null)
			s = findSymbol(lookAhead.getLexeme(), "function");
		return s;
	}
	
	/**
	 * 
	 * @param name		The name of the symbol to find
	 * @param token		The token type of the symbol to find
	 * @return			The symbol that was found
	 */
	public Symbol findSymbol(String name, String token) {
		Symbol x = new Symbol(this,name,token,null,null,0);
		return findSymbol(x);
	}
	
	public Symbol findSymbol(Symbol x) {
		return findSymbol(x,false);
	}
	
	public Symbol findSymbol(Symbol x, boolean inScope) {
		for (int i=0; i<symbols.size(); i++) {
			if (symbols.get(i).equals(x)) {
				return (Symbol)symbols.get(i);
			}
		}
		if (parent!=null && !inScope)
			return parent.findSymbol(x);
		else
			return null;
	}
	
	/**
	 * 
	 * @param name		The name of the symbol to find
	 * @param token		The token type of the symbol to find
	 * @return			The symbol that was found
	 */
	public boolean inTable(String name, String token) {
		Symbol x = new Symbol(this,name,token,null,null,0);
		x = findSymbol(x);
		if(x == null){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * @param name		The name of the symbol to find
	 * @return			The symbol that was found
	 */
	public boolean inTable(String name) {
		return inTable(name,false);
	}
	
	public boolean inTable(String name, boolean inScope) {
		Symbol x = new Symbol(this,name,null,null,null,0);
		x = findSymbol(x,inScope);
		if(x == null){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 */
	public Symbol insert(String name, String token, String type, String[][] attributes) {
		return insertSymbol(name,token,type,attributes);
	}
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 */
	private Symbol insertSymbol(String name, String token, String type, String[][] attributes) {
		if (inTable(name,true)) {
			return null;
		} else {
			Symbol s = new Symbol(this,name,token,type,attributes,symbols.size());
			symbols.add(s);
			return s;
		}
	}
	
	/**
	 *  print out the current table
	 */
	public void describe(){
		ListIterator<Symbol> iter = symbols.listIterator();
		System.out.println();
		System.out.println("Table " + title + " (Level "+level+")");
		while (iter.hasNext()){
			Symbol row = iter.next();
			System.out.println("----------------------------------");
			row.describe();
		}
		System.out.println("----------------------------------");
	}
	
	/**
	 * set the title of the table
	 */
	public void setTitle(String s){
		this.title = s;
	}
	
	/**
	 * get the title of the table
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * get the parent of the table
	 */
	public Table getParent(){
		return parent;
	}
	
	/**
	 * get the size of the table (number of elements, not size of elements)
	 */
	public int getSize(){
		return symbols.size();
	}
	
	public int getOffset() {
		if (parent==null) {
			return this.address;
		} else {
			return parent.getSize()+parent.getOffset();
		}
	}

}
