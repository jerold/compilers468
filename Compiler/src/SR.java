
public class SR {
	
	public SymType symtype;
	
	public static enum SymType {
	    intlit, fixedlit, stringlit, bool 
	}
	
	private SR(SymType t) {
		symtype = t;
	}
	
	public static SR intlit() {
		return new SR(SymType.intlit);
	}
	
	public static SR fixedlit() {
		return new SR(SymType.fixedlit);
	}
	
	public static SR stringlit() {
		return new SR(SymType.stringlit);
	}
	
	public static SR bool() {
		return new SR(SymType.bool);
	}
	
	public boolean checkIntlit() {
		if (symtype==SymType.intlit) {
			return true;
		}
		return false;
	}
	
	public boolean checkFixedlit() {
		if (symtype==SymType.fixedlit) {
			return true;
		}
		return false;
	}
	
	public boolean checkStringlit() {
		if (symtype==SymType.stringlit) {
			return true;
		}
		return false;
	}
	
	public boolean checkBool() {
		if (symtype==SymType.bool) {
			return true;
		}
		return false;
	}
	
	public void describe() {
		System.out.println("TYPE: "+symtype.toString());
	}

}
