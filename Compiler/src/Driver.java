
public class Driver {

	/**
	 * @param args
	 * git address to push
	 * https://github.com/jerold/compilers468.git
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner();
		if(args.length == 0){
			// System.out.print("Please enter a filename to be compiled");
			scanner.openFile("src/testFile.txt"); // Eventually use input instead of this hard value
		} else {
			scanner.openFile(args[0]);
		}
		// print out all the tokens while there are tokens to fetch
		while (!scanner.endOfFile()) {
			Token t = scanner.getToken();
			if (t != null)
				t.describe();
		}
		
		System.exit(0);
	}

}
