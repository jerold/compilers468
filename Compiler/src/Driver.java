
public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner();
		if(args.length == 0){
			// System.out.print("Please enter a filename to be compiled");
			scanner.openFile("src/testFile.txt"); // Eventually use input instead of this hard value
		} else {
			scanner.openFile((String) args[0]); // Eventually use input instead of this hard value
		}
		
		// print out all the tokens while there are tokens to fetch
		while (!scanner.endOfFile()) {
			Token t = scanner.getToken();
			t.describe();
		}
		
		System.exit(0);
	}

}
