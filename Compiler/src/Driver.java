/*
 * git address to push
 * https://github.com/jerold/compilers468.git
 */

public class Driver {
	
	// TODO: Type checking (maybe we should keep an internal representation of what the stack looks like? (float, float, int, float, etc).

	/**
	 * @param args Input file to scan
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner();
		Compiler compiler = new StudentCompiler();
		if(args.length == 0){
			scanner.openFile("src/testFile2.txt");
		} else {
			scanner.openFile(args[0]);
		}
		// print out all the tokens while there are tokens to fetch
		Parser parser = new Parser(scanner,compiler);
		parser.setRamSize(1000);
		parser.run();
		
		System.exit(0);
	}

}
