import java.util.ArrayList;
import java.util.ListIterator;

public class Table {

	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	Table parent = null;
	
	private Table(Table parent) {
		this.parent = parent;
	}
	
	public static Table rootInstance() {
		return new Table(null);
	}
	
	public Table createScope() {
		Table t = new Table(null);
		t.parent = this;
		return t;
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
		while (iter.hasNext()){
			Symbol row = iter.next();
			System.out.println("----------------------------------");
			row.describe();
		}
		System.out.println("----------------------------------");
	}

}
