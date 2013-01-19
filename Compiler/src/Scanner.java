
public class Scanner {
	static FilePointer fp;
	
	public static void main(String[] args) {
		
		// An example of how to use the FilePointer
		fp = new FilePointer("src/testFile.txt");
		char newChar = fp.getNext();
		while (!fp.endOfFile()) {
			System.out.print(newChar);
			newChar = fp.getNext();
		}
		System.out.println("");
		
		System.out.println("Hello World!");
	}
	
	public Token getNextToken() {
		skipWhiteSpace();
		char x = examine();
		Tokenizer T = dispatch(x);
		T.Scan();
		return new Token();
	}
	
	private Tokenizer dispatch(char x) {
		Tokenizer T = new TokenizerNumeric(); // Gives error if not initially set
		switch(x) {
			case '(':
				// TODO: (
				break;
			case ')':
				// TODO: )
				break;
			case ';':
				// TODO: ;
				break;
			case ':':
				// TODO: :
				break;
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
				// TODO: vars
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				T = new TokenizerNumeric();
				break;
//			case EOF:
//				// TODO: EOF
//				break;
			default:
				// TODO: other stuff
				break;
		}
		return T;
	}
	
	private void skipWhiteSpace() {
		// TODO: skip whitespace
	}
	
	private char examine() {
		// TODO: return char at file pointer but do not move file pointer
		return 'x';
	}
	
}
