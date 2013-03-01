import java.util.ArrayList;
import java.util.ListIterator;

public class Table {

	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	private Table parent = null;
	//Table child = null;
	private String title= null;
	private static Table root = new Table(null);
	
	private Table(Table parent) {
		this.parent = parent;
	}
	
	public static Table rootInstance() {
		return root;
	}
	
	public Table createScope() {
		return new Table(this);
	}
	
	/**
	 * 
	 * @param name		The name of the symbol to find
	 * @param token		The token type of the symbol to find
	 * @return			The symbol that was found
	 */
	public Symbol findSymbol(String name, String token) {
		Symbol x = new Symbol(name,token,null,null,0);
		return findSymbol(x);
	}
	
	/**
	 * 
	 * @param name		The name of the symbol to find
	 * @param token		The token type of the symbol to find
	 * @return			The symbol that was found
	 */
	public boolean inTable(String name, String token) {
		Symbol x = new Symbol(name,token,null,null,0);
		x = findSymbol(x);
		if(x == null){
			return false;
		} else {
			return true;
		}
	}
	
	public Symbol findSymbol(Symbol x) {
		for (int i=0; i<symbols.size(); i++) {
			if (symbols.get(i).equals(x))
				return (Symbol)symbols.get(i);
		}
		if (parent!=null)
			return parent.findSymbol(x);
		else
			return null;
	}
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 */
	public void insert(String name, String token, String type, String attributes) {
		insertSymbol(name,token,type,attributes);
	}
	
	/**
	 * Creates a row representing a symbol within a table.
	 * 
	 * @param name			The name of the symbol (a, fred)
	 * @param token			The token type (var, procedure)
	 * @param type			The type of token (int, float)
	 * @param attributes	Any attributes that may be passed
	 */
	private void insertSymbol(String name, String token, String type, String attributes) {
		symbols.add(new Symbol(name,token,type,attributes,symbols.size()));
	}
	
	/**
	 *  print out the current table
	 */
	public void describe(){
		ListIterator<Symbol> iter = symbols.listIterator();
		System.out.println();
		System.out.println("Table " + title);
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

}
