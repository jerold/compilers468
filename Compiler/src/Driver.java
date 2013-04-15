/*
 * git address to push
 * https://github.com/jerold/compilers468.git
 */

public class Driver {
	
	public enum SymType {
	    intlit, fixedlit, stringlit, bool 
	}
	
	// TODO: Type checking (maybe we should keep an internal representation of what the stack looks like? (float, float, int, float, etc).

	/**
	 * @param args Input file to scan
	 */
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner();
		Compiler compiler = new StudentCompiler();
		
		/*
		
		if(args.length == 0){
			scanner.openFile("src/testFile2.txt");
		} else {
			scanner.openFile(args[0]);
		}
		// print out all the tokens while there are tokens to fetch
		Parser parser = new Parser(scanner,compiler);
		parser.run();
		
		// write and execute file on ESUS if possible (if cannot connect or the VM cannot be reached, just print a success message)
		if (compiler.checkOK()) {
			ServerUpload su = new ServerUpload();
			su.stripMessage();
			boolean success = su.go();
			// compiled, but unable to use server upload
			if (!success) {
				System.out.println("File compiled successfully.");
			}
		} else {
			System.out.println("File failed to compile.");
		}
		
		*/
		
		
		TestSuite ts = new TestSuite(compiler,"TestSuite");
		ts.run();
		
		System.exit(0);
	}

}
