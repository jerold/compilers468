
public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length == 0){
			System.out.print("Please enter a filename to be compiled");
			System.exit(1);
		//scan the entire file and get list of tokens
		} else {
			Scanner scanner = new Scanner();
			scanner.openFile("src/testFile.txt"); // Eventually use input instead of this hard value
			
			// ArrayList<Token> tokens = new ArrayList<Token>();
			// while(scanner.hasNextToken()){
			// 	tokens.add(scanner.getNextToken());
			// }
		}
		System.exit(0);
	}

}
