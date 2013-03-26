import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class StudentCompiler implements Compiler {

	private boolean compileFlag;
	private String root;	// the root directory to compile to
	private String output;  // filename to output to
	private int label;		// Incrementor for label
	FileWriter fstream;
	BufferedWriter out;
	private List<String> code = new ArrayList<String>();
	
	public StudentCompiler() {
		compileFlag = true;
		label = 0;
		root = "target/";
		output = "uMachine_code.il";
	}
	
	public void openFile() {
		openFile(output);
	}
	
	public void openFile(String output) {
		try {
			fstream = new FileWriter(root+output);
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			System.out.println("Unable to open output file: "+output);
		}
		
	}
	
	public void closeFile() {
		try{
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: Could not close file.");
		}
	}
	
	private void writeCommand(String command) {
		if (compileFlag) {
			System.out.println(command);
			code.add(command);
		}
	}
	
	public void writeFile() {
		if (compileFlag) {
			try {
				openFile();
				for (String command : code) {
					//System.out.println(command);
					out.write(command+"\n");
				}
				closeFile();
			} catch (IOException e) {
				System.out.println("Unable to write to the output file");
			}
		}
	}
	
	public void turnOff() {
		compileFlag = false;
	}
	
	// HALT instruction
	
	public void halt() {
		writeCommand("HLT");
	}
	
	// I/O instructions
	
	public void readInt(String dst) {
		writeCommand("RD "+dst);
	}
	
	public void readFloat(String dst) {
		writeCommand("RDF "+dst);
	}
	
	public void write(String src) {
		writeCommand("WRT "+src);
	}
	
	public void writeStack() {
		writeCommand("WRTS");
	}
	
	public void writeLine(String src) {
		writeCommand("WRTLN "+src);
	}
	
	public void writeLineStack() {
		writeCommand("WRTLNS");
	}
	
	// MEMORY instructions
	
	public void move(String src, String dst) {
		writeCommand("MOV "+src+" "+dst);
	}
	
	//  - INTEGER
	
	public void negate() {
		writeCommand("NEG src dst");
	}
	
	public void add() {
		writeCommand("ADD src1 src2 dst");
	}
	
	public void subtract() {
		writeCommand("SUB src1 src2 dst");
	}
	
	public void multiply() {
		writeCommand("MUL src1 src2 dst");
	}
	
	public void divide() {
		writeCommand("DIV src1 src2 dst");
	}
	
	public void modulus() {
		writeCommand("MOD src1 src2 dst");
	}
	
	//  - FLOAT
	
	public void negateFloat() {
		writeCommand("NEGF src dst");
	}
	
	public void addFloat() {
		writeCommand("ADDF src1 src2 dst");
	}
	
	public void subtractFloat() {
		writeCommand("SUBF src1 src2 dst");
	}
	
	public void multiplyFloat() {
		writeCommand("MULF src1 src2 dst");
	}
	
	public void divideFloat() {
		writeCommand("DIVF src1 src2 dst");
	}
	
	//  - CASTING
	
	public void castInteger() {
		writeCommand("CASTI src dst");
	}
	
	public void castFloat() {
		writeCommand("CASTF src dst");
	}
	
	// STACK instructions
	
	public void push(String src) {
		writeCommand("PUSH "+src);
	}
	
	public void pop(String dst) {
		writeCommand("POP "+dst);
	}
	
	//  - INTEGER
	
	public void negateStack() {
		writeCommand("NEGS");
	}
	
	public void addStack() {
		writeCommand("ADDS");
	}
	
	public void subtractStack() {
		writeCommand("SUBS");
	}
	
	public void multiplyStack() {
		writeCommand("MULS");
	}
	
	public void divideStack() {
		writeCommand("DIVS");
	}
	
	public void modulusStack() {
		writeCommand("MODS");
	}
	
	//  - FLOAT
	
	public void negateStackFloat() {
		writeCommand("NEGSF");
	}
	
	public void addStackFloat() {
		writeCommand("ADDSF");
	}
	
	public void subtractStackFloat() {
		writeCommand("SUBSF");
	}
	
	public void multiplyStackFloat() {
		writeCommand("MULSF");
	}
	
	public void divideStackFloat() {
		writeCommand("DIVSF");
	}
	
	//  - CASTING
	
	public void castStackInteger() {
		writeCommand("CASTSI");
	}
	
	public void castStackFloat() {
		writeCommand("CASTSF");
	}
	
	// LABEL instructions
	
	public String label() {
		String l = "L"+label++;
		label(l);
		return l;
	}
	
	public void label(String l) {
		writeCommand(l+":");
	}
	
	public String skipLabel() {
		return "L"+label++;
	}
	
	public String getLabel(int index) {
		return "L"+(label+index);
	}
	
	// LOGICAL OPERATOR instructions - defined for Booleans
	
	public void andStack() {
		writeCommand("ANDS");
	}
	
	public void orStack() {
		writeCommand("ORS");
	}
	
	public void notStack() {
		writeCommand("NOTS");
	}
	
	// COMPARE instructions - defined for Integers and Floats
	
	//  - INTEGER
	
	public void compareEqualStack() {
		writeCommand("CMPEQS");
	}
	
	public void compareGreaterEqualStack() {
		writeCommand("CMPGES");
	}
	
	public void compareGreaterStack() {
		writeCommand("CMPGTS");
	}
	
	public void compareLessEqualStack() {
		writeCommand("CMPLES");
	}
	
	public void compareLessStack() {
		writeCommand("CMPLTS");
	}
	
	public void compareNotEqualStack() {
		writeCommand("CMPNES");
	}
	 
	//  - INTEGER
		
	public void compareEqualStackFloat() {
		writeCommand("CMPEQSF");
	}
	
	public void compareGreaterEqualStackFloat() {
		writeCommand("CMPGESF");
	}
	
	public void compareGreaterStackFloat() {
		writeCommand("CMPGTSF");
	}
	
	public void compareLessEqualStackFloat() {
		writeCommand("CMPLESF");
	}
	
	public void compareLessStackFloat() {
		writeCommand("CMPLTSF");
	}
	
	public void compareNotEqualStackFloat() {
		writeCommand("CMPNESF");
	}
	
	// STACK BRANCH instructions - defined for Booleans
	
	public void branchTrueStack(String l) {
		writeCommand("BRTS "+l);
	}
	
	public void branchFalseStack(String l) {
		writeCommand("BRFS "+l);
	}
			
	// BRANCH instructions - defined for Integers and Floats
	
	public void branch(String l) {
		writeCommand("BR "+l);
	}
			
	//  - INTEGER
	
	public void branchEqual(int n) {
		writeCommand("BEQ src1 src2 L"+n);
	}
	
	public void branchGreaterEqual(int n) {
		writeCommand("BGE src1 src2 L"+n);
	}
	
	public void branchGreater(int n) {
		writeCommand("BGT src1 src2 L"+n);
	}
	
	public void branchLessEqual(int n) {
		writeCommand("BLE src1 src2 L"+n);
	}
	
	public void branchLess(int n) {
		writeCommand("BLT src1 src2 L"+n);
	}
	
	public void branchNotEqual(int n) {
		writeCommand("BNE src1 src2 L"+n);
	}

	//  - FLOAT
	
	public void branchEqualFloat(int n) {
		writeCommand("BEQF src1 src2 L"+n);
	}
	
	public void branchGreaterEqualFloat(int n) {
		writeCommand("BGEF src1 src2 L"+n);
	}
	
	public void branchGreaterFloat(int n) {
		writeCommand("BGTF src1 src2 L"+n);
	}
	
	public void branchLessEqualFloat(int n) {
		writeCommand("BLEF src1 src2 L"+n);
	}
	
	public void branchLessFloat(int n) {
		writeCommand("BLTF src1 src2 L"+n);
	}
	
	public void branchNotEqualFloat(int n) {
		writeCommand("BNEF src1 src2 L"+n);
	}
	
	// SUBROUTINE instructions
	
	public void call(String l) {
		writeCommand("CALL "+l);
	}
	
	public void returnCall() {
		writeCommand("RET");
	}
	
	public void printStack() {
		writeCommand("PRTS");
	}
	
	public void printRegisters() {
		writeCommand("PRTR");
	}
	
	public int getLine() {
		return code.size();
	}
	
	public void injectLast(int linenumber) {
		if (linenumber<code.size()-1 && linenumber>=0) {
			String lastline = code.remove(code.size()-1);
			code.add(linenumber, lastline);
		} else {
			System.out.println("Error: Cannot inject line at this location.");
		}
	}

}