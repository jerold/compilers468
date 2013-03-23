import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class StudentCompiler implements Compiler {

	private String root;	// the root directory to compile to
	private String output;  // filename to output to
	private int label;		// Incrementor for label
	FileWriter fstream;
	BufferedWriter out;
	
	public StudentCompiler() {
		label = 0;
		root = "src/bin/";
		output = root+"uMachine_code.il";
	}
	
	public void openFile(String output) {
		this.output = root+output;
		try {
			fstream = new FileWriter(output);
		} catch (IOException e) {
			System.out.println("Unable to open output file");
		}
		out = new BufferedWriter(fstream);
	}
	
	public void write(String code) {
		try {
			out.write(code);
		} catch (IOException e) {
			System.out.println("Unable to write to the output file");
		}
	}
	
	// TODO: move this down below to where it belongs
	
	// HALT instruction
	
	public void halt() {
		write("HLT");
	}
	
	// I/O instructions
	
	public void readInt() {
		write("RD dst");
	}
	
	public void readFloat() {
		write("RDF dst");
	}
	
	public void write() {
		write("WRT src");
	}
	
	public void writeStack() {
		write("WRTS");
	}
	
	public void writeLine() {
		write("WRTLN src");
	}
	
	public void writeLineStack() {
		write("WRTLNS");
	}
	
	// MEMORY instructions
	
	public void move() {
		write("MOV src dst");
	}
	
	//  - INTEGER
	
	public void negate() {
		write("NEG src dst");
	}
	
	public void add() {
		write("ADD src1 src2 dst");
	}
	
	public void subtract() {
		write("SUB src1 src2 dst");
	}
	
	public void multiply() {
		write("MUL src1 src2 dst");
	}
	
	public void divide() {
		write("DIV src1 src2 dst");
	}
	
	public void modulus() {
		write("MOD src1 src2 dst");
	}
	
	//  - FLOAT
	
	public void negateFloat() {
		write("NEGF src dst");
	}
	
	public void addFloat() {
		write("ADDF src1 src2 dst");
	}
	
	public void subtractFloat() {
		write("SUBF src1 src2 dst");
	}
	
	public void multiplyFloat() {
		write("MULF src1 src2 dst");
	}
	
	public void divideFloat() {
		write("DIVF src1 src2 dst");
	}
	
	//  - CASTING
	
	public void castInteger() {
		write("CASTI src dst");
	}
	
	public void castFloat() {
		write("CASTF src dst");
	}
	
	// STACK instructions
	
	public void push() {
		write("PUSH src");
	}
	
	public void pop() {
		write("POP dst");
	}
	
	//  - INTEGER
	
	public void negateStack() {
		write("NEGS");
	}
	
	public void addStack() {
		write("ADDS");
	}
	
	public void subtractStack() {
		write("SUBS");
	}
	
	public void multiplyStack() {
		write("MULS");
	}
	
	public void divideStack() {
		write("DIVS");
	}
	
	public void modulusStack() {
		write("MODS");
	}
	
	//  - FLOAT
	
	public void negateStackFloat() {
		write("NEGSF");
	}
	
	public void addStackFloat() {
		write("ADDSF");
	}
	
	public void subtractStackFloat() {
		write("SUBSF");
	}
	
	public void multiplyStackFloat() {
		write("MULSF");
	}
	
	public void divideStackFloat() {
		write("DIVSF");
	}
	
	//  - CASTING
	
	public void castStackInteger() {
		write("CASTSI");
	}
	
	public void castStackFloat() {
		write("CASTSF");
	}
	
	// LABEL instructions
	
	public void label() {
		write("L"+label++);
	}
	
	// LOGICAL OPERATOR instructions - defined for Booleans
	
	public void andStack() {
		write("ANDS");
	}
	
	public void orStack() {
		write("ORS");
	}
	
	public void notStack() {
		write("NOTS");
	}
	
	// COMPARE instructions - defined for Integers and Floats
	
	//  - INTEGER
	
	public void compareEqualStack() {
		write("CMPEQS");
	}
	
	public void compareGreaterEqualStack() {
		write("CMPGES");
	}
	
	public void compareGreaterStack() {
		write("CMPGTS");
	}
	
	public void compareLessEqualStack() {
		write("CMPLES");
	}
	
	public void compareLessStack() {
		write("CMPLTS");
	}
	
	public void compareNotEqualStack() {
		write("CMPNES");
	}
	 
	//  - INTEGER
		
	public void compareEqualStackFloat() {
		write("CMPEQSF");
	}
	
	public void compareGreaterEqualStackFloat() {
		write("CMPGESF");
	}
	
	public void compareGreaterStackFloat() {
		write("CMPGTSF");
	}
	
	public void compareLessEqualStackFloat() {
		write("CMPLESF");
	}
	
	public void compareLessStackFloat() {
		write("CMPLTSF");
	}
	
	public void compareNotEqualStackFloat() {
		write("CMPNESF");
	}
	
	// STACK BRANCH instructions - defined for Booleans
	
	public void branchTrueStack(int n) {
		write("BRTS L"+n);
	}
	
	public void branchFalseStack(int n) {
		write("BRFS L"+n);
	}
			
	// BRANCH instructions - defined for Integers and Floats
	
	public void branch(int n) {
		write("BR L"+n);
	}
			
	//  - INTEGER
	
	public void branchEqual(int n) {
		write("BEQ src1 src2 L"+n);
	}
	
	public void branchGreaterEqual(int n) {
		write("BGE src1 src2 L"+n);
	}
	
	public void branchGreater(int n) {
		write("BGT src1 src2 L"+n);
	}
	
	public void branchLessEqual(int n) {
		write("BLE src1 src2 L"+n);
	}
	
	public void branchLess(int n) {
		write("BLT src1 src2 L"+n);
	}
	
	public void branchNotEqual(int n) {
		write("BNE src1 src2 L"+n);
	}

	//  - FLOAT
	
	public void branchEqualFloat(int n) {
		write("BEQF src1 src2 L"+n);
	}
	
	public void branchGreaterEqualFloat(int n) {
		write("BGEF src1 src2 L"+n);
	}
	
	public void branchGreaterFloat(int n) {
		write("BGTF src1 src2 L"+n);
	}
	
	public void branchLessEqualFloat(int n) {
		write("BLEF src1 src2 L"+n);
	}
	
	public void branchLessFloat(int n) {
		write("BLTF src1 src2 L"+n);
	}
	
	public void branchNotEqualFloat(int n) {
		write("BNEF src1 src2 L"+n);
	}
	
	// SUBROUTINE instructions
	
	public void call(int n) {
		write("CALL L"+n);
	}
	
	public void returnCall() {
		write("RET");
	}

}