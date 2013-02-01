/*
 * git address to push
 * https://github.com/jerold/compilers468.git
 */

public class Driver {

	/**
	 * @param args Input file to scan
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner();
		if(args.length == 0){
			scanner.openFile("src/TestFile3.txt");
		} else {
			scanner.openFile(args[0]);
		}
		// print out all the tokens while there are tokens to fetch
		while (!scanner.endOfFile()) {
			Token t = scanner.getToken();
			if (t != null){
				t.describe();
			}
		}
		
		System.exit(0);
	}

}
